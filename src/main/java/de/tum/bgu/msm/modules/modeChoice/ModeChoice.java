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
    private final boolean includeAV = Resources.INSTANCE.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);

    public ModeChoice(DataSet dataSet) {
        super(dataSet);
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

        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {

            //filter valid trips by purpose and without disabilities
            Map<Purpose, List<MitoTrip>> tripsByPurposeWithoutDisability = dataSet.getTrips().values().stream()
                    .filter(trip -> trip.getTripMode() != null)
                    .filter(trip -> trip.getPerson().getDisability() == Disability.WITHOUT)
                    .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));


            tripsByPurposeWithoutDisability.forEach((purpose, trips) -> {
                final long totalTrips = trips.size();
                trips.parallelStream()
                        //group number of trips by mode
                        .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                        //calculate and add share to data set table
                        .forEach((mode, count) ->
                                dataSet.addModeShareForPurposeWithoutDisability(purpose, mode, (double) count / totalTrips));
            });

            for (Map.Entry<Purpose, List<MitoTrip>> entry : tripsByPurposeWithoutDisability.entrySet()) {
                Purpose key = entry.getKey();
                dataSet.addTripByPurposeByDisability(key, Disability.WITHOUT, tripsByPurposeWithoutDisability.get(key).size());
            }

            for (Purpose purpose : Purpose.values()) {
                logger.info("#################################################");
                logger.info("Persons without disabilities performed " + dataSet.getTripsByPurposeByDisability(purpose,Disability.WITHOUT) + " " + purpose + " trips.");
                for (Mode mode : Mode.values()) {
                    Double share = dataSet.getModeSharesByPurposeWithoutDisability(purpose, mode);
                    if (share != null) {
                        logger.info(mode + " = " + share * 100 + "%");
                    }
                }
            }

            //filter valid trips by purpose and with mental disabilities
            Map<Purpose, List<MitoTrip>> tripsByPurposeMentalDisability = dataSet.getTrips().values().stream()
                    .filter(trip -> trip.getTripMode() != null)
                    .filter(trip -> trip.getPerson().getDisability() == Disability.MENTAL)
                    .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));


            tripsByPurposeMentalDisability.forEach((purpose, trips) -> {
                final long totalTrips = trips.size();
                trips.parallelStream()
                        //group number of trips by mode
                        .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                        //calculate and add share to data set table
                        .forEach((mode, count) ->
                                dataSet.addModeShareForPurposeMentalDisability(purpose, mode, (double) count / totalTrips));
            });

            for (Map.Entry<Purpose, List<MitoTrip>> entry : tripsByPurposeMentalDisability.entrySet()) {
                Purpose key = entry.getKey();
                dataSet.addTripByPurposeByDisability(key, Disability.MENTAL, tripsByPurposeMentalDisability.get(key).size());
            }

            for (Purpose purpose : Purpose.values()) {
                logger.info("#################################################");
                logger.info("Persons with mental disabilities performed " + dataSet.getTripsByPurposeByDisability(purpose,Disability.MENTAL) + " " + purpose + " trips.");
                for (Mode mode : Mode.values()) {
                    Double share = dataSet.getModeSharesByPurposeMentalDisability(purpose, mode);
                    if (share != null) {
                        logger.info(mode + " = " + share * 100 + "%");
                    }
                }
            }


            //filter valid trips by purpose and with mental disabilities
            Map<Purpose, List<MitoTrip>> tripsByPurposePhysicalDisability = dataSet.getTrips().values().stream()
                    .filter(trip -> trip.getTripMode() != null)
                    .filter(trip -> trip.getPerson().getDisability() == Disability.PHYSICAL)
                    .collect(Collectors.groupingBy(MitoTrip::getTripPurpose));


            tripsByPurposePhysicalDisability.forEach((purpose, trips) -> {
                final long totalTrips = trips.size();
                trips.parallelStream()
                        //group number of trips by mode
                        .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                        //calculate and add share to data set table
                        .forEach((mode, count) ->
                                dataSet.addModeShareForPurposePhysicalDisability(purpose, mode, (double) count / totalTrips));
            });

/*            tripsByPurposePhysicalDisability.forEach((purpose, trips) -> {
                final long totalTrips = trips.size();
                trips.parallelStream()
                        //group number of trips by mode
                        .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                        //calculate and add share to data set table
                        .forEach((mode, count) ->
                                dataSet.addTripByPurposeByDisability(purpose, Disability.PHYSICAL, count));
            });*/
            for (Map.Entry<Purpose, List<MitoTrip>> entry : tripsByPurposePhysicalDisability.entrySet()) {
                Purpose key = entry.getKey();
                dataSet.addTripByPurposeByDisability(key, Disability.PHYSICAL, tripsByPurposePhysicalDisability.get(key).size());
            }

            for (Purpose purpose : Purpose.values()) {
                logger.info("#################################################");
                logger.info("Persons with physical disabilities performed " + dataSet.getTripsByPurposeByDisability(purpose,Disability.PHYSICAL) + " " + purpose + " trips.");
                logger.info("Persons with physical disability. Mode shares for purpose " + purpose + ":");
                for (Mode mode : Mode.values()) {
                    Double share = dataSet.getModeSharesByPurposePhysicalDisability(purpose, mode);
                    if (share != null) {
                        logger.info(mode + " = " + share * 100 + "%");
                    }
                }
            }
        }

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
            if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
                if (includeAV) {
                    this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoiceAVDisability")), purpose);
                } else {
                    this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoiceDisability")), purpose);
                }
            } else {
                if (includeAV) {
                    this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoiceAV")), purpose);
                } else {
                    this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoice")), purpose);

                }
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
            return calculator.calculateProbabilities(household, trip.getPerson(), origin, destination, travelTimes, travelDistanceAuto,
                    travelDistanceNMT, dataSet.getPeakHour());
        }

        private void chooseMode(MitoTrip trip, double[] probabilities) {
            if (probabilities == null) {
                countTripsSkipped++;
                return;
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
