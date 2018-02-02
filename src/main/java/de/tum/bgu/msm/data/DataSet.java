package de.tum.bgu.msm.data;

import de.tum.bgu.msm.data.survey.SurveyRecord;
import de.tum.bgu.msm.data.survey.TravelSurvey;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataSet {

    private final Map<String, TravelTimes> travelTimes = new LinkedHashMap<>();

    private TravelDistances travelDistancesAuto;
    private TravelDistances travelDistancesNMT;

    private TravelSurvey<? extends SurveyRecord> survey;

    private double peakHour = Double.NaN;

    private final NavigableMap<Integer, MitoZone> zones= new TreeMap<>();
    private final NavigableMap<Integer, MitoHousehold> households = new TreeMap<>();
    private final NavigableMap<Integer, MitoPerson> persons = new TreeMap<>();
    private final NavigableMap<Integer, MitoTrip> trips = new TreeMap<>();


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

    public NavigableMap<Integer, MitoPerson> getPersons() {
        return Collections.unmodifiableNavigableMap(persons);
    }

    public NavigableMap<Integer, MitoZone> getZones() {
        return Collections.unmodifiableNavigableMap(zones);
    }

    public NavigableMap<Integer, MitoHousehold> getHouseholds() {
        return Collections.unmodifiableNavigableMap(households);
    }

    public NavigableMap<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableNavigableMap(trips);
    }

    public void addTrip(final MitoTrip trip) {
        MitoTrip test;
        synchronized (trips) {
            test = trips.putIfAbsent(trip.getId(), trip);
        }
        if(test != null) {
            throw new IllegalArgumentException("MitoTrip id " + trip.getId() + " already exists!");
        }
    }

    public void addZone(final MitoZone zone) {
        MitoZone test;
        synchronized (zones) {
            test = zones.putIfAbsent(zone.getId(), zone);
        }
        if(test != null) {
            throw new IllegalArgumentException("MitoZone id " + zone.getId() + " already exists!");
        }
    }

    public void addHousehold(final MitoHousehold household) {
        MitoHousehold test;
        synchronized (households) {
            test = households.putIfAbsent(household.getId(), household);
        }
        if(test != null) {
            throw new IllegalArgumentException("MitoHousehold id " + household.getId() + " already exists!");
        }
    }

    public void addPerson(final MitoPerson person) {
        MitoPerson test;
        synchronized (trips) {
            test = persons.putIfAbsent(person.getId(), person);
        }
        if(test != null) {
            throw new IllegalArgumentException("MitoPerson id " + person.getId() + " already exists!");
        }
    }

    public synchronized void removeTrip(final int tripId) {
        trips.remove(tripId);
    }

    public double getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(double peakHour) {
        this.peakHour = peakHour;
    }

    public static int getFemalesForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getGender().equals(Gender.FEMALE)).count();
    }

    public static int getChildrenForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() < 18).count();
    }

    public static int getYoungAdultsForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() >= 18 && person.getAge() <= 25).count();

    }

    public static int getRetireesForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getAge() > 65).count();
    }

    public static int getNumberOfWorkersForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getOccupation() == Occupation.WORKER).count();

    }

    public static int getStudentsForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(person ->
                person.getOccupation() == Occupation.STUDENT).count();

    }

    public static int getLicenseHoldersForHousehold(MitoHousehold household) {
        return (int) household.getPersons().values().stream().filter(MitoPerson::hasDriversLicense).count();
    }
}
