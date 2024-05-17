package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


public final class TripDistributionAggregate extends Module {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);
    public final static AtomicInteger completelyRandomNhbTrips = new AtomicInteger(0);

    //todo turn to static to be mantained for both mandatory and discretionary - we expect to remove ttb from the trip distribution
    private static Map<Integer, IndexedDoubleMatrix2D> probabilityMatrices = new LinkedHashMap<>();

    private static Map<Integer, IndexedDoubleMatrix2D> tripMatrices = new LinkedHashMap<>();

    private final static Logger logger = Logger.getLogger(TripDistributionAggregate.class);

    private final Map<Purpose, Double> travelDistanceCalibrationParameters;
    private final Map<Purpose, Double> impedanceCalibrationParameters;
    private final boolean useBudgetsInDestinationChoice;

    private final Purpose purpose;

    private MitoAggregatePersona persona;

    private final DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory;

    public TripDistributionAggregate(DataSet dataSet, List<Purpose> purposes, Map<Purpose, Double> travelDistanceCalibrationParameters,
                                     Map<Purpose, Double> impedanceCalibrationParameters, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory,
                                     MitoAggregatePersona persona) {
        super(dataSet, purposes);
        this.travelDistanceCalibrationParameters = travelDistanceCalibrationParameters;
        this.impedanceCalibrationParameters = impedanceCalibrationParameters;
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
        this.purpose = purposes.get(0);
        this.persona = persona;
    }

    public TripDistributionAggregate(DataSet dataSet, List<Purpose> purposes, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory,
                                     MitoAggregatePersona persona) {
        super(dataSet, purposes);
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
        travelDistanceCalibrationParameters = new HashMap<>();
        impedanceCalibrationParameters = new HashMap<>();
        for (Purpose purpose : purposes){
            travelDistanceCalibrationParameters.put(purpose, 1.0);
            impedanceCalibrationParameters.put(purpose, 1.0);
        }
        purpose = purposes.get(0);
        this.persona = persona;
    }

    @Override
    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();
        if (purpose.equals(Purpose.NHBO) || purpose.equals(Purpose.NHBW)) {
            distributeNonHomeBasedTrips();
        } else {
            distributeHomeBasedTrips();
        }
        printMatrices();
        summarizeTripLength();
    }

    private void summarizeTripLength() {
        Path fileTripGen = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_DestChoice_summary.csv");
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(fileTripGen.toAbsolutePath().toString(), true);

        Path fileTripGen1 = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_DestChoice_zonalSummary.csv");
        PrintWriter pwZne = MitoUtil.openFileForSequentialWriting(fileTripGen1.toAbsolutePath().toString(), true);

        if (purpose.equals(Purpose.HBW)) {
            pw.println("persona,purpose,totalLength,averageTripLength");
            pwZne.println("persona,purpose,zone,totalLength,averageTripLength");
        }
        pw.print(persona.getId());
        pwZne.print(persona.getId());
        pw.print(",");
        pwZne.print(",");
        pw.print(purpose);
        pwZne.print(purpose);
        pw.print(",");
        double totalTripLength = calculateTotalLength().getFirst();
        pw.print(totalTripLength);
        pw.print(",");
        pw.print(totalTripLength / dataSet.getTotalTripsByPurpose().get(purpose));
        IndexedDoubleMatrix1D totalDistanceByOrigin = calculateTotalLength().getSecond();
        for (MitoZone zone : dataSet.getZones().values()){
            pwZne.print(",");
            pwZne.print(totalDistanceByOrigin.getIndexed(zone.getId()));
            pwZne.print(",");
            pwZne.print(totalDistanceByOrigin.getIndexed(zone.getId())/ dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(zone.getId(), 1));
        }
        pw.println();
        pwZne.println();
        pw.close();
        pwZne.close();

    }

    private Tuple< Double, IndexedDoubleMatrix1D> calculateTotalLength() {
        IndexedDoubleMatrix1D totalDistanceByOrigin = new IndexedDoubleMatrix1D(dataSet.getZones().values());
        double totalLength = 0.;
        for (MitoZone origin : dataSet.getZones().values()){
            double totalLength0 = 0.;
            for (MitoZone destination : dataSet.getZones().values()){
                totalLength0 += dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(origin.getId(), destination.getId()) *
                        dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId());
            }
            totalDistanceByOrigin.setIndexed(origin.getId(), totalLength0);
            totalLength += totalLength;
        }

        return new Tuple<>(totalLength, totalDistanceByOrigin);
    }

    private void distributeNonHomeBasedTrips() {
        //distribute non-home based trips
        // TODO: 5/15/2024 set different origins to NHBW trips (it is assuming that all trips start at work
        //to do that, we have the same method but with only 50 % of the trips. The other 50% are calculated transposing the matrix. Then, both matrices are added up
        if (purposes.get(0).equals(Purpose.NHBW)){
            //distribute origins according to the HBW destination trips. Destination is based on the utility calculated
            double totalWorkers = Arrays.stream(dataSet.getHBWtripsAttracted().toNonIndexedArray()).sum();
            IndexedDoubleMatrix2D tripMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
            for (MitoZone origin : dataSet.getZones().values()){
                double tripsOriginated = dataSet.getTotalTripsByPurpose().get(Purpose.NHBW) *
                        dataSet.getHBWtripsAttracted().getIndexed(origin.getId()) / totalWorkers;
                for (MitoZone destination : dataSet.getZones().values()) {
                    double tripsDistributed = tripsOriginated * probabilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId());
                    tripMatrix.setIndexed(origin.getId(), destination.getId(), tripsDistributed);
                }
            }

        } else if (purposes.get(0).equals(Purpose.NHBO)){
            //distribute origins according to all destination trips
            double totalTripsOtherPurposes = Arrays.stream(dataSet.getHomeBasedTripsAttractedToZone().toNonIndexedArray()).sum();
            IndexedDoubleMatrix2D tripMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
            for (MitoZone origin : dataSet.getZones().values()){
                double tripsOriginated = dataSet.getTotalTripsByPurpose().get(Purpose.NHBO) *
                                dataSet.getHomeBasedTripsAttractedToZone().getIndexed(origin.getId()) / totalTripsOtherPurposes;
                for (MitoZone destination : dataSet.getZones().values()) {
                    double tripsDistributed = tripsOriginated * probabilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId());
                    tripMatrix.setIndexed(origin.getId(), destination.getId(), tripsDistributed);
                }
            }
        }
    }

    private void printMatrices() {

        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_destinationChoice_"+ purposes.get(0) +"_trips.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);

        Path filePersona1 = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_destinationChoice_"+ purposes.get(0) +"_results.csv");
        PrintWriter pwtrips = MitoUtil.openFileForSequentialWriting(filePersona1.toAbsolutePath().toString(), false);

        for (MitoZone origin : dataSet.getZones().values()){
            pwh.print(origin.getId());
            pwh.print(",");
            pwtrips.print(origin.getId());
            pwtrips.print(",");
        }
        pwh.println();
        pwtrips.println();

        // print only four selected zones by area type
        for (MitoZone origin : dataSet.getZonesByAreaType().values()) {
            for (MitoZone destination : dataSet.getZones().values()) {
                pwh.print(dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(origin.getId(), destination.getId()));
                pwh.print(",");
                pwtrips.print(probabilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId()));
                pwtrips.print(",");
            }
            pwh.println();
            pwtrips.println();
        }
        pwh.close();
        pwtrips.close();
    }

    private void buildMatrices() {
        //IndexedDoubleMatrix2D
        List<Callable<Tuple<Integer,IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (MitoZone zone : dataSet.getZones().values()) {
            utilityCalcTasks.add(new DestinationUtilityByPurposeGeneratorAggregate(zone, purpose, dataSet,
                    destinationUtilityCalculatorFactory,
                    travelDistanceCalibrationParameters.get(purpose),
                    impedanceCalibrationParameters.get(purpose)));

        }
        ConcurrentExecutor<Tuple<Integer, IndexedDoubleMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(dataSet.getZones().size());
        List<Tuple<Integer,IndexedDoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Tuple<Integer, IndexedDoubleMatrix2D> result: results) {
            probabilityMatrices.put(result.getFirst(), result.getSecond());
        }
        logger.info("Destination choice. Finished purpose " + purpose);

    }

    private void distributeHomeBasedTrips() {
        IndexedDoubleMatrix2D utilityMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        IndexedDoubleMatrix1D tripsAttractedPurpose = new IndexedDoubleMatrix1D(dataSet.getZones().values());
        tripsAttractedPurpose.assign(0);
        for (MitoZone origin : dataSet.getZones().values()){
            IndexedDoubleMatrix1D tripGeneration = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).viewRow(origin.getId());
            double originalTrips = tripGeneration.getIndexed(origin.getId());
            for(MitoZone destination : dataSet.getZones().values()) {
                double trips = originalTrips * probabilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId());
                utilityMatrix.setIndexed(origin.getId(), destination.getId(),trips);
                tripsAttractedPurpose.setIndexed(destination.getId(), tripsAttractedPurpose.getIndexed(destination.getId())+trips);
            }
        }
        dataSet.getAggregateTripMatrix().put(Mode.pooledTaxi, utilityMatrix);

        if (purpose.equals(Purpose.HBW)) {
            IndexedDoubleMatrix1D tripsAttracted = dataSet.getHBWtripsAttracted();
            for (MitoZone destination : dataSet.getZones().values()) {
                double value = tripsAttracted.getIndexed(destination.getId()) + tripsAttractedPurpose.getIndexed(destination.getId());
                tripsAttracted.setIndexed(destination.getId(), value);
            }
            dataSet.setHBWtripsAttracted(tripsAttracted);
        } else {
            // for HBS, HBR, HBO
            IndexedDoubleMatrix1D tripsAttracted = dataSet.getHomeBasedTripsAttractedToZone();
            for (MitoZone destination : dataSet.getZones().values()) {
                double value = tripsAttracted.getIndexed(destination.getId()) + tripsAttractedPurpose.getIndexed(destination.getId());
                tripsAttracted.setIndexed(destination.getId(), value);
            }
            dataSet.setHomeBasedTripsAttractedToZone(tripsAttracted);
        }
    }

}
