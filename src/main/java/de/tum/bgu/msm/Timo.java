package de.tum.bgu.msm;

import com.pb.common.matrix.Matrix;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
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


    public void feedData(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes) {
        // Feed data from other program. Need to write new methods to read these data if Timo is used as stand-alone program.
        td.setZones(zones);
        td.setAutoTravelTimes(autoTravelTimes);
        td.setTransitTravelTimes(transitTravelTimes);
    }


    public void run() {
        // initialize Timo from other program
        long startTime = System.currentTimeMillis();
        logger.info("Started the Transport in Microsimulation Orchestrator (TIMO)");

        // setup
        td.readInputData();
        TimoAccessibility ta = new TimoAccessibility(rb, td);
        ta.calculateAccessibilities();

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
