package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;

public class ModeChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);
    private final boolean includeAV = Resources.instance.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);

    public ModeChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info(" Calculating mode choice probabilities for each trip. Modes considered - 1. Auto driver, 2. Auto passenger, 3. Bicycle, 4. Bus, 5. Train, 6. Tram or Metro, 7. Walk, 8. Private AV, 9. TNC ");
        modeChoiceByPurpose();
        printModeShares();
    }

    private void modeChoiceByPurpose() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new ModeChoiceByPurpose(purpose, dataSet, includeAV));
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

        //filter valid trips by purpose
        Map<Purpose, List<MitoTrip>> tripsByPurposeTNC = dataSet.getTrips().values().stream()
                .filter(trip -> trip.getTripMode() != null)
                .filter(trip -> dataSet.getZones().get(trip.getTripOrigin().getZoneId()).isMunichZone())
                .filter(trip -> dataSet.getZones().get(trip.getTripDestination().getZoneId()).isMunichZone())
                .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));

        tripsByPurposeTNC.forEach((purpose, trips) -> {
            final long totalTrips = trips.size();
            trips.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((mode, count) ->
                            dataSet.addModeShareForPurposeTNC(purpose, mode, (double) count / totalTrips));
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


        for (Purpose purpose : Purpose.values()) {
            logger.info("#################################################");
            logger.info("#Munich trips#");
            logger.info("Mode shares for purpose " + purpose + ":");
            for (Mode mode : Mode.values()) {
                Double share = dataSet.getModeShareForPurposeTNC(purpose, mode);
                if (share != null) {
                    logger.info(mode + " = " + share * 100 + "%");
                }
            }
        }


        //auxiliar code for calibration
//        double shareAutoDriver = 0.15;
//        double shareAutoPassenger = 0.43;
//        double shareNonMotorized = 0.0;
//        double shareMetro = 0.0;
//        double shareBus = 0.03;
//        double shareTrain = 1 - shareAutoDriver - shareAutoPassenger - shareNonMotorized - shareMetro - shareBus;
//
//        double k_autoDriver = shareAutoDriver - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.autoDriver);
//        double k_autoPassenger = shareAutoPassenger - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.autoPassenger);
//        double k_bicycle = shareNonMotorized - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.bicycle);
//        double k_walk = shareNonMotorized - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.walk);
//        double k_bus = shareBus - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.bus);
//        double k_metro = shareMetro - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.tramOrMetro);
//        double k_train = shareTrain - dataSet.getModeShareForPurpose(Purpose.AIRPORT, Mode.train);
//
//        logger.info(k_autoDriver + "," +
//                k_autoPassenger + "," + k_bicycle +
//                "," + k_bus + "," + k_train + "," + k_metro + "," +
//                k_walk );


    }

    static class ModeChoiceByPurpose extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final ModeChoiceJSCalculator calculator;
        private final TravelTimes travelTimes;
        private int countTripsSkipped;

        ModeChoiceByPurpose(Purpose purpose, DataSet dataSet, boolean includeAV) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            if (includeAV) {
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("ModeChoice_av_tnc")), purpose);
            } else {
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("ModeChoice")), purpose);
            }
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

        double[] calculateTripProbabilities(MitoHousehold household, MitoTrip trip) {
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
            final double tncCost = Double.parseDouble(Resources.instance.getString(Properties.TNC_COST));
            final double tncWaitingTime = Double.parseDouble(Resources.instance.getString(Properties.TNC_WAITING_TIME));
            final double tncDetourFactor = Double.parseDouble(Resources.instance.getString(Properties.TNC_DETOUR_FACTOR));
            double income = household.getMonthlyIncome_EUR();
            double scenario = 0;
            if (Resources.instance.getString(de.tum.bgu.msm.resources.Properties.TNC_SERVICE_AREA).equals("munich")){
                scenario = 1;
            }
            return calculator.calculateProbabilities(household, trip.getPerson(), origin, destination, travelTimes, travelDistanceAuto,
                    travelDistanceNMT, dataSet.getPeakHour(),scenario, tncCost, tncWaitingTime, tncDetourFactor);
        }

        private void chooseMode(MitoTrip trip, double[] probabilities) {
            if (probabilities == null) {
                countTripsSkipped++;
                return;
            }
            //found Nan when there is no transit!!
            for (int i = 0; i < probabilities.length; i++) {
                if (Double.isNaN(probabilities[i])) {
                    probabilities[i] = 0;
                }
            }

            double sum = MitoUtil.getSum(probabilities);
            if (sum > 0) {
                trip.setTripMode(Mode.valueOf(MitoUtil.select(probabilities, random, sum)));
            } else {
                logger.error("Negative probabilities for trip " + trip.getId());
                trip.setTripMode(null);
            }
        }
    }
}
