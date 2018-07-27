package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by matthewokrah on 12/03/2018.
 */
public class SummarizeDataToVisualize {
    private static final Logger logger = Logger.getLogger(SummarizeDataToVisualize.class);
    private static PrintWriter spatialResultWriter;
    private static PrintWriter spatialResultWriterFinal;
    private static PrintWriter resultWriter;
    private static PrintWriter resultWriterFinal;
    private static Boolean resultWriterReplicate = false;

    /**
     * Handles the spatial result file along with the final spatial result file
     *
     * @param action a string to indicate the action to take on the spatial result file
     */
    private static void resultFileSpatial(String action) {
        resultFileSpatial(action, true);
    }

    /**
     * Handles the spatial result file with the option of handling the final spatial result file
     *
     * @param action     a string to indicate the action to take on the spatial result file
     * @param writeFinal a boolean to indicate whether to write to the final spatial result file
     */
    private static void resultFileSpatial(String action, Boolean writeFinal) {
        // handle summary file
        switch (action) {
            case "open":
                String directory = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/scenOutput";
                MitoUtil.createDirectoryIfNotExistingYet(directory);
                spatialResultWriter = MitoUtil.openFileForSequentialWriting(directory + "/" + "resultFileSpatial" +
                        ".csv", false);
                spatialResultWriterFinal = MitoUtil.openFileForSequentialWriting(directory + "/" + "resultFileSpatial" +
                        "_final.csv", false);
                break;
            case "close":
                spatialResultWriter.close();
                spatialResultWriterFinal.close();
                break;
            default:
                spatialResultWriter.println(action);
                if (resultWriterReplicate && writeFinal) spatialResultWriterFinal.println(action);
                break;
        }
    }

    /**
     * Handles the aspatial result file along with the final aspatial result file
     *
     * @param action a string to indicate the action to take on the result file
     */
    private static void resultFile(String action) {
        resultFile(action, true);
    }

    /**
     * Handles the aspatial result file with the option of handling the final aspatial result file
     *
     * @param action     a string to indicate the action to take on result file
     * @param writeFinal a boolean to indicate whether to write to the final result file
     */
    private static void resultFile(String action, Boolean writeFinal) {
        switch (action) {
            case "open":
                String directory = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/scenOutput";
                MitoUtil.createDirectoryIfNotExistingYet(directory);
                resultWriter = MitoUtil.openFileForSequentialWriting(directory + "/" + "resultFile" +
                        ".csv", false);
                resultWriterFinal = MitoUtil.openFileForSequentialWriting(directory + "/" + "resultFile" +
                        "_final.csv", false);
                break;
            case "close":
                resultWriter.close();
                resultWriterFinal.close();
                break;
            default:
                resultWriter.println(action);
                if (resultWriterReplicate && writeFinal) {
                    resultWriterFinal.println(action);
                }
                break;
        }
    }

