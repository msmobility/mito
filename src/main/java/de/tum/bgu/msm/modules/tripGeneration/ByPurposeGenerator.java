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

import java.util.HashMap;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.counterDroppedTripsAtBorder;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.currentTripId;

/**
 * Created by Nico on 21/07/2017.
 */
public class ByPurposeGenerator implements Function1<String, Void> {

    private static Logger logger = Logger.getLogger(ByPurposeGenerator.class);

    private final DataSet dataSet;

    public ByPurposeGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public Void apply(String purpose) {
        microgenerateTripsByPurpose(purpose);
        return null;
    }


    private void microgenerateTripsByPurpose (String strPurp) {

        logger.info("  Generating trips with purpose " + strPurp + " (multi-threaded)");
        HouseholdTypeManager generator = new HouseholdTypeManager(dataSet);
        generator.createHouseHoldTypeDefinitionsForPurpose(strPurp);
        Map<Integer, HouseholdType> householdTypeBySampleId = generator.assignHouseholdTypeOfEachSurveyRecordForPurpose(strPurp);
        HashMap<String, Integer[]> tripsByHhTypeAndPurpose = collectTripFrequencyDistributionForPurpose(householdTypeBySampleId, strPurp);
        int purposeNum = dataSet.getPurposeIndex(strPurp);
        // Generate trips for each household
        for (MitoHousehold hh: dataSet.getHouseholds().values()) {
            int incCategory = translateIncomeIntoCategory (hh.getIncome());
            HouseholdType hhType = generator.getHhType(strPurp,  hh.getHhSize(), hh.getNumberOfWorkers(),
                    incCategory, hh.getAutos(), dataSet.getZones().get(hh.getHomeZone()).getRegion());
            String token = hhType.getId() + "_" + strPurp;
            Integer[] tripFrequencies = tripsByHhTypeAndPurpose.get(token);
            if (tripFrequencies == null) {
                logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
            }
            if (MitoUtil.getSum(tripFrequencies) == 0) {
                continue;
            }
            int numTrips = selectNumberOfTrips(tripFrequencies);
            for (int i = 0; i < numTrips; i++) {
                // todo: for non-home based trips, do not set origin as home
                int tripOrigin = hh.getHomeZone();
                boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(tripOrigin);
                if (dropThisTrip) {
                    counterDroppedTripsAtBorder.incrementAndGet();
                }
                if (dropThisTrip) continue;
                synchronized (currentTripId) {
                    MitoTrip trip = new MitoTrip(currentTripId.incrementAndGet(), hh.getHhId(), purposeNum, tripOrigin);
                    dataSet.getTrips().put(trip.getTripId(), trip);
                    hh.addTrip(trip);
                }
            }
        }
    }

    private int translateIncomeIntoCategory (int hhIncome) {
        // translate income in absolute dollars into household travel survey income categories

        if (hhIncome < 10000) return 1;
        else if (hhIncome >= 10000 && hhIncome < 15000) return 2;
        else if (hhIncome >= 15000 && hhIncome < 30000) return 3;
        else if (hhIncome >= 30000 && hhIncome < 40000) return 4;
        else if (hhIncome >= 40000 && hhIncome < 50000) return 5;
        else if (hhIncome >= 50000 && hhIncome < 60000) return 6;
        else if (hhIncome >= 60000 && hhIncome < 75000) return 7;
        else if (hhIncome >= 75000 && hhIncome < 100000) return 8;
        else if (hhIncome >= 100000 && hhIncome < 125000) return 9;
        else if (hhIncome >= 125000 && hhIncome < 150000) return 10;
        else if (hhIncome >= 150000 && hhIncome < 200000) return 11;
        else if (hhIncome >= 200000) return 12;
        logger.error("Unknown HTS income: " + hhIncome);
        return -1;
    }

    private int selectNumberOfTrips (Integer[] tripFrequencies) {
        // select number of trips
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) {
            probabilities[i] = (double) tripFrequencies[i];
        }
        return MitoUtil.select(MitoUtil.getRand(), probabilities);
    }


    private boolean reduceTripGenAtStudyAreaBorder(int tripOrigin) {
        // as trips near border of study area that travel to destinations outside of study area are not represented,
        // trip generation near border of study area can be reduced artificially with this method

        if (!Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            return false;
        }

        float damper = dataSet.getZones().get(tripOrigin).getReductionAtBorderDamper();
        return MitoUtil.getRand().nextFloat() < damper;
    }

    public HashMap<String, Integer[]> collectTripFrequencyDistributionForPurpose(Map<Integer, HouseholdType> householdTypeBySampleId, String purpose) {
        // Summarize frequency of number of trips for each household type by each trip purpose
        //
        // Storage Structure
        //   HashMap<String, Integer> tripsByHhTypeAndPurpose: Token is hhType_TripPurpose
        //   |
        //   contains -> Integer[] tripFrequencyList: Frequency of 0, 1, 2, 3, ... trips

        HashMap<String, Integer[]> tripsByHhTypeAndPurpose = new HashMap<>();  // contains trips by hhtype and purpose

        for (HouseholdType type : householdTypeBySampleId.values()) {
            String token = type.getId() + "_" + purpose;
            // fill Storage structure from bottom       0                  10                  20                  30
            Integer[] tripFrequencyList = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  // space for up to 30 trips
            tripsByHhTypeAndPurpose.put(token, tripFrequencyList);
        }

        TableDataSet travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        for (int hhRow = 1; hhRow <= travelSurveyHouseholdTable.getRowCount(); hhRow++) {
            int sampleId = (int) travelSurveyHouseholdTable.getValueAt(hhRow, "sampn");
            HouseholdType type = householdTypeBySampleId.get(sampleId);
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
            String token = type.getId() + "_" + purpose;
            Integer[] tripsOfThisHouseholdType = tripsByHhTypeAndPurpose.get(token);
            tripsOfThisHouseholdType[tripsOfThisHouseholdForGivenPurpose]++;
            tripsByHhTypeAndPurpose.put(token, tripsOfThisHouseholdType);

        }
        return tripsByHhTypeAndPurpose;
    }
}
