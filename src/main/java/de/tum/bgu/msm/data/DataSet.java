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

    private TableDataSet travelSurveyHouseholdTable;
    private TableDataSet travelSurveyTripsTable;

    private TableDataSet tripAttractionRates;

    private TravelTimes autoTravelTimes;
    private TravelTimes transitTravelTimes;

    private String[] purposes;
    private HashMap<String, Integer> purposeIndices;

    private final Map<Integer, Zone> zones= new HashMap<>();
    private final Map<Integer, MitoHousehold> households = new HashMap<>();
    private final Map<Integer, MitoPerson> persons = new HashMap<>();
    private final Map<Integer, MitoTrip> trips = new HashMap<>();

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

    public TravelTimes getAutoTravelTimes() {
        return this.autoTravelTimes;
    }

    public void setAutoTravelTimes(TravelTimes travelTimes) {
        this.autoTravelTimes = travelTimes;
    }

    public TravelTimes getTransitTravelTimes() {
        return this.transitTravelTimes;
    }

    public void setTransitTravelTimes(TravelTimes travelTimes) {
        this.transitTravelTimes = travelTimes;
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

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return households;
    }

    public Map<Integer, MitoPerson> getPersons() {
        return persons;
    }

    public Map<Integer, MitoTrip> getTrips() {
        return trips;
    }
}
