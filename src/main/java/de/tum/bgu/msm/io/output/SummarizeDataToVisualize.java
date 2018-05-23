package de.tum.bgu.msm.io.output;

import com.google.common.math.Stats;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.*;

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
            hd = hd.concat("," + purpose + "P" + "," + purpose + "A" + "," + purpose + "AvDist" + "," + purpose + "AvTime" + "," + purpose + "TTB");
        }
        resultFileSpatial(hd);

        Map<Integer, Map<Purpose, Integer>> tripProdByZoneAndPurp = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Double>> avDistByZoneAndPurp = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Double>> avTimeByZoneAndPurp = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Double>> avTTBudgetByZoneAndPurp = new HashMap<>(dataSet.getZones().size());

        Map<Integer, Map<Purpose, Integer>> avDistByZoneAndPurpCount = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Integer>> avTimeByZoneAndPurpCount = new HashMap<>(dataSet.getZones().size());
        Map<Integer, Map<Purpose, Integer>> avTTBudgetByZoneAndPurpCount = new HashMap<>(dataSet.getZones().size());



        for(Integer zoneId: dataSet.getZones().keySet()) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            Map<Purpose, Double> initialDoubles = new HashMap<>(Purpose.values().length);
            for(Purpose purpose: Purpose.values()) {
                initialValues.put(purpose, 0);
                initialDoubles.put(purpose, 0.);
            }
            tripProdByZoneAndPurp.put(zoneId, initialValues);
            avDistByZoneAndPurp.put(zoneId, initialDoubles);
            avTimeByZoneAndPurp.put(zoneId, initialDoubles);
            avTTBudgetByZoneAndPurp.put(zoneId, initialDoubles);

            avDistByZoneAndPurpCount.put(zoneId, initialValues);
            avTimeByZoneAndPurpCount.put(zoneId, initialValues);
            avTTBudgetByZoneAndPurpCount.put(zoneId, initialValues);
        }

        for (MitoTrip trip: dataSet.getTrips().values()) {
            if(trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                Purpose purpose = trip.getTripPurpose();

                int number = tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).get(purpose);
                tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).replace(purpose, (number + 1));

                double distance = dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getId(), trip.getTripDestination().getId());
                double existingDistance = avDistByZoneAndPurp.get(trip.getTripOrigin().getId()).get(purpose);
                avDistByZoneAndPurp.get(trip.getTripOrigin().getId()).replace(purpose, (distance + existingDistance));

                int distCount = avDistByZoneAndPurpCount.get(trip.getTripOrigin().getId()).get(purpose);
                avDistByZoneAndPurpCount.get(trip.getTripOrigin().getId()).replace(purpose, (distCount + 1));


                //double travelTime = dataSet.getTravelTimes(String.valueOf(trip.getTripMode())).getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour());
                double travelTime = dataSet.getTravelTimes("car").getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), dataSet.getPeakHour());
                double existingTime = avTimeByZoneAndPurp.get(trip.getTripOrigin().getId()).get(purpose);
                avTimeByZoneAndPurp.get(trip.getTripOrigin().getId()).replace(purpose, (travelTime + existingTime));

                int timeCount = avTimeByZoneAndPurpCount.get(trip.getTripOrigin().getId()).get(purpose);
                avTimeByZoneAndPurpCount.get(trip.getTripOrigin().getId()).replace(purpose, (timeCount + 1));

            }
        }

        for (MitoHousehold household: dataSet.getHouseholds().values()){
            for (Purpose purpose: Purpose.values()){
                double budget = household.getTravelTimeBudgetForPurpose(purpose);
                double existingBudget = avTTBudgetByZoneAndPurp.get(household.getHomeZone().getId()).get(purpose);
                avTTBudgetByZoneAndPurp.get(household.getHomeZone().getId()).replace(purpose, (budget + existingBudget));

                int budgetCount = avTTBudgetByZoneAndPurpCount.get(household.getHomeZone().getId()).get(purpose);
                avTTBudgetByZoneAndPurpCount.get(household.getHomeZone().getId()).replace(purpose, (budgetCount + 1));

            }
        }

        for (MitoZone zone: dataSet.getZones().values()) {
            final int zoneId = zone.getId();
            String txt = String.valueOf(zoneId);
            for (Purpose purpose: Purpose.values()){
                int tripsProduced = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                int tripsAttracted = (int)zone.getTripAttraction(purpose);
                double tripDist = avDistByZoneAndPurp.get(zoneId).get(purpose) / avDistByZoneAndPurpCount.get(zoneId).get(purpose);
                double tripTime = avTimeByZoneAndPurp.get(zoneId).get(purpose) / avTimeByZoneAndPurpCount.get(zoneId).get(purpose);
                double ttBudget = avTTBudgetByZoneAndPurp.get(zoneId).get(purpose) / avTTBudgetByZoneAndPurpCount.get(zoneId).get(purpose) ;
                txt = txt.concat("," + tripsProduced + "," + tripsAttracted + "," + tripDist + "," + tripTime + "," + ttBudget);
            }
            resultFileSpatial(txt);
        }
        resultFileSpatial("close");
    }

