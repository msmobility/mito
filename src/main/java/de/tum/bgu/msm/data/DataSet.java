package de.tum.bgu.msm.data;

import de.tum.bgu.msm.data.survey.SurveyRecord;
import de.tum.bgu.msm.data.survey.TravelSurvey;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);

    private final Map<String, TravelTimes> travelTimes = new LinkedHashMap<>();
    private TravelDistances travelDistancesAuto;
    private TravelDistances travelDistancesNMT;

    private TravelSurvey<? extends SurveyRecord> survey;

    private final Map<Integer, Zone> zones= new LinkedHashMap<>();
    private final Map<Integer, MitoHousehold> households = new LinkedHashMap<>();
    private final Map<Integer, MitoPerson> persons = new LinkedHashMap<>();
    private final Map<Integer, MitoTrip> trips = new ConcurrentHashMap<>();

    public TravelSurvey<? extends SurveyRecord> getSurvey() {
        return this.survey;
    }

    public void setSurvey(TravelSurvey<? extends SurveyRecord> survey) {
        this.survey = survey;
    }

    public TravelDistances getTravelDistancesAuto(){return this.travelDistancesAuto;}
    public TravelDistances getTravelDistancesNMT(){return this.travelDistancesNMT;}

    public void setTravelDistancesAuto(TravelDistances travelDistancesAuto){this.travelDistancesAuto = travelDistancesAuto;}
    public void setTravelDistancesNMT(TravelDistances travelDistancesNMT){this.travelDistancesNMT = travelDistancesNMT;}

    public Map<String, TravelTimes> getTravelTimes() {
        return Collections.unmodifiableMap(travelTimes);
    }

    public TravelTimes getTravelTimes(String mode) {
        return this.travelTimes.get(mode);
    }

    public TravelTimes addTravelTimeForMode(String mode, TravelTimes travelTimes) {
        return this.travelTimes.put(mode, travelTimes);
    }

    public Map<Integer, MitoPerson> getPersons() {
        return Collections.unmodifiableMap(persons);
    }

    public Map<Integer, Zone> getZones() {
        return Collections.unmodifiableMap(zones);
    }

    public Map<Integer, MitoHousehold> getHouseholds() {
        return Collections.unmodifiableMap(households);
    }

    public Map<Integer, MitoTrip> getTrips() {
        return trips;
    }

    public void addZone(final Zone zone) {
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

    public synchronized void removeTrip(final int tripId) {
        trips.remove(tripId);
    }
}
