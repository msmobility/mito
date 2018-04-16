package de.tum.bgu.msm.io.output;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by matthewokrah on 12/03/2018.
 */
public class SummarizeDataToVisualize {
    private static final Logger logger = Logger.getLogger(SummarizeDataToVisualize.class);

    private static PrintWriter spatialResultWriter;
    private static PrintWriter spatialResultWriterFinal;

    private static PrintWriter resultWriter;
    private static PrintWriter resultWriterFinal;

    public static Boolean resultWriterReplicate = false;

    public static void resultFileSpatial(String action) {
        resultFileSpatial(action,true);
    }

    public static void resultFileSpatial(String action, Boolean writeFinal) {
        // handle summary file
        switch (action) {
            case "open":
                String directory = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/scenOutput";
                MitoUtil.createDirectoryIfNotExistingYet(directory);
                String resultFileName = Resources.INSTANCE.getString(Properties.SPATIAL_RESULT_FILE_NAME);
                spatialResultWriter = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +
                        ".csv", false);
                spatialResultWriterFinal = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +
                        "_final.csv", false);
                break;
            case "close":
                spatialResultWriter.close();
                spatialResultWriterFinal.close();
                break;
            default:
                spatialResultWriter.println(action);
                if(resultWriterReplicate && writeFinal)spatialResultWriterFinal.println(action);
                break;
        }
    }

    public static void resultFile(String action){
        resultFile(action, true);
    }

    public static void resultFile(String action, Boolean writeFinal){
        switch (action){
            case "open":
                String directory = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/scenOutput";
                MitoUtil.createDirectoryIfNotExistingYet(directory);
                String resultFileName = Resources.INSTANCE.getString(Properties.RESULT_FILE_NAME);
                resultWriter = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +
                        ".csv", false);
                resultWriterFinal = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +
                        "_final.csv", false);
                break;
            case "close":
                resultWriter.close();
                resultWriterFinal.close();
                break;
            default:
                resultWriter.println(action);
                if(resultWriterReplicate && writeFinal)resultWriterFinal.println(action);
                break;
        }
    }

    public static void summarizeSpatially (DataSet dataSet) {
        // write out results by zone
        resultFileSpatial("open");

        String hd = "Zone";
        for (Purpose purpose: Purpose.values()){
            hd = hd.concat("," + purpose + "P");
            hd = hd.concat("," + purpose + "A");
            hd = hd.concat("," + purpose + "TTB");
        }

        resultFileSpatial(hd);

        Map<Integer, Map<Purpose, Integer>> tripProdByZoneAndPurp = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Integer>> tripAttrByZoneAndPurp = new HashMap<>(dataSet.getZones().size());

        Map<Integer, Map<Purpose, Double>> avgBudgetByPurpAndZone = new HashMap<>(dataSet.getZones().size());
/*
        Map<MitoZone, List<MitoHousehold>> householdsByZone =
                dataSet.getHouseholds().values().stream().collect(Collectors.groupingBy(household -> household.getHomeZone()));

        Map<MitoZone, Map<Purpose, Double>> avgBudgetByPurpAndZone = new HashMap<>();
        for(Map.Entry<MitoZone, List<MitoHousehold>> entry: householdsByZone.entrySet()) {
            Map<Purpose, Double> budgetByPurpose = new HashMap<>();
            for(Purpose purpose: Purpose.values()) {
                List<MitoHousehold> households = entry.getValue();
                double averageBudget = households.stream().mapToDouble(
                        household -> household.getTravelTimeBudgetForPurpose(purpose)).average().getAsDouble();
                budgetByPurpose.put(purpose, averageBudget);
            }
            avgBudgetByPurpAndZone.put(entry.getKey(), budgetByPurpose);
        }
*/
        for(Integer zoneId: dataSet.getZones().keySet()) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            Map<Purpose, Double> initialDoubles = new HashMap<>(Purpose.values().length);
            for(Purpose purpose: Purpose.values()) {
                initialValues.put(purpose, 0);
                initialDoubles.put(purpose, 0.);
            }
            tripProdByZoneAndPurp.put(zoneId, initialValues);
            tripAttrByZoneAndPurp.put(zoneId, initialValues);
            avgBudgetByPurpAndZone.put(zoneId, initialDoubles);
        }

        for (MitoTrip trip: dataSet.getTrips().values()) {
            if(trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                Purpose purpose = trip.getTripPurpose();
                int origin = tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).get(purpose);
                tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).replace(purpose, (origin + 1));

                int destination = tripAttrByZoneAndPurp.get(trip.getTripDestination().getId()).get(purpose);
                tripAttrByZoneAndPurp.get(trip.getTripDestination().getId()).replace(purpose, (destination + 1));
            }
        }

        for (MitoHousehold household: dataSet.getHouseholds().values()){
            for (Purpose purpose: Purpose.values()){
                double budgetByPurpose = household.getTravelTimeBudgetForPurpose(purpose);
                Double oldBudget = avgBudgetByPurpAndZone.get(household.getHomeZone().getId()).get(purpose);
                if (oldBudget != 0){
                    avgBudgetByPurpAndZone.get(household.getHomeZone().getId()).replace(purpose, (oldBudget + budgetByPurpose)/2);
                } else {
                    avgBudgetByPurpAndZone.get(household.getHomeZone().getId()).replace(purpose, (budgetByPurpose));
                }
            }
        }

        for (int zoneId: dataSet.getZones().keySet()){
            //MitoZone zone = dataSet.getZones().get(zoneId);
            String txt = String.valueOf(zoneId);
            for (Purpose purpose: Purpose.values()){
                int tripsProduced = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                int tripsAttracted = tripAttrByZoneAndPurp.get(zoneId).get(purpose);
                double travelTimeBudget = avgBudgetByPurpAndZone.get(zoneId).get(purpose);
                //double travelTimeBudget = avgBudgetByPurpAndZone.get(dataSet.getZones().get(zoneId)).get(purpose);
                txt = txt.concat("," + tripsProduced + "," + tripsAttracted + "," + travelTimeBudget);
            }
            resultFileSpatial(txt);
        }

        resultFileSpatial("close");
    }

    public static void summarizeNonSpatially (DataSet dataSet) {

        resultFile("open");

        String hd = "Attribute";
        for (Purpose purpose: Purpose.values()){
            hd = hd.concat("," + purpose);
        }

        resultFile(hd);

        for (Mode mode: Mode.values()){
            String txt = "ModeShare_";
            txt = txt.concat(String.valueOf(mode));
            for (Purpose purpose: Purpose.values()){
                Double share = dataSet.getModeShareForPurpose(purpose, mode);

                txt = txt.concat("," + share);
            }
            resultFile(txt);
        }

        resultFile("close");

/*
        Map<Integer, Map<Purpose, Integer>> tripByPersonAndPurp = new HashMap<>(dataSet.getPersons().size());

        for(Integer personId: dataSet.getPersons().keySet()) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            for(Purpose purpose: Purpose.values()) {
                initialValues.put(purpose, 0);
            }
            tripByPersonAndPurp.put(personId, initialValues);
        }

        for (MitoTrip trip: dataSet.getTrips().values()) {
            Purpose purpose = trip.getTripPurpose();
            int person = tripByPersonAndPurp.get(trip.getPerson().getId()).get(purpose);
            tripByPersonAndPurp.get(trip.getPerson().getId()).replace(purpose, (person + 1));
        }

        int maxCount = tripByPersonAndPurp.get
        for (int zoneId: dataSet.getZones().keySet()){
            String txt = String.valueOf(zoneId);
            for (Purpose purpose: Purpose.values()){
                int tripsProduced = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                txt = txt.concat("," + tripsProduced);
            }
            resultFileSpatial(txt);
        }*/
    }

}
