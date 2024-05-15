package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;

public class ModeChoiceAggregate extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoiceAggregate.class);

    private final Map<Purpose, ModeChoiceCalculatorAggregate> modeChoiceCalculatorByPurpose = new EnumMap<>(Purpose.class);

    private MitoAggregatePersona persona;

    private IndexedDoubleMatrix2D aggregateTripsAutoD;
    private IndexedDoubleMatrix2D aggregateTripsAutoP;
    private IndexedDoubleMatrix2D aggregateTripsBus;
    private IndexedDoubleMatrix2D aggregateTripsTrain;
    private IndexedDoubleMatrix2D aggregateTripsTramMetro;
    private IndexedDoubleMatrix2D aggregateTripsBicycle;
    private IndexedDoubleMatrix2D aggregateTripsWalk;
    private IndexedDoubleMatrix2D aggregateTripsTaxi;

    public ModeChoiceAggregate(DataSet dataSet, List<Purpose> purposes, MitoAggregatePersona persona) {
        super(dataSet, purposes);
        boolean includeAV = Resources.instance.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);
        this.persona = persona;
        //AV option is deactivated for now, since it uses outdate mode choice calculators.

//        modeChoiceCalculatorByPurpose.put(Purpose.HBW, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.HBW, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.HBE, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.HBE, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.HBS, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.HBS, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.HBO, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.HBO, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.HBR, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.HBR, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.NHBW, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.NHBW, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.NHBO, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(Purpose.NHBO, dataSet), dataSet.getModeChoiceCalibrationData()));
//        modeChoiceCalculatorByPurpose.put(Purpose.AIRPORT, new CalibratingModeChoiceCalculatorImpl(new AirportModeChoiceCalculator(), dataSet.getModeChoiceCalibrationData()));
//        logger.info("Using the mode choice calculators obtained from MID 2017. Register alternative mode choice calculators is desired.");

    }

    public void registerModeChoiceCalculatorAggregate(Purpose purpose, ModeChoiceCalculatorAggregate modeChoiceCalculator) {
        final ModeChoiceCalculatorAggregate prev = modeChoiceCalculatorByPurpose.put(purpose, modeChoiceCalculator);
        if (prev != null) {
            logger.info("Overwrote mode choice calculator for purpose " + purpose + " with " + modeChoiceCalculator.getClass());
        }
    }

    @Override
    public void run() {
        if (modeChoiceCalculatorByPurpose.isEmpty()){
            throw new RuntimeException("It is mandatory to define mode choice calculators. Look at TravelDemandGeneratorXXX.java");
        }
        logger.info(" Calculating mode choice probabilities for each trip");
        initializeMatrices();
        modeChoiceByPurpose();
        printModeShares();
    }

    private void modeChoiceByPurpose() {
        initializeMatrices();
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (MitoZone origin : dataSet.getZones().values()) {
            executor.addTaskToQueue(new modeChoiceByPurposeAggregate(persona, origin, purposes.get(0), dataSet,
                    modeChoiceCalculatorByPurpose.get(purposes.get(0))));
        }
        executor.execute();

        logger.info("Mode choice. Finished purpose " + purposes.get(0));
    }

    private void initializeMatrices() {
        final IndexedDoubleMatrix2D destinationChoiceTrips = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
        aggregateTripsAutoD = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsAutoD = destinationChoiceTrips.copy();
        aggregateTripsAutoP = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsAutoP = destinationChoiceTrips.copy();
        aggregateTripsBus = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsBus = destinationChoiceTrips.copy();
        aggregateTripsTrain = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsTrain = destinationChoiceTrips.copy();
        aggregateTripsTramMetro = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsTramMetro = destinationChoiceTrips.copy();
        aggregateTripsBicycle = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsBicycle = destinationChoiceTrips.copy();
        aggregateTripsWalk = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsWalk = destinationChoiceTrips.copy();
        aggregateTripsTaxi = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        aggregateTripsTaxi = destinationChoiceTrips.copy();
        ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
        tripMatrix.put(Mode.autoDriver, aggregateTripsAutoD);
        tripMatrix.put(Mode.autoPassenger, aggregateTripsAutoP);
        tripMatrix.put(Mode.bus, aggregateTripsBus);
        tripMatrix.put(Mode.train, aggregateTripsTrain);
        tripMatrix.put(Mode.tramOrMetro, aggregateTripsTramMetro);
        tripMatrix.put(Mode.bicycle, aggregateTripsBicycle);
        tripMatrix.put(Mode.walk, aggregateTripsWalk);
        tripMatrix.put(Mode.taxi, aggregateTripsTaxi);
        tripMatrix.put(Mode.pooledTaxi, dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi));
        dataSet.setAggregateTripMatrix(tripMatrix);
    }

    private void printModeShares() {

        ConcurrentMap<Mode, IndexedDoubleMatrix1D> tripMatrixByMode = new ConcurrentHashMap<>();
        IndexedDoubleMatrix1D totalTripsByZone = new IndexedDoubleMatrix1D(dataSet.getZones().values());
        totalTripsByZone.assign(0.);

        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_modeChoice_"+ purposes.get(0) +"_trips.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);
        pwh.println("origin,mode,trips");

        double totalTrips = 0.;

        for (Mode mode : Mode.values()){
            IndexedDoubleMatrix2D tripMatrix = dataSet.getAggregateTripMatrix().get(mode);
            IndexedDoubleMatrix1D trips = new IndexedDoubleMatrix1D(dataSet.getZones().values());
            for (MitoZone origin : dataSet.getZones().values()){
                double tripsByModeOrigin = Arrays.stream(tripMatrix.viewRow(origin.getId()).toNonIndexedArray()).sum();
                trips.setIndexed(origin.getId(), tripsByModeOrigin);
                totalTripsByZone.setIndexed(origin.getId(), totalTripsByZone.getIndexed(origin.getId())+ tripsByModeOrigin);
                pwh.print(origin.getId());
                pwh.print(",");
                pwh.print(mode.toString());
                pwh.print(",");
                pwh.println(tripsByModeOrigin);
            }
            tripMatrixByMode.putIfAbsent(mode, trips);
            totalTrips = totalTrips + Arrays.stream(trips.toNonIndexedArray()).sum();
        }


        //for (Purpose purpose : Purpose.values()) {
            logger.info("#################################################");
            logger.info("Mode shares for purpose " + purposes.get(0) + ":");
            for (Mode mode : Mode.values()) {
                Double share  = Arrays.stream(tripMatrixByMode.get(mode).toNonIndexedArray()).sum() / totalTrips;
                if (share != null) {
                    logger.info(mode + " = " + share * 100 + "%");
                }
            }
        //}
    }

    static class modeChoiceByPurposeAggregate extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final TravelTimes travelTimes;
        private final ModeChoiceCalculatorAggregate modeChoiceCalculator;
        private int countTripsSkipped;

        private MitoZone origin;

        private MitoAggregatePersona persona;

        private IndexedDoubleMatrix2D aggregateTripsAutoD;
        private IndexedDoubleMatrix2D aggregateTripsAutoP;
        private IndexedDoubleMatrix2D aggregateTripsBus;
        private IndexedDoubleMatrix2D aggregateTripsTrain;
        private IndexedDoubleMatrix2D aggregateTripsTramMetro;
        private IndexedDoubleMatrix2D aggregateTripsBicycle;
        private IndexedDoubleMatrix2D aggregateTripsWalk;
        private IndexedDoubleMatrix2D aggregateTripsTaxi;

        modeChoiceByPurposeAggregate(MitoAggregatePersona persona,MitoZone zone, Purpose purpose, DataSet dataSet, ModeChoiceCalculatorAggregate modeChoiceCalculator) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.modeChoiceCalculator = modeChoiceCalculator;
            this.origin = zone;
            this.persona = persona;
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            try {
                //all matrices have the total trips by purpose, for that OD pair
                aggregateTripsAutoD = dataSet.getAggregateTripMatrix().get(Mode.autoDriver);
                aggregateTripsAutoP = dataSet.getAggregateTripMatrix().get(Mode.autoPassenger);
                aggregateTripsBus = dataSet.getAggregateTripMatrix().get(Mode.bus);
                aggregateTripsTrain = dataSet.getAggregateTripMatrix().get(Mode.train);
                aggregateTripsTramMetro = dataSet.getAggregateTripMatrix().get(Mode.tramOrMetro);
                aggregateTripsBicycle = dataSet.getAggregateTripMatrix().get(Mode.bicycle);
                aggregateTripsWalk = dataSet.getAggregateTripMatrix().get(Mode.walk);

                for (MitoZone destination : dataSet.getZones().values()) {
                    EnumMap<Mode, Double> probabilities = calculateUtilities(calculateTripProbabilities(destination));
                    for (Mode mode: probabilities.keySet()){
                        double prevTripsZone = dataSet.getAggregateTripMatrix().get(mode).getIndexed(origin.getId(), destination.getId());
                        double trips = prevTripsZone * probabilities.get(mode);
                        switch(mode) {
                            case autoDriver:
                                aggregateTripsAutoD.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case autoPassenger:
                                aggregateTripsAutoP.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case bus:
                                aggregateTripsBus.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case train:
                                aggregateTripsTrain.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case tramOrMetro:
                                aggregateTripsTramMetro.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case bicycle:
                                aggregateTripsBicycle.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case walk:
                                aggregateTripsWalk.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case taxi:
                                aggregateTripsTaxi.setIndexed(origin.getId(), destination.getId(), trips);
                                break;
                            case privateAV:
                                break;
                            case sharedAV:
                                break;
                            case pooledTaxi:
                                break;
                            default:
                                aggregateTripsAutoP.setIndexed(origin.getId(), destination.getId(), trips);
                        }
                        // print only four selected zones by area type
                        // TODO: 5/15/2024  print mode share of a given origin destination pair
                    }
                }
                ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
                tripMatrix.put(Mode.autoDriver, aggregateTripsAutoD);
                tripMatrix.put(Mode.autoPassenger, aggregateTripsAutoP);
                tripMatrix.put(Mode.bus, aggregateTripsBus);
                tripMatrix.put(Mode.train, aggregateTripsTrain);
                tripMatrix.put(Mode.tramOrMetro, aggregateTripsTramMetro);
                tripMatrix.put(Mode.bicycle, aggregateTripsBicycle);
                tripMatrix.put(Mode.walk, aggregateTripsWalk);
                tripMatrix.put(Mode.taxi, aggregateTripsTaxi);
                tripMatrix.put(Mode.pooledTaxi, dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi));
                dataSet.setAggregateTripMatrix(tripMatrix);
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info(countTripsSkipped + " trips skipped for " + purpose);
            return null;
        }

        private EnumMap<Mode, Double> calculateTripProbabilities(MitoZone destination) {

            final int originId = origin.getZoneId();

            final int destinationId = destination.getZoneId();
            final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originId,
                    destinationId);
            final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originId,
                    destinationId);
            return modeChoiceCalculator.calculateProbabilities(purpose, persona, origin, destination, travelTimes, travelDistanceAuto,
                    travelDistanceNMT, dataSet.getPeakHour());
        }

        private EnumMap<Mode, Double>  calculateUtilities(EnumMap<Mode, Double> probabilities) {
            if (probabilities == null) {
                countTripsSkipped++;
                return null;
            }
            return probabilities;
        }
    }
}
