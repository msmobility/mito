package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */

public class MitoPerson {

    private static final Logger logger = Logger.getLogger(MitoPerson.class);

    private final int id;
    private final Gender gender;
    private Occupation occupation;
    private int workplace;
    private Zone workzone;
    private int age;
    private boolean driversLicense;

    private Map<Integer,MitoTrip> trips = new HashMap<>();

    public MitoPerson(int id, Occupation occupation, int workplace, int age, Gender gender, boolean driversLicense) {
        this.id = id;
        this.occupation = occupation;
        this.workplace = workplace;
        this.age = age;
        this.gender = gender;
        this.driversLicense = driversLicense;
    }

    public void setWorkplace(int workplace) {
        this.workplace = workplace;
    }

    public int getWorkplace() {
        return workplace;
    }

    public void setWorkzone(Zone workzone) {
        this.workzone = workzone;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public Zone getWorkzone() {
        return workzone;
    }

    public int getId() {
        return this.id;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean hasDriversLicense() {
        return driversLicense;
    }

    public void setDriversLicense(boolean driversLicense) {
        this.driversLicense = driversLicense;
    }

    public Map<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableMap(this.trips);
    }

    public void addTrip(MitoTrip trip) {
        MitoTrip test = this.trips.get(trip.getTripId());
        if(test != null) {
            if(test.equals(trip)) {
                logger.warn("Trip " + trip.getTripId() + "already exists in person " + this.getId());
            } else {
                throw new IllegalArgumentException("Trip id " + trip.getTripId() + " already exists in person " + this.getId());
            }
        }
        this.trips.put(trip.getTripId(), trip);
    }

    public void removeTrip(Integer tripId) {
        this.trips.remove(tripId);
    }
}
