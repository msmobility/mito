package de.tum.bgu.msm.data;

import java.io.Serializable;

/**
 * Holds trip objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Mar 26, 2017 in Munich, Germany
 *
 */

public class MitoTrip implements Serializable {

    private final int tripId;
    private final Purpose tripPurpose;

    private Zone tripOrigin;
    private Zone tripDestination;

    private MitoPerson person;

    public MitoTrip(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    public int getTripId() {
        return tripId;
    }

    public Zone getTripOrigin() {
        return tripOrigin;
    }

    public void setTripOrigin(Zone origin) {
        this.tripOrigin = origin;
    }

    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    public Zone getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(Zone destination) {
        this.tripDestination = destination;
    }

    public MitoPerson getPerson() {
        return person;
    }

    public void setPerson(MitoPerson person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return "Trip [id: " + this.tripId + " purpose: " + this.tripPurpose + "]";
    }
}
