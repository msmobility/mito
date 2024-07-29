package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AtomicDouble;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripDistribution.tripDistributors.*;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public class TripDistribution extends Module {


    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    protected final EnumMap<Purpose, List<tripDistributionData>> tripDistributionDataByPurpose = new EnumMap<>(Purpose.class);
    protected final EnumMap<Purpose,Map<Integer,Integer>> personCategories = new EnumMap<>(Purpose.class);
    private final Map<Purpose, Tuple<AbstractDestinationUtilityCalculator,TripDistributorType>> tripDistributionCalculatorsByPurpose = new EnumMap<>(Purpose.class);
    private final int numberOfThreads = Runtime.getRuntime().availableProcessors();

    public TripDistribution(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
    }

    public static class tripDistributionData {
        public final Predicate<MitoPerson> predicate;
        public final AtomicInteger distributedTripCounter;
        public final AtomicDouble distributedTripDistance;
        public final AtomicInteger failedTripsCounter;
        public final AtomicInteger randomTripCounter;
        public final AtomicDouble randomTripDistance;
        private IndexedDoubleMatrix2D utilityMatrix;

        tripDistributionData(Predicate<MitoPerson> predicate) {
            this.predicate = predicate;
            this.distributedTripCounter = new AtomicInteger(0);
            this.distributedTripDistance = new AtomicDouble(0.);
            this.failedTripsCounter = new AtomicInteger(0);
            this.randomTripCounter = new AtomicInteger(0);
            this.randomTripDistance = new AtomicDouble(0.);
        }

        void setUtilityMatrix(IndexedDoubleMatrix2D utilityMatrix) {
            this.utilityMatrix = utilityMatrix;
        }

        public IndexedDoubleMatrix2D getUtilityMatrix() {
            return utilityMatrix;
        }

    }

    public void registerDestinationUtilityCalculator(Purpose purpose, TripDistributorType type, AbstractDestinationUtilityCalculator utilityCalculator) {
        final Tuple<AbstractDestinationUtilityCalculator, TripDistributorType> prev = tripDistributionCalculatorsByPurpose.put(purpose, new Tuple<>(utilityCalculator, type));
        if (prev != null) {
            logger.warn("Overwrote distribution calculators for purpose " + purpose + " with " + utilityCalculator.getClass() + " and " + type.toString());
        } else {
            logger.info("Registered distribution calculator for purpose " + purpose + " with " + utilityCalculator.getClass() + " and " + type.toString());
        }
        initialise(purpose);
    }

    public void registerDestinationUtilityCalculator(Purpose purpose, AbstractDestinationUtilityCalculator utilityCalculator) {
        registerDestinationUtilityCalculator(purpose, TripDistributorType.getDefault(purpose),utilityCalculator);
    }

    @Override
    public void run() {
        logger.info("Categorise persons for building person-based utility matrices...");
        categorisePersons(tripDistributionCalculatorsByPurpose.keySet());

        logger.info("Building destination choice utility matrices...");
        buildMatrices(tripDistributionCalculatorsByPurpose.keySet());

        logger.info("Distributing trips for households...");
        distributeTrips(purposes);
    }

    public void calibrate(Purpose purpose, double[] referenceMeans) {
        double[] adjustments;
        int iterations = 0;
        do {
            iterations++;
            if(iterations > 100) {
                logger.error("Calibration failed! \uD83D\uDE22");
            }
            initialise(purpose);
            buildMatrices(Collections.singletonList(purpose));
            distributeTrips(Collections.singletonList(purpose));
            adjustments = getAdjustments(purpose, referenceMeans);
            tripDistributionCalculatorsByPurpose.get(purpose).getFirst().adjustDistanceParams(adjustments, logger);
        } while (Arrays.stream(adjustments).map(b -> Math.abs(b-1.)).max().orElseThrow() > 0.02);
        logger.info("Calibration complete! \uD83C\uDF89");
    }

    private void categorisePersons(Collection<Purpose> purposes) {

        for(Purpose purpose : purposes) {
            List<Predicate<MitoPerson>> categories = tripDistributionCalculatorsByPurpose.get(purpose).getFirst().getCategories();
            double[] personsPerCategory = new double[categories.size()];
            Map<Integer,Integer> categorisedPersons = new HashMap<>();
            for (Map.Entry<Integer,MitoPerson> e : dataSet.getModelledPersons().entrySet()) {

                //TODO: check with Corin logic conflict here 1) no trip for purpose RRT, then it is possible that person mode set AutoPt, then results in error in no category found
                //TODO: but if skip when no trip for purpose, will cause error in find random origin function
                if (purpose.equals(RRT) & !e.getValue().hasTripsForPurpose(purpose)){
                    continue;
                }
                int ppId = e.getKey();
                for(int i = 0 ; i < categories.size() ; i++) {
                    if(categories.get(i).test(e.getValue())) {
                        personsPerCategory[i]++;
                        Integer prev = categorisedPersons.put(ppId,i);
                        if(prev != null) {
                            throw new RuntimeException("Person " + ppId + " in multiple trip distribution categories for purpose " + purpose);
                        }
                    }
                }
                if(!categorisedPersons.containsKey(ppId)) {
                    throw new RuntimeException("Person " + ppId + " in no trip distribution categories for purpose " + purpose);
                }
            }
            personCategories.put(purpose, categorisedPersons);
            logger.info("Organised " + dataSet.getModelledPersons().size() + " MitoPersons into " + categories.size() + " categories.\n" +
                    "Persons in each category: " + Arrays.toString(personsPerCategory));
        }

    }

    private void initialise(Purpose purpose) {
        tripDistributionDataByPurpose.put(purpose, tripDistributionCalculatorsByPurpose.get(purpose).getFirst().getCategories().stream().map(tripDistributionData::new).collect(Collectors.toList()));
    }

    private void buildMatrices(Collection<Purpose> purposes) {
        ConcurrentExecutor<Triple<Purpose, Integer, IndexedDoubleMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);
        List<Callable<Triple<Purpose,Integer, IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (Purpose purpose : purposes) {
            // Distribution of airport trips to the airport does not need a matrix of weights
            if (!purpose.equals(AIRPORT)){
                AbstractDestinationUtilityCalculator utilityCalculator = tripDistributionCalculatorsByPurpose.get(purpose).getFirst();
                for(int i = 0; i < utilityCalculator.getCategories().size() ; i++) {
                    utilityCalcTasks.add(new DestinationUtilityByPurposeGenerator(purpose, dataSet, utilityCalculator, i));
                }
            }
        }
        List<Triple<Purpose, Integer, IndexedDoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Triple<Purpose, Integer, IndexedDoubleMatrix2D> result: results) {
            Purpose purpose = result.getLeft();
            Integer index = result.getMiddle();
            IndexedDoubleMatrix2D utilityMatrix = result.getRight();
            tripDistributionDataByPurpose.get(purpose).get(index).setUtilityMatrix(utilityMatrix);
        }
    }

    private void distributeTrips(Collection<Purpose> purposes) {

        // Create partitions
        final Collection<MitoHousehold> households = dataSet.getModelledHouseholds().values();
        final int partitionSize = (int) ((double) households.size() / (numberOfThreads)) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(households, partitionSize);

        logger.info("Using " + numberOfThreads + " thread(s)" +
                " with partitions of size " + partitionSize);

        // Home-based trips
        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        List<Callable<Void>> otherTasks = new ArrayList<>();
        //for (final List<MitoHousehold> partition : partitions) {
            for (Purpose purpose : purposes) {
                if (Purpose.getHomeBasedPurposes().contains(purpose)) {
                    homeBasedTasks.add(getDistributor(purpose,households, tripDistributionCalculatorsByPurpose.get(purpose).getSecond()));
                } else {
                    otherTasks.add(getDistributor(purpose,households, tripDistributionCalculatorsByPurpose.get(purpose).getSecond()));
                }
            }
        //}

        // Run tasks in order
        ConcurrentExecutor<Void> executor;

        if(!homeBasedTasks.isEmpty()) {
            executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);
            executor.submitTasksAndWaitForCompletion(homeBasedTasks);
        }

        if(!otherTasks.isEmpty()) {
            executor = ConcurrentExecutor.fixedPoolService(numberOfThreads);
            executor.submitTasksAndWaitForCompletion(otherTasks);
        }

        // Print statistics
        distributionStatistics(purposes);
    }

    private void distributionStatistics(Collection<Purpose> purposes) {
        // Trip counts / failed / mean distances
        logger.info("Overall distribution statistics:");
        StringBuilder headerRow = new StringBuilder();
        headerRow.append(String.format("%-10s","Purpose"));
        headerRow.append(String.format("%-10s","Success"));
        headerRow.append(String.format("%-10s","Failed"));
        int maxCategories = tripDistributionDataByPurpose.values().stream().mapToInt(List::size).max().orElseThrow();
        for(int i = 0 ; i < maxCategories ; i++) {
            headerRow.append(String.format("%-10s","Index_" + i));
        }
        logger.info("          Counts:             Mean trip distances (kilometres):");
        logger.info(headerRow);

        StringBuilder row;
        for (Purpose purpose : purposes) {
            List<tripDistributionData> purposeData = tripDistributionDataByPurpose.get(purpose);
            row = new StringBuilder();
            row.append(String.format("%-10s",purpose));
            row.append(String.format("%-10d",purposeData.stream().mapToInt(d -> d.distributedTripCounter.get()).sum()));
            row.append(String.format("%-10d",purposeData.stream().mapToInt(d -> d.failedTripsCounter.get()).sum()));
            for(tripDistributionData categoryData : purposeData) {
                row.append(String.format("%-10.2f",categoryData.distributedTripDistance.get() / categoryData.distributedTripCounter.get()));
            }
            logger.info(row);
        }

        // Random trips
        logger.info("Trips with randomly selected destination (for home-based) or origin (for non-home based):");
        headerRow = new StringBuilder();
        headerRow.append(String.format("%-10s","Purpose"));
        headerRow.append(String.format("%-10s","Random"));
        for(int i = 0 ; i < maxCategories ; i++) {
            headerRow.append(String.format("%-10s","Index_" + i));
        }
        logger.info("                    Mean trip distances (metres - computed for home-based trips only):");
        logger.info(headerRow);

        for (Purpose purpose : purposes) {
            List<tripDistributionData> purposeData = tripDistributionDataByPurpose.get(purpose);
            row = new StringBuilder();
            row.append(String.format("%-10s",purpose));
            row.append(String.format("%-10d",purposeData.stream().mapToInt(d -> d.randomTripCounter.get()).sum()));
            for(tripDistributionData categoryData : purposeData) {
                row.append(String.format("%-10.2f",categoryData.randomTripDistance.get() / categoryData.randomTripCounter.get()));
            }
            logger.info(row);
        }
    }

    private AbstractDistributor getDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, TripDistributorType type) {
        switch (type) {
            case HomeBasedMandatory:
                return new MandatoryDistributor(purpose, householdCollection, dataSet, tripDistributionDataByPurpose, personCategories);
            case HomeBasedDiscretionary:
                return new DiscretionaryDistributor(purpose, householdCollection, dataSet, tripDistributionDataByPurpose, personCategories);
            case HomeBasedDiscretionaryWithTTB:
                return new DiscretionaryDistributorWithTTB(purpose, householdCollection, dataSet, tripDistributionDataByPurpose);
            case Airport:
                return new AirportDistributor(purpose, householdCollection, dataSet, tripDistributionDataByPurpose);
            case NonHomeBasedDiscretionary:
                return new NonHomeBasedDistributor(purpose, householdCollection, dataSet, tripDistributionDataByPurpose, personCategories);
            case NonHomeBasedDiscretionaryWithTTB:
                return new NonHomeBasedDistributorWithTTB(purpose, householdCollection, dataSet, tripDistributionDataByPurpose);
            case RecreationalRoundTrip:
                return new RecreationalRoundTripDistributor(purpose, householdCollection, dataSet, tripDistributionDataByPurpose, personCategories);
            default:
                throw new RuntimeException("Distributor type " + type + " not recognised!");
        }
    }

    private double[] getAdjustments(Purpose purpose, double[] referenceMeans) {

        List<tripDistributionData> calibrationData = tripDistributionDataByPurpose.get(purpose);
        int categories = calibrationData.size();

        double[] adjustments = new double[categories];
        if(purpose.equals(HBW) || purpose.equals(HBE)){
            for(int i = 0 ; i < categories ; i++) {
                double mean = calibrationData.get(i).randomTripDistance.get() / calibrationData.get(i).randomTripCounter.get();
                adjustments[i] = Math.max(0.5, Math.min(2, mean / referenceMeans[i]));
                logger.info("Index: "+ i + " Mean: " + mean + " Ref: " + referenceMeans[i] + " Adjustment: " + adjustments[i]);
            }
        }else{
            for(int i = 0 ; i < categories ; i++) {
                double mean = calibrationData.get(i).distributedTripDistance.get() / calibrationData.get(i).distributedTripCounter.get();
                adjustments[i] = Math.max(0.5, Math.min(2, mean / referenceMeans[i]));
                logger.info("Index: "+ i + " Mean: " + mean + " Ref: " + referenceMeans[i] + " Adjustment: " + adjustments[i]);
            }
        }

        return adjustments;

    }
}
