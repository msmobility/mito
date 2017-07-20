package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import com.pb.sawdust.calculator.Function1;
import com.pb.sawdust.util.array.ArrayUtil;
import com.pb.sawdust.util.concurrent.ForkJoinPoolFactory;
import com.pb.sawdust.util.concurrent.IteratorAction;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGenerator {

    private static Logger logger = Logger.getLogger(RawTripGenerator.class);

    private int counterDroppedTripsAtBorder = 0;
    private Integer currentTripId = 0;

    private final DataSet dataSet;

    public RawTripGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {
        microgenerateTrips();
    }

    private void microgenerateTrips () {

        // Multi-threading code
//        Function1<String,Void> tripGenByPurposeMethod = new Function1<String,Void>() {
//            public Void apply(String purp) {
//                microgenerateTripsByPurpose(purp);
//                return null;
//            }
//        };

//        // Generate trips for each purpose
//        Iterator<String> tripPurposeIterator = ArrayUtil.getIterator(dataSet.getPurposes());
//        IteratorAction<String> itTask = new IteratorAction<>(tripPurposeIterator, tripGenByPurposeMethod);
//        ForkJoinPool pool = ForkJoinPoolFactory.getForkJoinPool();
//        pool.execute(itTask);
//        itTask.waitForCompletion();

        for(String purpose: dataSet.getPurposes()) {
            microgenerateTripsByPurpose(purpose);
        }

        logTripGeneration();
    }

    private void microgenerateTripsByPurpose (String strPurp) {

        logger.info("  Generating trips with purpose " + strPurp + " (multi-threaded)");
        HouseholdTypeGenerator generator = new HouseholdTypeGenerator(dataSet);
        generator.createHouseHoldTypeDefinitionsForPurpose(strPurp);
        Map<Integer, HouseholdType> householdTypeBySampleId = generator.assignHouseholdTypeOfEachSurveyRecordForPurpose(strPurp);
        HashMap<String, Integer[]> tripsByHhTypeAndPurpose = generator.collectTripFrequencyDistributionForPurpose(householdTypeBySampleId, strPurp);
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
                if (dropThisTrip) counterDroppedTripsAtBorder++;
                if (dropThisTrip) continue;
                synchronized (currentTripId) {
                    currentTripId++;
                    MitoTrip trip = new MitoTrip(currentTripId, hh.getHhId(), purposeNum, tripOrigin);
                    dataSet.getTrips().put(trip.getTripId(), trip);
                    hh.addTrip(trip);
                }
            }
        }
    }

    private void logTripGeneration() {
        int rawTrips = dataSet.getTrips().size() + counterDroppedTripsAtBorder;
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (counterDroppedTripsAtBorder > 0)
            logger.info(MitoUtil.customFormat("  " + "###,###", counterDroppedTripsAtBorder) + " trips were dropped at boundary of study area.");
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

}