//        Map<MitoZone, Long> result = dataSet.getTrips().values().stream()
//                .filter(trip ->
//                        trip.getTripPurpose() == purpose && trip.getTripOrigin() != null && trip.getTripDestination() != null)
//                .collect(Collectors.groupingBy(MitoTrip::getTripOrigin, Collectors.counting()));

//        dataSet.getTrips().values().stream()
//                .filter(trip ->
//                        trip.getTripPurpose() == purpose && trip.getTripOrigin() != null && trip.getTripDestination() != null)
//                .collect(Collectors.groupingBy(MitoTrip::getTripOrigin, Collectors.averagingDouble(new ToDoubleFunction<MitoTrip>() {
//                    @Override
//                    public double applyAsDouble(MitoTrip value) {
//                        return dataSet.getTravelTimes().get("car").getTravelTime(value.getTripOrigin().getId(), value.getTripDestination().getId(), 8*60*60);
//                    }
//                })));


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

        Map<Integer, Map<Purpose, Integer>> myhouseholdsByTripsAndPurp = new HashMap<>();
        Map<Integer, Map<Purpose, Integer>> householdsByTripsAndPurp = new HashMap<>();
        Map<Integer, Map<Purpose, Integer>> personsByTripsAndPurp = new HashMap<>();

        for(int i = 0; i < 30; i++) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            for(Purpose purpose: Purpose.values()) {
                initialValues.put(purpose, 0);
            }
            myhouseholdsByTripsAndPurp.put(i, initialValues);
            householdsByTripsAndPurp.put(i, initialValues);
            personsByTripsAndPurp.put(i, initialValues);
        }

        for (MitoPerson person : dataSet.getPersons().values()) {
            Map<Purpose, Integer> trips = new HashMap<>(Purpose.values().length);
            for (Purpose purpose : Purpose.values()){
                trips.put(purpose, 0);
            }
            Set<MitoTrip> personTrips = person.getTrips();
            for (MitoTrip trip : personTrips) {
                Purpose purpose = trip.getTripPurpose();
                int existingCount = trips.get(purpose);
                trips.put(purpose, existingCount + 1);
            }
            for (Purpose purpose: Purpose.values()){
                int tripsByPurp = trips.get(purpose);
                int existingCount = personsByTripsAndPurp.get(tripsByPurp).get(purpose);
                personsByTripsAndPurp.get(tripsByPurp).replace(purpose, (existingCount + 1));
            }
        }

        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            Map<Purpose, Integer> trips = new HashMap<>(Purpose.values().length);
            for (Purpose purpose : Purpose.values()){
                trips.put(purpose, 0);
            }
            //Map<Integer, MitoPerson> householdPersons = household.getPersons();
            for (MitoPerson person: household.getPersons().values()) {
                Set<MitoTrip> personTrips = person.getTrips();

                for (MitoTrip trip : personTrips) {
                    Purpose purpose = trip.getTripPurpose();
                    int existingCount = trips.get(purpose);
                    trips.put(purpose, existingCount + 1);
                }
            }

            for (Purpose purpose: Purpose.values()){
                int tripsByPurp = trips.get(purpose);
                int existingCount = myhouseholdsByTripsAndPurp.get(tripsByPurp).get(purpose);
                myhouseholdsByTripsAndPurp.get(tripsByPurp).replace(purpose, (existingCount + 1));
            }
        }


        for (MitoHousehold household: dataSet.getHouseholds().values()){
            for (Purpose purpose: Purpose.values()){
                int tripsByPurp = household.getTripsForPurpose(purpose).size();
                int existingCount = householdsByTripsAndPurp.get(tripsByPurp).get(purpose);
                householdsByTripsAndPurp.get(tripsByPurp).replace(purpose, (existingCount + 1) );
            }
        }

        for (int i = 0; i < 30; i++) {
            String txt = "HHbyTrips_";
            txt = txt.concat(String.valueOf(i));
            for (Purpose purpose: Purpose.values()){
                Integer trips = householdsByTripsAndPurp.get(i).get(purpose);

                txt = txt.concat("," + trips);
            }
            resultFile(txt);
        }

        for (int i = 0; i < 30; i++) {
            String txt = "PPbyTrips_";
            txt = txt.concat(String.valueOf(i));
            for (Purpose purpose: Purpose.values()){
                Integer trips = personsByTripsAndPurp.get(i).get(purpose);

                txt = txt.concat("," + trips);
            }
            resultFile(txt);
        }

        for (int i = 0; i < 30; i++) {
            String txt = "myHHbyTrips_";
            txt = txt.concat(String.valueOf(i));
            for (Purpose purpose: Purpose.values()){
                Integer trips = myhouseholdsByTripsAndPurp.get(i).get(purpose);

                txt = txt.concat("," + trips);
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

