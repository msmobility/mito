package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

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
    private int workplace; //TODO change the name "workplace" to be more clear Qin 21' Jun
    private MitoZone occupationZone;
    private final int age;
    private final boolean driversLicense;
    private Coord occupationCoord; //jobLocation or schoolLocation
    private Set<MitoTrip> trips = new LinkedHashSet<>();

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

    public Set<MitoTrip> getTrips() {
        return Collections.unmodifiableSet(this.trips);
    }

    public void addTrip(MitoTrip trip) {
        this.trips.add(trip);
        if(trip.getPerson() != this) {
            trip.setPerson(this);
        }
    }

    public Coord getOccupationCoord() {
        return occupationCoord;
    }

    public void setOccupationCoord(Coord occupationCoord) {
        this.occupationCoord = occupationCoord;
    }
}
