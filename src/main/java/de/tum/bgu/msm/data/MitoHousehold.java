package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds households objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 21, 2016 in Munich, Germany
 *
 */

public class MitoHousehold implements Serializable {
    static Logger logger = Logger.getLogger(MitoHousehold.class);

    private int hhId;
    private int hhSize;
    private int numberOfWorkers;
    private int income;
    private int autos;
    private int homeZone;
    private MitoTrip[] trips;
    private static final Map<Integer,MitoHousehold> householdMap = new HashMap<>();


    public MitoHousehold(int id, int hhSize, int numberOfWorkers, int income, int autos, int homeZone) {
        // create new MitoHousehold
        this.hhId = id;
        this.hhSize = hhSize;
        this.numberOfWorkers = numberOfWorkers;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
        this.trips = new MitoTrip[0];
        householdMap.put(id, this);
    }


    public static MitoHousehold getHouseholdFromId (int id) {
        return householdMap.get(id);
    }

    public static MitoHousehold[] getHouseholdArray() {
        return householdMap.values().toArray(new MitoHousehold[householdMap.size()]);
    }


    public int getHhId() {
        return hhId;
    }

    public int getHhSize() {
        return hhSize;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(int workers) {
        numberOfWorkers = workers;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int inc) {
        income = inc;
    }

    public int getAutos() {
        return autos;
    }

    public int getHomeZone() {
        return homeZone;
    }

    public void addTrip(MitoTrip trip) {
        MitoTrip[] newTrips = new MitoTrip[trips.length + 1];
        System.arraycopy(trips, 0, newTrips, 0, trips.length);
        newTrips[trips.length] = trip;
        trips = newTrips;
    }
}
