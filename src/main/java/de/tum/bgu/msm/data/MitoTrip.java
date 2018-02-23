package de.tum.bgu.msm.data;

/**
 * Holds trip objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Mar 26, 2017 in Munich, Germany
 *
 */

public class MitoTrip implements Id{

    private final int tripId;
    private final Purpose tripPurpose;

    private MitoZone tripOrigin;
    private MitoZone tripDestination;

    private MitoPerson person;

    private Mode tripMode;

    public MitoTrip(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    @Override
    public int getId() {
        return tripId;
    }

    public MitoZone getTripOrigin() {
        return tripOrigin;
    }

    public void setTripOrigin(MitoZone origin) {
        this.tripOrigin = origin;
    }

    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    public MitoZone getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(MitoZone destination) {
        this.tripDestination = destination;
    }

    public MitoPerson getPerson() {
        return person;
    }

    public void setPerson(MitoPerson person) {
        this.person = person;
    }

    public Mode getTripMode() { return tripMode; }

    public void setTripMode(Mode tripMode) { this.tripMode = tripMode; }

    @Override
    public String toString() {
        return "Trip [id: " + this.tripId + " purpose: " + this.tripPurpose + "]";
    }
}