    /**
     * Writes all summaries to their respective result files
     *
     * @param dataSet
     */
    public static void writeFinalSummary(DataSet dataSet) {
        // opening aspatial result file and writing out the header
        resultFile("open");
        String hdAspatial = "Attribute";
        for (Purpose purpose : Purpose.values()) {
            hdAspatial = hdAspatial.concat("," + purpose);
        }
        resultFile(hdAspatial);

        // opening spatial result file and writing out the header
        resultFileSpatial("open");
        String hdSpatial = "Zone";
        for (Purpose purpose : Purpose.values()) {
            hdSpatial = hdSpatial.concat("," + purpose + "P" + "," + purpose + "A" + "," + purpose + "AvDist" + "," + purpose + "AvTime" + "," + purpose + "TTB");
        }
        resultFileSpatial(hdSpatial);

        // initializing HashMaps to hold results
        Map<Integer, Map<Purpose, Integer>> distanceByPurpose = new HashMap<>();
        Map<Integer, Map<Purpose, Integer>> timeByPurpose = new HashMap<>();
        Map<Integer, Map<Purpose, Integer>> tripProdByZoneAndPurp = initializedIntMap(dataSet);
        Map<Integer, Map<Purpose, Double>> avDistByZoneAndPurp = initializedDoubleMap(dataSet);
        Map<Integer, Map<Purpose, Double>> avTimeByZoneAndPurp = initializedDoubleMap(dataSet);

        // Looping through trips to get trip level results
        for (MitoTrip trip : dataSet.getTrips().values()) {
            if (trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                Purpose purpose = trip.getTripPurpose();
                Integer tripOrigin = trip.getTripOrigin().getId();
                double rawDistance = dataSet.getTravelDistancesAuto().getTravelDistance(tripOrigin, trip.getTripDestination().getId());
                int refinedDistance = (int) Math.round(rawDistance);
                double rawTime = dataSet.getTravelTimes().getTravelTime(tripOrigin, trip.getTripDestination().getId(), dataSet.getPeakHour(), "car");
                int refinedTime = (int) Math.round(rawTime);

                // updating the intitalized HashMaps
                updateMap(distanceByPurpose, refinedDistance, purpose);
                updateMap(timeByPurpose, refinedTime, purpose);
                updateMap(tripProdByZoneAndPurp, tripOrigin, purpose);
                updateSpatialMap(avDistByZoneAndPurp, tripOrigin, purpose, rawDistance);
                updateSpatialMap(avTimeByZoneAndPurp, tripOrigin, purpose, rawTime);
            }
        }

        // writing out aspatial results
        writeAspatialSummary(distanceByPurpose, "Distance_");
        writeAspatialSummary(timeByPurpose, "Time_");
        summarizeModeChoice(dataSet);
        summarizeAtPersonLevel(dataSet);


        // writing out household level aspatial results and getting household level spatial results
        Map<Integer, Map<Purpose, Double>> avTTBudgetByZoneAndPurp = householdLevelSummary(dataSet);

        // writing out spatial results
        for (MitoZone zone : dataSet.getZones().values()) {
            final int zoneId = zone.getId();
            String txt = String.valueOf(zoneId);
            for (Purpose purpose : Purpose.values()) {
                int tripsProduced = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                int tripsAttracted = (int) zone.getTripAttraction(purpose);
                double avTripDist = avDistByZoneAndPurp.get(zoneId).get(purpose) / tripsProduced;
                double avTripTime = avTimeByZoneAndPurp.get(zoneId).get(purpose) / tripsProduced;
                double avTTBudget = avTTBudgetByZoneAndPurp.get(zoneId).get(purpose);
                txt = txt.concat("," + tripsProduced + "," + tripsAttracted + "," + avTripDist + "," + avTripTime + "," + avTTBudget);
            }
            resultFileSpatial(txt);
        }

        // closing all result files
        resultFile("close");
        resultFileSpatial("close");
    }

    /**
     * Writes out aspatial results to result file
     *
     * @param map    a HashMap containing the aspatial results
     * @param prefix a string indicating the aspatial attribute
     */
    private static void writeAspatialSummary(Map<Integer, Map<Purpose, Integer>> map, String prefix) {
        for (int i : map.keySet()) {
            String txt = prefix.concat(String.valueOf(i));
            for (Purpose purpose : Purpose.values()) {
                Integer trips = map.get(i).get(purpose);
                txt = txt.concat("," + trips);
            }
            resultFile(txt);
        }
    }

    /**
     * Updates a HashMap by adding 1 to the existing count
     *
     * @param map     the HashMap to be updated
     * @param key     the attribute to be used as the key of the HashMap
     * @param purpose the trip purpose
     */
    private static void updateMap(Map<Integer, Map<Purpose, Integer>> map, Integer key, Purpose purpose) {
        if (map.containsKey(key)) {
            if (map.get(key).containsKey(purpose)) {
                int existingCount = map.get(key).get(purpose);
                map.get(key).replace(purpose, existingCount + 1);
            } else {
                map.get(key).put(purpose, 1);
            }
        } else {
            Map<Purpose, Integer> subMap = new HashMap<>();
            subMap.put(purpose, 1);
            map.put(key, subMap);
        }
    }

    /**
     * Updates a HashMap whose key is a zone by adding the current value of an attribute to the existing value in the HashMap
     *
     * @param map          the HashMap to be updated
     * @param key          the zone to be used as the key of the HashMap
     * @param purpose      the trip purpose
     * @param currentValue the current value of the attribute
     */
    private static void updateSpatialMap(Map<Integer, Map<Purpose, Double>> map, Integer key, Purpose purpose, Double currentValue) {
        double existingValue = map.get(key).get(purpose);
        map.get(key).replace(purpose, (currentValue + existingValue));
    }

    /**
     * Summarizes mode choice results
     *
     * @param dataSet
     */
    private static void summarizeModeChoice(DataSet dataSet) {
        for (Mode mode : Mode.values()) {
            String txt = "ModeShare_";
            txt = txt.concat(String.valueOf(mode));
            for (Purpose purpose : Purpose.values()) {
                Double share = dataSet.getModeShareForPurpose(purpose, mode);
                txt = txt.concat("," + share);
            }
            resultFile(txt);
        }
    }

