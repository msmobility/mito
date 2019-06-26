package de.tum.bgu.msm.modules.tripGeneration;

import cern.jet.random.tdouble.NegativeBinomial;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

class TripsByPurposeGeneratorAccessibility extends Module {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorAccessibility.class);
    final static AtomicInteger DROPPED_TRIPS_AT_BORDER_COUNTER = new AtomicInteger();
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();

    public TripsByPurposeGeneratorAccessibility(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(){
        tripGenerationByPurpose();
        logTripGeneration();
    }

    private void tripGenerationByPurpose(){
/*
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()){
            executor.addTaskToQueue(new TripGenerationByPurpose(purpose, dataSet));
        }
        executor.execute();
*/
        final ConcurrentExecutor<Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>>> executor =
                ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Callable<Pair<Purpose, Map<MitoHousehold,List<MitoTrip>>>>> tasks = new ArrayList<>();
        for(Purpose purpose: Purpose.values()) {
            tasks.add(new TripGenerationByPurpose(purpose, dataSet));
        }
        final List<Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>>> results = executor.submitTasksAndWaitForCompletion(tasks);
        for(Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>> result: results) {
            final Purpose purpose = result.getKey();
            final Map<MitoHousehold, List<MitoTrip>> tripsByHouseholds = result.getValue();
            for(Map.Entry<MitoHousehold, List<MitoTrip>> tripsByHousehold: tripsByHouseholds.entrySet()) {
                tripsByHousehold.getKey().setTripsByPurpose(tripsByHousehold.getValue(), purpose);
                dataSet.addTrips(tripsByHousehold.getValue());
            }
        }
    }

    private void logTripGeneration() {
        long rawTrips = dataSet.getTrips().size() + DROPPED_TRIPS_AT_BORDER_COUNTER.get();
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (DROPPED_TRIPS_AT_BORDER_COUNTER.get() > 0) {
            logger.info(MitoUtil.customFormat("  " + "###,###", DROPPED_TRIPS_AT_BORDER_COUNTER.get()) +
                    " trips were dropped at boundary of study area.");
        }
    }


    static class TripGenerationByPurpose extends RandomizableConcurrentFunction<Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>>>{

        private final Purpose purpose;
        private final DataSet dataSet;
        private final TripGenerationAccessibilityJSCalculator calculator;
        private int countTripsSkipped;
        private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();
        private final boolean dropAtBorder = Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER);

        TripGenerationByPurpose(Purpose purpose, DataSet dataSet){
            super((MitoUtil.getRandomObject().nextLong()));
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.calculator = new TripGenerationAccessibilityJSCalculator(new InputStreamReader(
                    this.getClass().getResourceAsStream("TripGenerationAccessibilityCalc")), purpose);
        }

        @Override
        public Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>> call(){
            countTripsSkipped = 0;
            try{
                for (MitoHousehold household : dataSet.getHouseholds().values()){
                    chooseTrips(household, calculateNumberOfTripsProbabilities(household));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            logger.info(countTripsSkipped + " households skipped for " + purpose);
            return new Pair<>(purpose, tripsByHH);
        }

        double[] calculateNumberOfTripsProbabilities(MitoHousehold household){
            double averageTrips = calculator.calculateProbabilities(household, household.getHomeZone());
            double[] probabilities = new double[30];
            double exponentialPower = Math.pow(2.718281828, -averageTrips); // negative power k
            double landaPowerK = Math.pow(averageTrips, 0); // Landa elevated k
            double numerator = exponentialPower * landaPowerK;
            probabilities[0] = numerator;

            int fact = 1;
            for (int i = 1; i < 30; i++){
                if (probabilities[i - 1] < 1.0*Math.pow(10, -7)){
                    probabilities[i] = 0;
                } else {
                    landaPowerK = Math.pow(averageTrips, i);
                    numerator = exponentialPower * landaPowerK;
                    fact = fact * i;
                    probabilities[i] = numerator / fact;
                }
            }
            return probabilities;
        }

        private void chooseTrips(MitoHousehold household, double[] probabilities){
            if (probabilities == null){
                countTripsSkipped++;
                return;
            }
            double sum = MitoUtil.getSum(probabilities);
            if (sum > 0){
                int numberOfTrips = MitoUtil.select(probabilities,random, sum);
                List<MitoTrip> trips = new ArrayList<>();
                for (int i = 0; i < numberOfTrips; i++) {
                    MitoTrip trip = createTrip(household);
                    if (trip != null) {
                        trips.add(trip);
                    }
                }
                tripsByHH.put(household, trips);
            }
        }


        private MitoTrip createTrip(MitoHousehold hh) {
            boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(hh.getHomeZone());
            if (dropThisTrip) {
                DROPPED_TRIPS_AT_BORDER_COUNTER.incrementAndGet();
                return null;
            }
            return new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), purpose);
        }

        private boolean reduceTripGenAtStudyAreaBorder(MitoZone tripOrigin) {
            if (dropAtBorder) {
                float damper = dataSet.getZones().get(tripOrigin.getId()).getReductionAtBorderDamper();
                return random.nextFloat() < damper;
            }
            return false;
        }
    }


/*    public Pair<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded + Poisson)");
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            generateTripsForHousehold(hh);
        }
        return new Pair<>(purpose, tripsByHH);
    }


    private void generateTripsForHousehold(MitoHousehold hh) {
        double averageNumberTrips = tripGenCalculator.calculateProbabilities(hh, hh.getHomeZone());
        double[] probabilityNumberOfTrips = calculatePoissonProbabilities(averageNumberTrips);
        List<MitoTrip> trips = new ArrayList<>();
        int numberOfTrips  = MitoUtil.select(probabilityNumberOfTrips, random);
        for (int i = 0; i < numberOfTrips; i++) {
            MitoTrip trip = createTrip(hh);
            if (trip != null) {
                trips.add(trip);
            }
        }
        tripsByHH.put(hh, trips);
    }*/


}
