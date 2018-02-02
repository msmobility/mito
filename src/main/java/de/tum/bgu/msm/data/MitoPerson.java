package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */

public class MitoPerson implements Id{

    private static final Logger logger = Logger.getLogger(MitoPerson.class);

    private final int id;
    private final Gender gender;
    private final Occupation occupation;
    private int workplace;
    private MitoZone occupationZone;
    private final int age;
    private final boolean driversLicense;

    private Map<Integer,MitoTrip> trips = new HashMap<>();

    public MitoPerson(int id, Occupation occupation, int workplace, int age, Gender gender, boolean driversLicense) {
        this.id = id;
        this.occupation = occupation;
        this.workplace = workplace;
        this.age = age;
        this.gender = gender;
        this.driversLicense = driversLicense;
    }

    public int getWorkplace() {
        return workplace;
    }

    public void setOccupationZone(MitoZone occupationZone) {
        this.occupationZone = occupationZone;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public MitoZone getOccupationZone() {
        return occupationZone;
    }

    @Override
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

    public Map<Integer, MitoTrip> getTrips() {
        return Collections.unmodifiableMap(this.trips);
    }

    public void addTrip(MitoTrip trip) {
        MitoTrip test = this.trips.get(trip.getId());
        if(test != null) {
            if(test.equals(trip)) {
                logger.warn("Trip " + trip.getId() + "already exists in person " + this.getId());
            } else {
                throw new IllegalArgumentException("Trip id " + trip.getId() + " already exists in person " + this.getId());
            }
        }
        this.trips.put(trip.getId(), trip);
    }
}
