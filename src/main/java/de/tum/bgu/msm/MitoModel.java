package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.InputFeed;
import de.tum.bgu.msm.io.input.InputManager;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.ResourceBundle;

/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 *         Created on Sep 18, 2016 in Munich, Germany
 *         <p>
 *         To run MITO, the following data need either to be passed in (using methods feedData) from another program or
 *         need to be read from files and passed in (using method initializeStandAlone):
 *         - zones:              public void setZones(int[] zones)
 *         - autoTravelTimes:    public void setAutoTravelTimes (Matrix autoTravelTimes)
 *         - transitTravelTimes: public void setTransitTravelTimes (Matrix transitTravelTimes)
 *         - timoHouseholds:     public void setHouseholds(MitoHousehold[] timoHouseholds)
 *         - retailEmplByZone:   public void setRetailEmplByZone(int[] retailEmplByZone)
 *         - officeEmplByZone:   public void setOfficeEmplByZone(int[] officeEmplByZone)
 *         - otherEmplByZone:    public void setOtherEmplByZone(int[] otherEmplByZone)
 *         - totalEmplByZone:    public void setTotalEmplByZone(int[] totalEmplByZone)
 *         - sizeOfZonesInAcre:  public void setSizeOfZonesInAcre(float[] sizeOfZonesInAcre)
 *         All other data are read by function  manager.readAdditionalData();
 */

public class MitoModel {

    private static Logger logger = Logger.getLogger(MitoModel.class);

    private final InputManager manager;

    private long startTime;
    private boolean initialized = false;

    private DataSet dataSet;

    public MitoModel(ResourceBundle resources) {
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
        Resources.INSTANCE.setResources(resources);
    }

    public void feedData(InputFeed feed) {
        if (!initialized) {
            manager.readFromFeed(feed);
            manager.readAdditionalData();
            initialized = true;
        } else {
            throw new RuntimeException("MitoModel was already initialized. Can only do this once either by feed or as standalone");
        }
    }

    public void initializeStandAlone() {
        if (!initialized) {
            // Read data if MITO is used as a stand-alone program and data are not fed from other program
            logger.info("  Reading input data for MITO");
            MitoUtil.initializeRandomNumber();
            manager.readAsStandAlone();
            manager.readAdditionalData();
            initialized = true;
        } else {
            throw new RuntimeException("MitoModel was already initialized. Can only do this once!");
        }
    }

    public void runModel() {
        startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        TravelDemandGenerator ttd = new TravelDemandGenerator(dataSet);
        ttd.generateTravelDemand();

        printOutline(startTime);
    }

    private void printOutline(long startTime) {
        String trips = MitoUtil.customFormat("  " + "###,###", dataSet.getTrips().size());
        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    public DataSet getTravelDemand() {
        return dataSet;
    }

    public void setBaseDirectory(String baseDirectory) {
        MitoUtil.setBaseDirectory(baseDirectory);
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }

    public void setScenarioName(String scenarioName) {
        dataSet.setScenarioName(scenarioName);
    }
}
