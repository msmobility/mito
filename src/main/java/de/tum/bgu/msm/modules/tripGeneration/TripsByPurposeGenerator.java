package de.tum.bgu.msm.modules.tripGeneration;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.survey.SurveyRecord;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.*;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.DROPPED_TRIPS_AT_BORDER_COUNTER;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

class TripsByPurposeGenerator extends RandomizableConcurrentFunction {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGenerator.class);
    private final boolean dropAtBorder = Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER);

    Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private final HouseholdTypeManager householdTypeManager;

    private final HashMap<String, Integer[]> tripsByHhType = new HashMap<>();

    public TripsByPurposeGenerator(DataSet dataSet, Purpose purpose) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        householdTypeManager = new HouseholdTypeManager(purpose);
    }

    @Override
    public void execute() {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        defineTripFrequenciesForHouseHoldTypes();
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        long counter = 0;
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for purpose " + purpose + ".");
            }
            generateTripsForHousehold(hh);
            counter++;
        }
        tripsByHH.forEach((hh, trips) -> {
            hh.setTripsByPurpose(trips, purpose);
            trips.forEach(trip -> {
                dataSet.addTrip(trip);
            });
        });
    }

    private void defineTripFrequenciesForHouseHoldTypes() {
        householdTypeManager.createHouseHoldTypeDefinitions();
        Map<Integer, HouseholdType> householdTypeBySampleId = householdTypeManager.assignHouseholdTypeOfEachSurveyRecord(dataSet.getSurvey());
        collectTripFrequencyDistribution(householdTypeBySampleId);
    }

    private void generateTripsForHousehold(MitoHousehold hh) {
        HouseholdType hhType = householdTypeManager.determineHouseholdType(hh);
        if (hhType == null) {
            logger.error("Could not create trips for Household " + hh.getId() + " with Purpose " + purpose + ": No Household Type applicable");
            return;
        }
        String token = hhType.getId() + "_" + purpose;
        Integer[] tripFrequencies = tripsByHhType.get(token);
        if (tripFrequencies == null) {
            logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
            return;
        }
        if (MitoUtil.getSum(tripFrequencies) == 0) {
            logger.info("No trips for this hhType/Purpose: " + token);
            return;
        }

        List<MitoTrip> trips = new ArrayList<>();
        for (int i = 0; i < selectNumberOfTrips(tripFrequencies); i++) {
            MitoTrip trip = createTrip(hh);
            if (trip != null) {
                trips.add(trip);
            }
        }
        tripsByHH.put(hh, trips);
    }

    private void collectTripFrequencyDistribution(Map<Integer, HouseholdType> householdTypeBySampleId) {
        // Summarize frequency of number of trips for each household type by each trip purpose
        //
        // Storage Structure
        //   HashMap<String, Integer> tripsByHhTypeAndPurpose: Token is hhType_TripPurpose
        //   |
        //   contains -> Integer[] tripFrequencyList: Frequency of 0, 1, 2, 3, ... trips
        initializeFrequencyArrays(householdTypeBySampleId);
        fillFrequencyArrays(householdTypeBySampleId);
    }

    private void fillFrequencyArrays(Map<Integer, HouseholdType> householdTypeBySampleId) {

        for (SurveyRecord record : dataSet.getSurvey().getRecords().values()) {
            HouseholdType type = householdTypeBySampleId.get(record.getId());
            if (type == null) {
                logger.info("Trips for travel survey record " + record.getId() + " and purpose " + purpose + " " +
                        "ignored, as no household type is applicable.");
                continue;
            }
            addTripFrequencyForHouseholdType(record.getTripsForPurpose(purpose), type);
        }
    }

    private void addTripFrequencyForHouseholdType(int tripsOfThisHouseholdForGivenPurpose, HouseholdType type) {
        String token = type.getId() + "_" + purpose;
        Integer[] tripsOfThisHouseholdType = tripsByHhType.get(token);
        tripsOfThisHouseholdType[tripsOfThisHouseholdForGivenPurpose]++;
        tripsByHhType.put(token, tripsOfThisHouseholdType);
    }


    private void initializeFrequencyArrays(Map<Integer, HouseholdType> householdTypeBySampleId) {
        for (HouseholdType type : householdTypeBySampleId.values()) {
            String token = type.getId() + "_" + purpose;
            // fill Storage structure from bottom       0                  10                  20                  30
            Integer[] tripFrequencyList = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // space for up to 30 trips
            tripsByHhType.put(token, tripFrequencyList);
        }
    }

    private int selectNumberOfTrips(Integer[] tripFrequencies) {
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) {
            probabilities[i] = (double) tripFrequencies[i];
        }
        return MitoUtil.select(probabilities, random);
    }

    private MitoTrip createTrip(MitoHousehold hh) {
        MitoZone tripOrigin = hh.getHomeZone();
        boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(tripOrigin);
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
