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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;


public final class TripDistributionAggregate extends Module {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);
    public final static AtomicInteger completelyRandomNhbTrips = new AtomicInteger(0);

    private Map<Purpose, Double> observedAverageDistances = new HashMap<>();

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
        observedAverageDistances.put(Purpose.HBE , 7.29);
        observedAverageDistances.put(Purpose.HBW , 18.1);
        observedAverageDistances.put(Purpose.HBO , 10.4);
        observedAverageDistances.put(Purpose.HBS , 5.07);
        observedAverageDistances.put(Purpose.HBR , 10.4);
        observedAverageDistances.put(Purpose.NHBO , 11.7);
        observedAverageDistances.put(Purpose.NHBW , 16.1);
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
        printForDebug();
        summarizeTripLength();
    }

    private void summarizeTripLength() {
        Path fileTripGen = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_DestChoice_summary.csv");
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(fileTripGen.toAbsolutePath().toString(), true);

        Path fileTripGen1 = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_DestChoice_zonalSummary.csv");
        PrintWriter pwZne = MitoUtil.openFileForSequentialWriting(fileTripGen1.toAbsolutePath().toString(), true);

        if (purpose.equals(Purpose.HBW) && persona.getId() == 1) {
            pw.println("persona,purpose,trips,totalLength,averageTripLength,observedAvTripLength");
            pwZne.print("persona,purpose");
            for (MitoZone zone : dataSet.getZones().values()){
                pwZne.print(",");
                pwZne.print(Integer.toString(zone.getId()));
            }
            pwZne.println();
        }
        pw.print(persona.getId());
        pwZne.print(persona.getId());
        pw.print(",");
        pwZne.print(",");
        pw.print(purpose);
        pwZne.print(purpose);
        pw.print(",");
        double totalTripLength = 0.;
        double totalTrips = 0;
        //IndexedDoubleMatrix1D totalDistanceByOrigin = calculateTotalLength().getSecond();
        //IndexedDoubleMatrix2D tripMatrix = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
        for (MitoZone zone : dataSet.getZones().values()){
            double zonalTripLength = 0.;
            double zonalTrips = 0;
            //IndexedDoubleMatrix1D trips = new IndexedDoubleMatrix1D(dataSet.getZones().values());
            //int inde1x = trips.getIdForInternalIndex(zone.getId());
            //double[] tripsByModeOrigin = tripMatrix.viewRow(inde1x).toNonIndexedArray();
            //int dd = 0;
            for (MitoZone dest : dataSet.getZones().values()){
                zonalTripLength += dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(zone.getId(), dest.getId()) *
                        dataSet.getTravelDistancesAuto().getTravelDistance(zone.getId(), dest.getId());
                zonalTrips += dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(zone.getId(), dest.getId());
                //dd++;
            }
            pwZne.print(",");
            pwZne.print(zonalTrips);
            pwZne.print(",");
            pwZne.print(zonalTripLength);
            pwZne.print(",");
            if (zonalTrips > 0) {
                pwZne.print(zonalTripLength / zonalTrips);
            } else {
                pwZne.print(0);
            }
            totalTripLength += zonalTripLength;
            totalTrips +=zonalTrips;
            /*totalTripsByZone.assign(0.);
            pwZne.print(",");
            pwZne.print(totalDistanceByOrigin.getIndexed(zone.getId()));
            totalTripLength += totalDistanceByOrigin.getIndexed(zone.getId());*/
        }
        pw.print(totalTrips);
        pw.print(",");
        pw.print(totalTripLength);
        pw.print(",");
        if (totalTrips > 0) {
            pw.print(totalTripLength / totalTrips);
        } else {
            pw.print(0);
        }
        pw.print(",");
        pw.println(observedAverageDistances.get(purpose));
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
                if (dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId()) != Double.NaN) {
                   if (dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId()) != Double.POSITIVE_INFINITY) {
                       totalLength0 += dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(origin.getId(), destination.getId()) *
                               dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId());
                   }
                }
            }

            // todo. debug here the total length, checking if it needs to be the indexed origin or which is the value that we are getting here
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

        pwh.print("origin,");
        pwtrips.print("origin,");
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
            pwh.print(origin.getId());
            pwh.print(",");
            pwtrips.print(origin.getId());
            pwtrips.print(",");
            for (MitoZone destination : dataSet.getZones().values()) {
                pwh.print(dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(origin.getId(), destination.getId()));
                pwh.print(",");
                pwtrips.print(probabilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId()));
                pwtrips.print(",");
            }
            pwh.println();
            pwtrips.println();
            /*int originIndex = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIdForInternalRowIndex(origin.getId());
            pwh.print(originIndex);
            pwh.print(",");
            pwtrips.print(originIndex);
            pwtrips.print(",");
            for (MitoZone destination : dataSet.getZones().values()) {
                int destinationIndex = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIdForInternalColumnIndex(destination.getId());
                pwh.print(dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi).getIndexed(originIndex, destinationIndex));
                pwh.print(",");
                pwtrips.print(probabilityMatrices.get(origin.getId()).getIndexed(originIndex, destinationIndex));
                pwtrips.print(",");
            }
            pwh.println();
            pwtrips.println();*/
        }
        pwh.close();
        pwtrips.close();
    }

    private void printForDebug(){
        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/1_destinationChoice_nmtDistance.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);

        pwh.println("origin,destination,distanceNMT,distanceAuto");

        // print only four selected zones by area type
        for (MitoZone origin : dataSet.getZonesByAreaType().values()) {
            for (MitoZone destination : dataSet.getZones().values()) {
                pwh.print(origin.getId());
                pwh.print(",");
                pwh.print(destination.getId());
                pwh.print(",");
                pwh.print(dataSet.getTravelDistancesAuto().getTravelDistance(origin.getId(), destination.getId()));
                pwh.print(",");
                pwh.println(dataSet.getTravelDistancesNMT().getTravelDistance(origin.getId(), destination.getId()));
            }
        }
        pwh.close();
    }

    private void buildMatrices() {
        //IndexedDoubleMatrix2D
        List<Callable<Tuple<Integer,IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (MitoZone zone : dataSet.getZones().values()) {
            utilityCalcTasks.add(new DestinationUtilityByPurposeGeneratorAggregate(zone, purpose, dataSet,
                    destinationUtilityCalculatorFactory,
                    travelDistanceCalibrationParameters.get(purpose),
                    impedanceCalibrationParameters.get(purpose),persona));

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
