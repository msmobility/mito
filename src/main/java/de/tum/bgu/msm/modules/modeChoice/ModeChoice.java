package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.calculators.AirportModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingAirportModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.av.AVModeChoiceCalculatorImpl;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;

public class ModeChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);

    private final Map<Purpose, ModeChoiceCalculator> modeChoiceCalculatorByPurpose = new EnumMap<>(Purpose.class);

    public ModeChoice(DataSet dataSet) {
        super(dataSet);
        boolean includeAV = Resources.instance.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);

        if(!includeAV) {
            modeChoiceCalculatorByPurpose.put(Purpose.HBW, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.HBE, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.HBS, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.HBO, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.NHBW,new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.NHBO, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
            modeChoiceCalculatorByPurpose.put(Purpose.AIRPORT, new CalibratingAirportModeChoiceCalculatorImpl(new AirportModeChoiceCalculator(), dataSet.getModeChoiceCalibrationData()));
        } else {
            modeChoiceCalculatorByPurpose.put(Purpose.HBW, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.HBE, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.HBS, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.HBO, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.NHBW, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.NHBO, new AVModeChoiceCalculatorImpl());
            modeChoiceCalculatorByPurpose.put(Purpose.AIRPORT, new AirportModeChoiceCalculator());
        }
    }

    public void registerModeChoiceCalculator(Purpose purpose, ModeChoiceCalculator modeChoiceCalculator) {
        final ModeChoiceCalculator prev = modeChoiceCalculatorByPurpose.put(purpose, modeChoiceCalculator);
        if(prev != null) {
            logger.info("Overwrote mode choice calculator for purpose " + purpose + " with " + modeChoiceCalculator.getClass());
        }
    }

    @Override
    public void run() {
        logger.info(" Calculating mode choice probabilities for each trip. Modes considered - 1. Auto driver, 2. Auto passenger, 3. Bicycle, 4. Bus, 5. Train, 6. Tram or Metro, 7. Walk ");
        modeChoiceByPurpose();
        printModeShares();
    }

    private void modeChoiceByPurpose() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new ModeChoiceByPurpose(purpose, dataSet, modeChoiceCalculatorByPurpose.get(purpose)));
        }
        executor.execute();
    }

    private void printModeShares() {

        //filter valid trips by purpose
        Map<Purpose, List<MitoTrip>> tripsByPurpose = dataSet.getTrips().values().stream()
                .filter(trip -> trip.getTripMode() != null)
                .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));


        tripsByPurpose.forEach((purpose, trips) -> {
            final long totalTrips = trips.size();
            trips.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((mode, count) ->
                            dataSet.addModeShareForPurpose(purpose, mode, (double) count / totalTrips));
        });

        for (Purpose purpose : Purpose.values()) {
            logger.info("#################################################");
            logger.info("Mode shares for purpose " + purpose + ":");
            for (Mode mode : Mode.values()) {
                Double share = dataSet.getModeShareForPurpose(purpose, mode);
                if (share != null) {
                    logger.info(mode + " = " + share * 100 + "%");
                }
            }
        }
    }

    static class ModeChoiceByPurpose extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final TravelTimes travelTimes;
        private final ModeChoiceCalculator modeChoiceCalculator;
        private int countTripsSkipped;

        ModeChoiceByPurpose(Purpose purpose, DataSet dataSet, ModeChoiceCalculator modeChoiceCalculator) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.modeChoiceCalculator = modeChoiceCalculator;
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            try {
                for (MitoHousehold household : dataSet.getHouseholds().values()) {
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        chooseMode(trip, calculateTripProbabilities(household, trip));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info(countTripsSkipped + " trips skipped for " + purpose);
            return null;
        }

        private EnumMap<Mode, Double> calculateTripProbabilities(MitoHousehold household, MitoTrip trip) {
            if (trip.getTripOrigin() == null || trip.getTripDestination() == null) {
                countTripsSkipped++;
                return null;
            }

            final int originId = trip.getTripOrigin().getZoneId();

            final int destinationId = trip.getTripDestination().getZoneId();
            final MitoZone origin = dataSet.getZones().get(originId);
            final MitoZone destination = dataSet.getZones().get(destinationId);
            final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originId,
                    destinationId);
            final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originId,
                    destinationId);
            return modeChoiceCalculator.calculateProbabilities(purpose, household, trip.getPerson(), origin, destination, travelTimes, travelDistanceAuto,
                    travelDistanceNMT, dataSet.getPeakHour());
    }

        private void chooseMode(MitoTrip trip, EnumMap<Mode, Double> probabilities) {
            if (probabilities == null) {
                countTripsSkipped++;
                return;
            }

            //found Nan when there is no transit!!
            probabilities.replaceAll((mode, probability) ->
                    probability.isNaN() ? 0: probability);

            double sum = MitoUtil.getSum(probabilities.values());
            if (sum > 0) {
                final Mode select = MitoUtil.select(probabilities, random);
                trip.setTripMode(select);
            } else {
                logger.error("Negative probabilities for trip " + trip.getId());
                trip.setTripMode(null);
            }
        }
    }
}