    /**
     * Summarizes results at person level
     *
     * @param dataSet
     */
    private static void summarizeAtPersonLevel(DataSet dataSet) {
        Map<Integer, Map<Purpose, Integer>> personsByTripsAndPurpose = new HashMap<>();
        for (MitoPerson person : dataSet.getPersons().values()) {
            Map<Purpose, Integer> trips = new HashMap<>(Purpose.values().length);
            for (Purpose purpose : Purpose.values()) {
                trips.put(purpose, 0);
            }
            Set<MitoTrip> personTrips = person.getTrips();
            for (MitoTrip trip : personTrips) {
                Purpose purpose = trip.getTripPurpose();
                int existingTripCount = trips.get(purpose);
                trips.put(purpose, existingTripCount + 1);
            }
            for (Purpose purpose : Purpose.values()) {
                int tripsByPurpose = trips.get(purpose);
                updateMap(personsByTripsAndPurpose, tripsByPurpose, purpose);
            }
        }
        writeAspatialSummary(personsByTripsAndPurpose, "PPbyTrips_");
    }

    /**
     * Initializes a HashMap with a size equal to the total number of zones and pre-fills it with 0s
     *
     * @param dataSet
     * @return the pre-filled HashMap
     */
    private static Map<Integer, Map<Purpose, Integer>> initializedIntMap(DataSet dataSet) {
        Map<Integer, Map<Purpose, Integer>> map = new HashMap<>(dataSet.getZones().size());
        for (Integer zoneId : dataSet.getZones().keySet()) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            for (Purpose purpose : Purpose.values()) {
                initialValues.put(purpose, 0);
            }
            map.put(zoneId, initialValues);
        }
        return map;
    }

    /**
     * Initializes a HashMap with a size equal to the total number of zones and pre-fills it with 0s
     *
     * @param dataSet
     * @return the pre-filled HashMap
     */
    private static Map<Integer, Map<Purpose, Double>> initializedDoubleMap(DataSet dataSet) {
        Map<Integer, Map<Purpose, Double>> map = new HashMap<>(dataSet.getZones().size());
        for (Integer zoneId : dataSet.getZones().keySet()) {
            Map<Purpose, Double> initialValues = new HashMap<>(Purpose.values().length);
            for (Purpose purpose : Purpose.values()) {
                initialValues.put(purpose, 0.);
            }
            map.put(zoneId, initialValues);
        }
        return map;
    }

    /**
     * Summarizes results at the household level
     *
     * @param dataSet
     * @return a hashMap containing the average travel time budget by zone and purpose
     */
    private static Map<Integer, Map<Purpose, Double>> householdLevelSummary(DataSet dataSet) {
        Map<Integer, Map<Purpose, Integer>> householdsByTripsAndPurpose = new HashMap<>();
        Map<Integer, Map<Purpose, Double>> totalTTBudgetByZoneAndPurpose = initializedDoubleMap(dataSet);
        Map<Integer, Map<Purpose, Integer>> ttBudgetCounterByZoneAndPurpose = initializedIntMap(dataSet);
        Map<Integer, Map<Purpose, Double>> avTTBudgetByZoneAndPurpose = initializedDoubleMap(dataSet);
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            Integer homeZone = household.getHomeZone().getId();
            for (Purpose purpose : Purpose.values()) {
                int tripsByPurpose = household.getTripsForPurpose(purpose).size();
                updateMap(householdsByTripsAndPurpose, tripsByPurpose, purpose);

                double budget = household.getTravelTimeBudgetForPurpose(purpose);
                updateSpatialMap(totalTTBudgetByZoneAndPurpose, homeZone, purpose, budget);
                updateMap(ttBudgetCounterByZoneAndPurpose, homeZone, purpose);
            }
        }
        for (MitoZone zone : dataSet.getZones().values()) {
            final int zoneId = zone.getId();
            for (Purpose purpose : Purpose.values()) {
                double total = totalTTBudgetByZoneAndPurpose.get(zoneId).get(purpose);
                int count = ttBudgetCounterByZoneAndPurpose.get(zoneId).get(purpose);
                avTTBudgetByZoneAndPurpose.get(zoneId).replace(purpose, total / count);
            }
        }
        writeAspatialSummary(householdsByTripsAndPurpose, "HHbyTrips_");
        return avTTBudgetByZoneAndPurpose;
    }
}
