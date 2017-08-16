package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.counterDroppedTripsAtBorder;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.currentTripId;

class TripsByPurposeGenerator {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGenerator.class);

    private final DataSet dataSet;
    private final Purpose purpose;

    private final HouseholdTypeManager householdTypeManager;

    private final TableDataSet travelSurveyHouseholdTable;
    private final TableDataSet travelSurveyTripsTable;

    private final List<MitoTrip> trips = new ArrayList<>();
    private final HashMap<String, Integer[]> tripsByHhType = new HashMap<>();

    public TripsByPurposeGenerator(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        householdTypeManager = new HouseholdTypeManager(dataSet, purpose);
        travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        travelSurveyTripsTable = dataSet.getTravelSurveyTripsTable();
    }

    public List<MitoTrip> generateTrips() {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        defineTripFrequenciesForHouseHoldTypes();
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            generateTripsForHousehold(hh);
        }
        return trips;
    }

    private void defineTripFrequenciesForHouseHoldTypes() {
        householdTypeManager.createHouseHoldTypeDefinitions();
        TableDataSet travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        Map<Integer, HouseholdType> householdTypeBySampleId = householdTypeManager.assignHouseholdTypeOfEachSurveyRecord(travelSurveyHouseholdTable);
        collectTripFrequencyDistribution(householdTypeBySampleId);
    }

    private void generateTripsForHousehold(MitoHousehold hh) {
        HouseholdType hhType = householdTypeManager.determineHouseholdType(hh);
        if (hhType == null) {
            logger.error("Could not create trips for Household " + hh.getHhId() + " with Purpose " + purpose + ": No Household Type applicable");
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
        int numTrips = selectNumberOfTrips(tripFrequencies);
        for (int i = 0; i < numTrips; i++) {
            createTrip(hh);
        }
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

        int pos = 1;
        for (int hhRow = 1; hhRow <= travelSurveyHouseholdTable.getRowCount(); hhRow++) {

            int sampleId = (int) travelSurveyHouseholdTable.getValueAt(hhRow, "sampn");
            int tripsOfThisHouseholdForGivenPurpose = 0;
            // Ready through trip file of HTS
            for (int trRow = pos; trRow <= travelSurveyTripsTable.getRowCount(); trRow++) {
                if ((int) travelSurveyTripsTable.getValueAt(trRow, "sampn") == sampleId) {
                    pos++;
                    Purpose htsTripPurpose = Purpose.valueOf(travelSurveyTripsTable.getStringValueAt(trRow, "mainPurpose"));
                    if (htsTripPurpose.equals(purpose)) {
                        // add this trip to this household
                        tripsOfThisHouseholdForGivenPurpose++;
                    }
                } else {
                    break;
                }
            }

            HouseholdType type = householdTypeBySampleId.get(sampleId);
            if(type == null) {
                logger.info("Trips for travel survey record " + sampleId + " and purpose " + purpose + " " +
                        "ignored, as no household type is applicable.");
                continue;
            }
            addTripFrequencyForHouseholdType(tripsOfThisHouseholdForGivenPurpose, type);
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
        // select number of trips
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) {
            probabilities[i] = (double) tripFrequencies[i];
        }
        return MitoUtil.select(probabilities);
    }

    private void createTrip(MitoHousehold hh) {
        int tripOrigin = hh.getHomeZone();
        boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(tripOrigin);
        if (dropThisTrip) {
            counterDroppedTripsAtBorder.incrementAndGet();
            return;
        }
        MitoTrip trip = new MitoTrip(currentTripId.incrementAndGet(), hh.getHhId(), purpose, tripOrigin);
        trips.add(trip);
    }

    private boolean reduceTripGenAtStudyAreaBorder(int tripOrigin) {
        if (!Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            return false;
        }
        float damper = dataSet.getZones().get(tripOrigin).getReductionAtBorderDamper();
        return MitoUtil.getRandomFloat() < damper;
    }
}
