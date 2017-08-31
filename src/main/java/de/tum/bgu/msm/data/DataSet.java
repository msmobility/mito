package de.tum.bgu.msm.data;

import com.pb.common.datafile.TableDataSet;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nico on 14.07.2017.
 */
public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);

    private TableDataSet travelSurveyHouseholdTable;
    private TableDataSet travelSurveyTripsTable;

    private TableDataSet tripAttractionRates;

    private TravelTimes autoTravelTimes;
    private TravelTimes transitTravelTimes;

    private final Map<Integer, Zone> zones= new LinkedHashMap<>();
    private final Map<Integer, MitoHousehold> households = new LinkedHashMap<>();
    private final Map<Integer, MitoPerson> persons = new LinkedHashMap<>();
    private final Map<Integer, MitoTrip> trips = new LinkedHashMap<>();

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


    public Map<Integer, Zone> getZones() {
        return Collections.unmodifiableMap(zones);
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return Collections.unmodifiableMap(households);
    }

    public Map<Integer, MitoPerson> getPersons() {
        return Collections.unmodifiableMap(persons);
    }

    public Map<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableMap(trips);
    }

    public synchronized void addZone(final Zone zone) {
        Zone test = this.zones.get(zone.getZoneId());
        if(test != null) {
            if(test.equals(zone)) {
                logger.warn("Zone " + zone.getZoneId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Zone id " + zone.getZoneId() + " already exists!");
        }
        zones.put(zone.getZoneId(), zone);
    }

    public synchronized void removeZone(final int zoneId) {
       zones.remove(zoneId);
    }

    public synchronized void addHousehold(final MitoHousehold household) {
        MitoHousehold test = this.households.get(household.getHhId());
        if(test != null) {
            if(test.equals(household)) {
                logger.warn("Household " + household.getHhId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Household id " + household.getHhId() + " already exists!");
        }
        households.put(household.getHhId(), household);
    }

    public void removeHousehold(final int householdId) {
        households.remove(householdId);
    }

    public synchronized void addPerson(final MitoPerson person) {
        MitoPerson test = this.persons.get(person.getId());
        if(test != null) {
            if(test.equals(person)) {
                logger.warn("Person " + person.getId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Person id " + person.getId() + " already exists!");
        }
        persons.put(person.getId(), person);
    }

    public synchronized void removePerson(final int personId) {
        persons.remove(personId);
    }

    public synchronized  void addTrip(final MitoTrip trip) {
        MitoTrip test = this.trips.get(trip.getTripId());
        if(test != null) {
            if(test.equals(trip)) {
                logger.warn("Trip " + trip.getTripId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Trip " +trip.getTripId() + " already exists!");
        }
        trips.put(trip.getTripId(), trip);
    }

    public synchronized void removeTrip(final int tripId) {
        trips.remove(tripId);
    }
}
