package de.tum.bgu.msm;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds households objects for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 21, 2016 in Munich, Germany
 *
 */

public class TimoHousehold implements Serializable {
    static Logger logger = Logger.getLogger(TimoHousehold.class);

    private int hhId;
    private int hhSize;
    private int numberOfWorkers;
    private int income;
    private int autos;
    private int homeZone;
    private int[] tripsByPurpose;
    private int[] nonMotorizedTripsByPurpose;


    public TimoHousehold(int id, int hhSize, int numberOfWorkers, int income, int autos, int homeZone) {
        // create new TimoHousehold
        this.hhId = id;
        this.hhSize = hhSize;
        this.numberOfWorkers = numberOfWorkers;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
    }

    public void createTripByPurposeArray (int numberOfPurposes) {
        this.tripsByPurpose = new int[numberOfPurposes];
        this.nonMotorizedTripsByPurpose = new int[numberOfPurposes];
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

    public int getIncome() {
        return income;
    }

    public int getAutos() {
        return autos;
    }

    public int getHomeZone() {
        return homeZone;
    }

    public void setNumberOfTrips (int purposeNumber, int numberOfTrips) {
        // note: if non-motorized trips are calculated, this variable stores motorized trips only; otherwise, this
        //       variable stores total trips
        tripsByPurpose[purposeNumber] = numberOfTrips;
    }

    public int getNumberOfTrips (int purposeNumber) {
        return tripsByPurpose[purposeNumber];
    }

    public void setNonMotorizedNumberOfTrips (int purposeNumber, int numberOfNonMotorizedTrips) {
        nonMotorizedTripsByPurpose[purposeNumber] = numberOfNonMotorizedTrips;
    }

}
