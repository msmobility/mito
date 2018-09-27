package de.tum.bgu.msm.data;

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */

public class MitoPerson implements Id {

    private static final Logger logger = Logger.getLogger(MitoPerson.class);

    private final int id;
    private final MitoGender mitoGender;
    private final MitoOccupation mitoOccupation;
    private int workplace; //TODO change the name "workplace" to be more clear Qin 21' Jun
    private MitoZone occupationZone;
    private Coordinate occupationLocation; //jobLocation or schoolLocation
    private final int age;
    private final boolean driversLicense;

    private Set<MitoTrip> trips = new LinkedHashSet<>();

    public MitoPerson(int id, MitoOccupation mitoOccupation, int workplace, int age, MitoGender mitoGender, boolean driversLicense) {
        this.id = id;
        this.mitoOccupation = mitoOccupation;
        this.workplace = workplace;
        this.age = age;
        this.mitoGender = mitoGender;
        this.driversLicense = driversLicense;
    }

    public int getWorkplace() {
        return workplace;
    }

    public void setOccupationZone(MitoZone occupationZone) {
        this.occupationZone = occupationZone;
    }

    public MitoOccupation getMitoOccupation() {
        return mitoOccupation;
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

    public MitoGender getMitoGender() {
        return mitoGender;
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

    public Coordinate getOccupationLocation() {
        return occupationLocation;
    }

    public void setOccupationLocation(Coordinate occupationLocation) {
        this.occupationLocation = occupationLocation;
    }
}
