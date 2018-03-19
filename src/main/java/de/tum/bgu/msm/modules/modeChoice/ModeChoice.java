package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.util.*;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;

public class ModeChoice extends Module{

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

    private void modeChoiceByPurpose(){
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose: Purpose.values()){
            executor.addTaskToQueue(new ModeChoiceByPurpose(purpose, dataSet, includeAV));
        }
        executor.execute();
//        for (Purpose purpose: Purpose.values()){
//            new ModeChoiceByPurpose(purpose, dataSet, includeAV);
//        }
    }

    private void printModeShares(){
        float[] tripsByMode = new float[Mode.values().length];
        float tripsWithNoMode = 0;
        for(MitoTrip trip : dataSet.getTrips().values()){
            if(trip.getTripMode() == null) {
                tripsWithNoMode++;
                continue;
            }
            for(int i=0; i<Mode.values().length; i++){
                if(trip.getTripMode().equals(Mode.valueOf(i))){
                    tripsByMode[i]++;
                }
            }
        }
        float totalTrips = 0;
        for(int i=0; i<tripsByMode.length; i++){
            totalTrips += tripsByMode[i];
        }
        logger.info("Mode could not be assigned to " + tripsWithNoMode + " trips");
        if(includeAV){
            for(int i=0; i<tripsByMode.length; i++){
                logger.info(Mode.valueOf(i) + " share = " + tripsByMode[i]*100/totalTrips + "%");
            }
        } else {
            for(int i=0; i<tripsByMode.length-2; i++){
                logger.info(Mode.valueOf(i) + " share = " + tripsByMode[i]*100/totalTrips + "%");
            }
        }
    }

    static class ModeChoiceByPurpose extends RandomizableConcurrentFunction<Void>{

        private final Purpose purpose;
        private final DataSet dataSet;
        private final Map<String, Double> travelTimeByMode = new HashMap<>();
        private final ModeChoiceJSCalculator calculator;
        private int countTripsSkipped;

        ModeChoiceByPurpose(Purpose purpose, DataSet dataSet, boolean includeAV) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            if(includeAV){ // if true
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAV")));
            } else {
                this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass().getResourceAsStream("ModeChoice")));
            }
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            for (MitoHousehold household: dataSet.getHouseholds().values()){
                for (MitoTrip trip : household.getTripsForPurpose(purpose)){
                    chooseMode(trip, calculateTripProbabilities(household, trip));
                }
            }
            logger.info(countTripsSkipped + " trips skipped for " + purpose);
            return null;
        }

         double[] calculateTripProbabilities(MitoHousehold household, MitoTrip trip) {
             if(trip.getTripOrigin() == null || trip.getTripDestination() == null) {
                 countTripsSkipped++;
                 return null;
             }
             travelTimeByMode.put("autoD", dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour(),1./60.));
             travelTimeByMode.put("autoP",dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour(),1./60.));
             double busTime =  dataSet.getTravelTimes("bus").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour(),1.);
             double trainTime = dataSet.getTravelTimes("train").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour(),1.);
             double tramMetroTime = dataSet.getTravelTimes("tramMetro").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour(),1.);
             if(busTime < 0) {
                 busTime = 1000;
             }
             if(trainTime < 0) {
                 trainTime = 1000;
             }
             if(tramMetroTime < 0) {
                 tramMetroTime = 1000;
             }
             travelTimeByMode.put("bus", busTime);
             travelTimeByMode.put("tramMetro", trainTime);
             travelTimeByMode.put("train", tramMetroTime);
             final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId())/1000.;
             final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId())/1000.;
             return calculator.calculateProbabilities(household, trip.getPerson(), trip, travelTimeByMode, travelDistanceAuto, travelDistanceNMT);
         }

        private void chooseMode(MitoTrip trip, double[] probabilities){
            if(probabilities==null){
                return;
            }
            double sum = MitoUtil.getSum(probabilities);
            if(sum > 0) {
                trip.setTripMode(Mode.valueOf(MitoUtil.select(probabilities, random, sum)));
            } else {
                logger.error("Negative probabilities for trip " + trip.getId());
                trip.setTripMode(null);
            }
        }
    }
}
