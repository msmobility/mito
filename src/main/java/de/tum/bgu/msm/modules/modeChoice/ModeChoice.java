package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;
import static de.tum.bgu.msm.resources.Properties.UAM_CHOICE;

public class ModeChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);
    private final boolean includeAV = Resources.INSTANCE.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);
    private final boolean includeUAM = Resources.INSTANCE.getBoolean(UAM_CHOICE, true);

    public ModeChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        StringBuilder info = new StringBuilder(" Calculating mode choice probabilities for each trip. Modes considered: ");
        int i = 1;
        for (Mode mode : Mode.values()) {
            info.append("" + i++ + ". " + mode);
            if (i != Mode.values().length)
                info.append(", ");
        }
        logger.info(info);

        if (includeAV)
            logger.info("AV mode choice is included.");
        else
            logger.info("AV mode choice is excluded.");

        if (includeUAM)
            logger.info("UAM mode choice is included.");
        else
            logger.info("UAM mode choice is excluded.");

        modeChoiceByPurpose();
        printModeShares();
    }

    private void modeChoiceByPurpose() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new ModeChoiceByPurpose(purpose, dataSet, includeAV, includeUAM));
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

        tripsByPurpose.forEach((purpose, trips) -> {
            final long totalTrips = trips.size();
            trips.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoTrip::getTripMode, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((mode, count) ->
                            dataSet.addModeCountForPurpose(purpose, mode, (double) count));
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
        logger.info("#################################################");
        for (Purpose purpose : Purpose.values()) {

                Double share = dataSet.getModeShareForPurpose(purpose, Mode.uam);
                if (share != null) {
                    logger.info(purpose + ": " + share);
                }
        }
        logger.info("#################################################");
        for (Purpose purpose : Purpose.values()) {

            Double count = dataSet.getModeCountForPurpose(purpose, Mode.uam);
            if (count != null) {
                logger.info(purpose + ": " + count);
            }
        }

        double uam = 0.;
        for (MitoTrip trip : this.dataSet.getTrips().values()){
            if(trip.getTripMode()== Mode.uam){
                uam++;
            }
        }

        logger.info("UAM share: " + uam/this.dataSet.getTrips().values().size());

    }

    public void printModalShares(int iteration, String scenarioName) {
        String fileName = "scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/modeChoice/modalShares" + iteration + ".csv";

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(fileName));
            pw.println("iteration,purpose,mode,share");
            for (Purpose purpose : Purpose.values()){
                for (Mode mode : Mode.values()){
                    StringBuilder sb = new StringBuilder();
                    Double share = dataSet.getModeShareForPurpose(purpose, mode);
                    if(share != null){
                        sb.append(iteration).append(",").append(purpose).append(",").append(mode).append(",").append(share);
                    } else {
                        sb.append(iteration).append(",").append(purpose).append(",").append(mode).append(",").append(0.);
                    }
                    pw.println(sb);
                }
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }

    static class ModeChoiceByPurpose extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final ModeChoiceJSCalculator calculator;
        private final TravelTimes travelTimes;
        private final AccessAndEgressVariables accessAndEgressVariables;
        private int countTripsSkipped;

        ModeChoiceByPurpose(Purpose purpose, DataSet dataSet, boolean includeAV, boolean includeUAM) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.accessAndEgressVariables = dataSet.getAccessAndEgressVariables();
            if (includeAV) {
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("ModeChoiceAV")), purpose);
            } else if (includeUAM) {
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("ModeChoiceUAMIncremental")), purpose);
            } else{
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
                        //double[] probabilities = calculateTripProbabilities(household, trip);
                        //logger.info("Probabilities for modes: " + Arrays.toString(probabilities));
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
            if (Resources.INSTANCE.getBoolean(UAM_CHOICE, true)){
                final double flyingDistanceUAM_km = dataSet.getFlyingDistanceUAM().getTravelDistance(originId,
                        destinationId);
                final double uamFare_eurkm = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_COST_KM));
                final double uamFare_eurbase = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_COST_BASE));

                //todo car costs hard coded to 0.07!!!!!
                final double uamCost_eur = uamFare_eurbase + flyingDistanceUAM_km * uamFare_eurkm +
                        dataSet.getAccessAndEgressVariables().
                                getAccessVariable(trip.getTripOrigin(), trip.getTripDestination(), "uam", AccessAndEgressVariables.AccessVariable.ACCESS_DIST_KM) * 0.07 +
                        dataSet.getAccessAndEgressVariables().
                                getAccessVariable(trip.getTripOrigin(), trip.getTripDestination(), "uam", AccessAndEgressVariables.AccessVariable.EGRESS_DIST_KM) * 0.07;

                final double processingTime_min = dataSet.getTotalHandlingTimes().
                        getWaitingTime(trip, trip.getTripOrigin(), trip.getTripDestination(), Mode.uam.toString());

                return calculator.calculateProbabilitiesUAM(household, trip.getPerson(), origin, destination, travelTimes, accessAndEgressVariables, travelDistanceAuto,
                        travelDistanceNMT, uamCost_eur, dataSet.getPeakHour(),processingTime_min,uamFare_eurkm);
            } else {
                return calculator.calculateProbabilities(household, trip.getPerson(), origin, destination, travelTimes, travelDistanceAuto,
                        travelDistanceNMT, dataSet.getPeakHour());
            }
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
                //logger.error("Negative probabilities for trip " + trip.getId());
                trip.setTripMode(null);
            }
        }
    }
}
