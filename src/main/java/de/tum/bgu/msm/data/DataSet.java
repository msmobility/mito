package de.tum.bgu.msm.data;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.matrix.Matrix;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 14.07.2017.
 */
public class DataSet {

    private static Logger logger = Logger.getLogger(DataSet.class);
    private static String scenarioName;

    private TableDataSet travelSurveyHouseholdTable;
    private TableDataSet travelSurveyTripsTable;

    private TableDataSet tripAttractionRates;

    private Matrix autoTravelTimes;
    private Matrix transitTravelTimes;
    private Matrix distanceMatrix;

    private String[] purposes;
    private HashMap<String, Integer> purposeIndices;

    private Map<Integer, Zone> zones;
    private Map<Integer, MitoHousehold> households;
    private Map<Integer, MitoPerson> persons;

    private final TripDataManager tripDataManager;

    public DataSet() {
        this.tripDataManager = new TripDataManager();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        DataSet.logger = logger;
    }

    public static String getScenarioName() {
        return scenarioName;
    }

    public static void setScenarioName(String scenarioName) {
        DataSet.scenarioName = scenarioName;
    }

    public TableDataSet getTravelSurveyHouseholdTable() {
        return travelSurveyHouseholdTable;
    }

    public void setTravelSurveyHouseholdTable(TableDataSet travelSurveyHouseholdTable) {
        this.travelSurveyHouseholdTable = travelSurveyHouseholdTable;
    }

    public TableDataSet getTravelSurveyTripsTable() {
        return travelSurveyTripsTable;
    }

    public void setTravelSurveyTripsTable(TableDataSet travelSurveyTripsTable) {
        this.travelSurveyTripsTable = travelSurveyTripsTable;
    }

    public TableDataSet getTripAttractionRates() {
        return tripAttractionRates;
    }

    public void setTripAttractionRates(TableDataSet tripAttractionRates) {
        this.tripAttractionRates = tripAttractionRates;
    }

    public float getAutoTravelTimeFromTo(int origin, int destination) {
        return autoTravelTimes.getValueAt(origin, destination);
    }

    public void setAutoTravelTimes(Matrix autoTravelTimes) {
        this.autoTravelTimes = autoTravelTimes;
    }

    public float getTransitTravelTimedFromTo(int origin, int destination) {
        return transitTravelTimes.getValueAt(origin, destination);
    }

    public void setTransitTravelTimes(Matrix transitTravelTimes) {
        this.transitTravelTimes = transitTravelTimes;
    }

    public String[] getPurposes() {
        return purposes;
    }

    public void setPurposes(String[] purposes) {
        this.purposes = purposes;
        purposeIndices = new HashMap<>();
        for (int i = 0; i < purposes.length; i++) {
            purposeIndices.put(purposes[i], i);
        }
    }

    public int getPurposeIndex(String purpose) {
        return purposeIndices.get(purpose);
    }

    public void setDistanceMatrix(Matrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public void setZones(Map<Integer, Zone> zones) {
        this.zones = zones;
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return households;
    }

    public void setHouseholds(Map<Integer, MitoHousehold> households) {
        this.households = households;
    }

    public Map<Integer, MitoPerson> getPersons() {
        return persons;
    }

    public void setPersons(Map<Integer, MitoPerson> persons) {
        this.persons = persons;
    }

    public TripDataManager getTripDataManager() {
        return tripDataManager;
    }
}
