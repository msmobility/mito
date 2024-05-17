package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
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

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;

public class ModeChoiceAggregate extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoiceAggregate.class);

    private final Map<Purpose, ModeChoiceCalculatorAggregate> modeChoiceCalculatorByPurpose = new EnumMap<>(Purpose.class);

    private MitoAggregatePersona persona;


    List<Mode> modesModeChoice = Arrays.asList(Mode.autoDriver, Mode.autoPassenger, Mode.train, Mode.tramOrMetro,
            Mode.bus, Mode.bicycle, Mode.walk, Mode.taxi);

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
/*        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (MitoZone origin : dataSet.getZones().values()) {
            executor.addTaskToQueue(new modeChoiceByPurposeAggregate(persona, origin, purposes.get(0), dataSet,
                    modeChoiceCalculatorByPurpose.get(purposes.get(0))));
        }
        executor.execute();*/

        /*new modeChoiceByPurposeAggregate(persona, dataSet.getZones().get(3359), purposes.get(0), dataSet,
                    modeChoiceCalculatorByPurpose.get(purposes.get(0))).call();*/

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (MitoZone origin : dataSet.getZones().values()) {
            executor.addTaskToQueue(new modeChoiceByPurposeAggregate(persona, origin, purposes.get(0), dataSet,
                    modeChoiceCalculatorByPurpose.get(purposes.get(0))));
        }
        executor.execute();

        logger.info("Mode choice. Finished purpose " + purposes.get(0));
    }

    private void initializeMatrices() {

        ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
        for (Mode mode : modesModeChoice){
            IndexedDoubleMatrix2D aggregateTripsTaxi = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
            tripMatrix.put(mode, aggregateTripsTaxi);
        }
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

        for (Mode mode : modesModeChoice){
            IndexedDoubleMatrix2D tripMatrix = dataSet.getAggregateTripMatrix().get(mode);
            IndexedDoubleMatrix1D trips = new IndexedDoubleMatrix1D(dataSet.getZones().values());

            for (MitoZone origin : dataSet.getZonesByAreaType().values()){

                //double tripsByModeOrigin = Arrays.stream(tripMatrix.viewRow(origin.getId()).toNonIndexedArray()).sum();
                //logger.info("Trips by mode " + mode.toString() + ": " + tripMatrix.getIndexed(origin.getId(), 1) + ". total trips: " + tripsByModeOrigin);

                int inde1x = trips.getIdForInternalIndex(origin.getId());
                double tripsByModeOrigin = Arrays.stream(tripMatrix.viewRow(inde1x).toNonIndexedArray()).sum();
                //logger.info("Trips by mode " + mode.toString() + ": " + tripMatrix.getIndexed(inde1x, 1) + ". total trips: " + tripsByModeOrigin);
                trips.setIndexed(inde1x, tripsByModeOrigin);
                totalTripsByZone.setIndexed(inde1x, totalTripsByZone.getIndexed(inde1x)+ tripsByModeOrigin);
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
            for (Mode mode : modesModeChoice) {
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

        private Map<Mode, IndexedDoubleMatrix1D> probabilitiesModeChoice = new LinkedHashMap<>();

        private Map<Mode, Map<String, Double>> coef;

        List<Mode> modesModeChoice = Arrays.asList(Mode.autoDriver, Mode.autoPassenger, Mode.train, Mode.tramOrMetro,
                Mode.bus, Mode.bicycle, Mode.walk, Mode.taxi);

        modeChoiceByPurposeAggregate(MitoAggregatePersona persona, MitoZone zone, Purpose purpose, DataSet dataSet, ModeChoiceCalculatorAggregate modeChoiceCalculator) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.modeChoiceCalculator = modeChoiceCalculator;
            this.origin = zone;
            this.persona = persona;
            coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            try {
                for (Mode mode : modesModeChoice){
                    IndexedDoubleMatrix1D matrix = new IndexedDoubleMatrix1D(dataSet.getZones().values());
                    probabilitiesModeChoice.put(mode, matrix);
                }

                //MitoZone destination = dataSet.getZones().get(1);
                for (MitoZone destination : dataSet.getZones().values()) {
                    EnumMap<Mode, Double> probabilities = calculateUtilities(calculateTripProbabilities(destination));
                    for (Mode mode: probabilities.keySet()){
                        //double prevTripsZone = dataSet.getAggregateTripMatrix().get(mode).getIndexed(origin.getId(), destination.getId());
                        double trips = probabilities.get(mode);
                        //logger.info("Trips before assigning matrix " + mode.toString() + ": " + trips);
                        probabilitiesModeChoice.get(mode).setIndexed(destination.getZoneId(), trips);

                    }
                }

                for (Mode mode : modesModeChoice){
                    //int index = probabilitiesModeChoice.get(mode).getIdForInternalIndex(origin.getId());
                    dataSet.getAggregateTripMatrix().get(mode).assign(probabilitiesModeChoice.get(mode).toNonIndexedArray(), origin.getId());
                    //logger.info("Trips after setting matrix " + mode + ": " + dataSet.getAggregateTripMatrix().get(mode).getIndexed(index, destination.getId()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //logger.info(countTripsSkipped + " trips skipped for " + purpose);
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
                    travelDistanceNMT, dataSet.getPeakHour(), coef);
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
