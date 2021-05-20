package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Iterables;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public final class TripDistribution extends Module {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);
    public final static AtomicInteger completelyRandomNhbTrips = new AtomicInteger(0);

    //todo turn to static to be mantained for both mandatory and discretionary - we expect to remove ttb from the trip distribution
    private static EnumMap<Purpose, IndexedDoubleMatrix2D> utilityMatrices = new EnumMap<>(Purpose.class);

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    private final Map<Purpose, Double> travelDistanceCalibrationParameters;
    private final Map<Purpose, Double> impedanceCalibrationParameters;
    private final boolean useBudgetsInDestinationChoice;

    private final DestinationUtilityCalculatorFactory destinationUtilityCalculatorFactory;

    public TripDistribution(DataSet dataSet, List<Purpose> purposes, Map<Purpose, Double> travelDistanceCalibrationParameters,
                            Map<Purpose, Double> impedanceCalibrationParameters, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactory destinationUtilityCalculatorFactory) {
        super(dataSet, purposes);
        this.travelDistanceCalibrationParameters = travelDistanceCalibrationParameters;
        this.impedanceCalibrationParameters = impedanceCalibrationParameters;
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
    }

    public TripDistribution(DataSet dataSet, List<Purpose> purposes, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactory destinationUtilityCalculatorFactory) {
        super(dataSet, purposes);
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
        travelDistanceCalibrationParameters = new HashMap<>();
        impedanceCalibrationParameters = new HashMap<>();
        for (Purpose purpose : Purpose.getAllPurposes()){
            travelDistanceCalibrationParameters.put(purpose, 1.0);
            impedanceCalibrationParameters.put(purpose, 1.0);
        }


    }

    @Override
    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();

        logger.info("Distributing trips for households...");
        distributeTrips();
    }

    //For moped integration
    public void setUp() {
        logger.info("Building initial destination choice utility matrices...");
        //TODO: is hb utility empty?
        buildMatrices();

        logger.info("finding origins for non home based trips...");
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);
        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();

        final Collection<MitoHousehold> households = dataSet.getHouseholds().values();
        final int partitionSize = (int) ((double) households.size() / (numberOfThreads)) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(households, partitionSize);

        for (final List<MitoHousehold> partition : partitions) {

            for (Purpose purpose : purposes){
                if (purpose.equals(NHBW)){
                    nonHomeBasedTasks.add(NhbwNhboOrigin.nhbw(utilityMatrices, partition, dataSet.getZones()));
                } else if (purpose.equals(NHBO)){
                    nonHomeBasedTasks.add(NhbwNhboOrigin.nhbo(utilityMatrices, partition, dataSet.getZones()));
                }
            }

        }

        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);
    }


    //For moped integration
    public void runHomeBased() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();

        logger.info("Distributing home based trips for households...");
        distributeHomeBasedTrips();
    }

    //For moped integration
    public void runNonHomeBased() {
        logger.info("Distributing non home based trips for households...");
        distributeNonHomeBasedTrips();
    }

    private void buildMatrices() {
        List<Callable<Tuple<Purpose,IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (Purpose purpose : purposes) {
            if (!purpose.equals(Purpose.AIRPORT)){
                //Distribution of trips to the airport does not need a matrix of weights
                utilityCalcTasks.add(new DestinationUtilityByPurposeGenerator(purpose, dataSet,
                        destinationUtilityCalculatorFactory,
                        travelDistanceCalibrationParameters.get(purpose),
                        impedanceCalibrationParameters.get(purpose)));
            }
        }
        ConcurrentExecutor<Tuple<Purpose, IndexedDoubleMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Tuple<Purpose,IndexedDoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Tuple<Purpose, IndexedDoubleMatrix2D> result: results) {
            utilityMatrices.put(result.getFirst(), result.getSecond());
        }
    }

    private void distributeTrips() {
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);

        final Collection<MitoHousehold> households = dataSet.getHouseholds().values();
        final int partitionSize = (int) ((double) households.size() / (numberOfThreads)) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(households, partitionSize);

        logger.info("Using " + numberOfThreads + " thread(s)" +
                " with partitions of size " + partitionSize);

        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        for (final List<MitoHousehold> partition : partitions) {
            for (Purpose purpose : purposes){
                if (purpose.equals(HBW)){
                    homeBasedTasks.add(HbeHbwDistribution.hbw(utilityMatrices.get(purpose), partition, dataSet.getZones()));
                } else if (purpose.equals(HBE)) {
                    homeBasedTasks.add(HbeHbwDistribution.hbe(utilityMatrices.get(purpose), partition, dataSet.getZones()));
                } else if (purpose.equals(HBS)){
                    homeBasedTasks.add(HbsHboDistribution.hbs(utilityMatrices.get(purpose), partition, dataSet.getZones(),
                            dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
                } else if (purpose.equals(HBO)) {
                    homeBasedTasks.add(HbsHboDistribution.hbo(utilityMatrices.get(purpose), partition, dataSet.getZones(),
                            dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
                } else if (purpose.equals(HBR)){
                    homeBasedTasks.add(HbsHboDistribution.hbr(utilityMatrices.get(purpose), partition, dataSet.getZones(),
                            dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
                }
            }
        }

        executor.submitTasksAndWaitForCompletion(homeBasedTasks);

        executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);
        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();

        for (final List<MitoHousehold> partition : partitions) {

            for (Purpose purpose : purposes){
                if (purpose.equals(NHBW)){
                    nonHomeBasedTasks.add(NhbwNhboDistribution.nhbw(utilityMatrices, partition, dataSet.getZones(),
                            dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
                } else if (purpose.equals(NHBO)){
                    nonHomeBasedTasks.add(NhbwNhboDistribution.nhbo(utilityMatrices, partition, dataSet.getZones(),
                            dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
                }


            }

        }
        if (Resources.instance.getBoolean(Properties.ADD_AIRPORT_DEMAND, false)) {
            nonHomeBasedTasks.add(AirportDistribution.airportDistribution(dataSet));
        }
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);

        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
        if(randomOccupationDestinationTrips.get() > 0) {
            logger.info("There have been " + randomOccupationDestinationTrips.get() +
                    " HBW or HBE trips not done by a worker or student or missing occupation zone. " +
                    "Picked a destination by random utility instead.");
        }
        if(completelyRandomNhbTrips.get() > 0) {
            logger.info("There have been " + completelyRandomNhbTrips + " NHBO or NHBW trips" +
                    "by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                    + " trip as either origin or destination for the non-home-based trip.");
        }
    }

    private void distributeHomeBasedTrips() {
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);

        final Collection<MitoHousehold> households = dataSet.getHouseholds().values();
        final int partitionSize = (int) ((double) households.size() / (numberOfThreads)) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(households, partitionSize);

        logger.info("Using " + numberOfThreads + " thread(s)" +
                " with partitions of size " + partitionSize);

        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        for (final List<MitoHousehold> partition : partitions) {
            homeBasedTasks.add(HbsHboDistribution.hbs(utilityMatrices.get(HBS), partition, dataSet.getZones(),
                    dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
            homeBasedTasks.add(HbsHboDistribution.hbo(utilityMatrices.get(HBO), partition, dataSet.getZones(),
                    dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
            homeBasedTasks.add(HbsHboDistribution.hbr(utilityMatrices.get(HBR), partition, dataSet.getZones(),
                    dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
            homeBasedTasks.add(HbeHbwDistribution.hbw(utilityMatrices.get(HBW), partition, dataSet.getZones()));
            homeBasedTasks.add(HbeHbwDistribution.hbe(utilityMatrices.get(HBE), partition, dataSet.getZones()));
        }

        executor.submitTasksAndWaitForCompletion(homeBasedTasks);

        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
        if(randomOccupationDestinationTrips.get() > 0) {
            logger.info("There have been " + randomOccupationDestinationTrips.get() +
                    " HBW or HBE trips not done by a worker or student or missing occupation zone. " +
                    "Picked a destination by random utility instead.");
        }

    }

    private void distributeNonHomeBasedTrips() {
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);

        final Collection<MitoHousehold> households = dataSet.getHouseholds().values();
        final int partitionSize = (int) ((double) households.size() / (numberOfThreads)) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(households, partitionSize);

        logger.info("Using " + numberOfThreads + " thread(s)" +
                " with partitions of size " + partitionSize);

        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();

        for (final List<MitoHousehold> partition : partitions) {
            nonHomeBasedTasks.add(NhbwNhboDistribution.nhbw(utilityMatrices, partition, dataSet.getZones(),
                    dataSet.getTravelTimes(), dataSet.getPeakHour(), useBudgetsInDestinationChoice));
            nonHomeBasedTasks.add(NhbwNhboDistribution.nhbo(utilityMatrices, partition, dataSet.getZones(),
                    dataSet.getTravelTimes(), dataSet.getPeakHour(),useBudgetsInDestinationChoice));
        }
        if (Resources.instance.getBoolean(Properties.ADD_AIRPORT_DEMAND, false)) {
            nonHomeBasedTasks.add(AirportDistribution.airportDistribution(dataSet));
        }
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);


        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);

        if(completelyRandomNhbTrips.get() > 0) {
            logger.info("There have been " + completelyRandomNhbTrips + " NHBO or NHBW trips" +
                    "by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                    + " trip as either origin or destination for the non-home-based trip.");
        }
    }
}
