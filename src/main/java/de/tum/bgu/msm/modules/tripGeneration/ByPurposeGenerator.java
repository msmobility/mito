package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import com.pb.sawdust.calculator.Function1;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.counterDroppedTripsAtBorder;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.currentTripId;

/**
 * Created by Nico on 21/07/2017.
 */
public class ByPurposeGenerator {

    private static Logger logger = Logger.getLogger(ByPurposeGenerator.class);

    private final DataSet dataSet;
    private final String purpose;
    private final int purposeIndex;

    private final HouseholdTypeManager householdTypeManager;

    private final List<MitoTrip> trips = new ArrayList<>();
    private HashMap<String, Integer[]> tripsByHhType = new HashMap<>();

    public ByPurposeGenerator(DataSet dataSet, String purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.purposeIndex = dataSet.getPurposeIndex(purpose);
        householdTypeManager = new HouseholdTypeManager(dataSet, purpose);
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
        String token = hhType.getId() + "_" + purpose;
        Integer[] tripFrequencies = tripsByHhType.get(token);
        if (tripFrequencies == null) {
            logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
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
        TableDataSet travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        for (int hhRow = 1; hhRow <= travelSurveyHouseholdTable.getRowCount(); hhRow++) {

            int sampleId = (int) travelSurveyHouseholdTable.getValueAt(hhRow, "sampn");
            int tripsOfThisHouseholdForGivenPurpose = getNumberOfTripsForSample(sampleId);

            HouseholdType type = householdTypeBySampleId.get(sampleId);
            addTripFrequencyForHouseholdType(tripsOfThisHouseholdForGivenPurpose, type);
        }
    }

    private void addTripFrequencyForHouseholdType(int tripsOfThisHouseholdForGivenPurpose, HouseholdType type) {
        String token = type.getId() + "_" + purpose;
        Integer[] tripsOfThisHouseholdType = tripsByHhType.get(token);
        tripsOfThisHouseholdType[tripsOfThisHouseholdForGivenPurpose]++;
        tripsByHhType.put(token, tripsOfThisHouseholdType);
    }

    private int getNumberOfTripsForSample(int sampleId) {
        int tripsOfThisHouseholdForGivenPurpose = 0;
        // Ready through trip file of HTS
        TableDataSet travelSurveyTripsDable = dataSet.getTravelSurveyTripsTable();
        for (int trRow = 1; trRow <= travelSurveyTripsDable.getRowCount(); trRow++) {
            if ((int) travelSurveyTripsDable.getValueAt(trRow, "sampn") == sampleId) {
                String htsTripPurpose = travelSurveyTripsDable.getStringValueAt(trRow, "mainPurpose");
                if (htsTripPurpose.equals(purpose)) {
                    // add this trip to this household
                    tripsOfThisHouseholdForGivenPurpose++;
                }
            }
        }
        return tripsOfThisHouseholdForGivenPurpose;
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
        return MitoUtil.select(MitoUtil.getRand(), probabilities);
    }

    private void createTrip(MitoHousehold hh) {
        // todo: for non-home based trips, do not set origin as home
        int tripOrigin = hh.getHomeZone();
        boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(tripOrigin);
        if (dropThisTrip) {
            counterDroppedTripsAtBorder.incrementAndGet();
            return;
        }
        MitoTrip trip = new MitoTrip(currentTripId.incrementAndGet(), hh.getHhId(), purposeIndex, tripOrigin);
        trips.add(trip);
    }

    private boolean reduceTripGenAtStudyAreaBorder(int tripOrigin) {
        if (!Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            return false;
        }
        float damper = dataSet.getZones().get(tripOrigin).getReductionAtBorderDamper();
        return MitoUtil.getRand().nextFloat() < damper;
    }
}
