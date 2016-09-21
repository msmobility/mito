package de.tum.bgu.msm;

import com.pb.common.matrix.Matrix;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 * To run TIMO, the following data need either to be passed in (using methods feedData) from another program or
 * need to be read from files and passed in:
 * - zones:              public void setZones(int[] zones)
 * - autoTravelTimes:    public void setAutoTravelTimes (Matrix autoTravelTimes)
 * - transitTravelTimes: public void setTransitTravelTimes (Matrix transitTravelTimes)
 * - timoHouseholds:     public void setHouseholds(TimoHousehold[] timoHouseholds)
 * - retailEmplByZone:   public void setRetailEmplByZone(int[] retailEmplByZone)
 * - otherEmplByZone:    public void setOtherEmplByZone(int[] otherEmplByZone)
 * - totalEmplByZone:    public void setTotalEmplByZone(int[] totalEmplByZone)
 * - sizeOfZonesInAcre:  public void setSizeOfZonesInAcre(float[] sizeOfZonesInAcre)
 * All other data are read by function TimoData.readInputData().
 */

public class Timo {

    private static Logger logger = Logger.getLogger(Timo.class);
    private TimoData td;
    private ResourceBundle rb;

    public Timo(ResourceBundle rb) {
        this.rb = rb;
        td = new TimoData(rb);
    }

    public static void main(String[] args) {
        // main run method
        logger.warn("Stand-alone method for Timo not yet implemented. Call program with initialize() instead");
        //long startTime = System.currentTimeMillis();
        //logger.info("Started the Transport in Microsimulation Orchestrator (TIMO)");
        ResourceBundle rb = TimoUtil.createResourceBundle(args[0]);
    }


    public void feedData(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes, TimoHousehold[] timoHouseholds,
                         int[] retailEmplByZone, int[] otherEmplByZone, int[] totalEmplByZone, float[] sizeOfZonesInAcre) {
        // Feed data from other program. Need to write new methods to read these data from files if Timo is used as
        // stand-alone program.
        td.setZones(zones);                           // zone are stored consecutively starting at position 0
        td.setAutoTravelTimes(autoTravelTimes);
        td.setTransitTravelTimes(transitTravelTimes);
        td.setHouseholds(timoHouseholds);
        td.setRetailEmplByZone(retailEmplByZone);     // All employment and acre values are stored in the position of
        td.setOtherEmplByZone(otherEmplByZone);       // the zone ID. Position 0 will be empty, data for zone 1 is
        td.setTotalEmplByZone(totalEmplByZone);       // stored in position 1, for zone 5 in position 5, etc.
        td.setSizeOfZonesInAcre(sizeOfZonesInAcre);   //
    }


    public void run() {
        // initialize Timo from other program
        long startTime = System.currentTimeMillis();
        logger.info("Started the Transport in Microsimulation Orchestrator (TIMO)");

        // setup
        td.readInputData();

        // readSyntheticPopulation
        // todo: needs to read synthetic population if used as a stand-alone program

        // generate travel demand
        TimoTravelDemand ttd = new TimoTravelDemand(rb, td);
        ttd.generateTravelDemand();

        logger.info("Completed the Transport in Microsimulation Orchestrator (TIMO)");
        float endTime = TimoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }
}
