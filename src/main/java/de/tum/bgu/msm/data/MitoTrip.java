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
    private final int householdId;
    private final int tripPurpose;

    private final int tripOrigin;
    private int tripDestination;

    public MitoTrip(int tripId, int householdId, int tripPurpose, int origin) {
        this.tripId = tripId;
        this.householdId = householdId;
        this.tripPurpose = tripPurpose;
        this.tripOrigin = origin;
    }

    public int getTripId() {
        return tripId;
    }

    public int getTripOrigin() {
        return tripOrigin;
    }

    public int getTripPurpose() {
        return tripPurpose;
    }

    public int getHouseholdId() {
        return this.householdId;
    }

    public int getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(int destination) {
        this.tripDestination = destination;
    }
}
