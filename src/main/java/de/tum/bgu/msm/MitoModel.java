package de.tum.bgu.msm;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.InputManager;
import de.tum.bgu.msm.modules.TravelTimeBudget;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.ResourceBundle;

/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 * To run MITO, the following data need either to be passed in (using methods feedData) from another program or
 * need to be read from files and passed in (using method initializeStandAlone):
 * - zones:              public void setZones(int[] zones)
 * - autoTravelTimes:    public void setAutoTravelTimes (Matrix autoTravelTimes)
 * - transitTravelTimes: public void setTransitTravelTimes (Matrix transitTravelTimes)
 * - timoHouseholds:     public void setHouseholds(MitoHousehold[] timoHouseholds)
 * - retailEmplByZone:   public void setRetailEmplByZone(int[] retailEmplByZone)
 * - officeEmplByZone:   public void setOfficeEmplByZone(int[] officeEmplByZone)
 * - otherEmplByZone:    public void setOtherEmplByZone(int[] otherEmplByZone)
 * - totalEmplByZone:    public void setTotalEmplByZone(int[] totalEmplByZone)
 * - sizeOfZonesInAcre:  public void setSizeOfZonesInAcre(float[] sizeOfZonesInAcre)
 * All other data are read by function  manager.readAdditionalData();
 */

public class MitoModel {

    private static Logger logger = Logger.getLogger(MitoModel.class);
    private TravelTimeBudget ttbModel;
    private final InputManager manager;
    private final ResourceBundle resources;

    private DataSet dataSet;

    public MitoModel(ResourceBundle resources) {
        this.resources = resources;
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
        Resources.INSTANCE.setResources(resources);
    }

    public void runModel() {

        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        // setup
        manager.readAdditionalData();

        // generate travel demand
        MitoTravelDemand ttd = new MitoTravelDemand(dataSet);
        ttd.generateTravelDemand();

        String trips = MitoUtil.customFormat("  " + "###,###", dataSet.getTrips().size());
        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    public void setBaseDirectory (String baseDirectory) {
        MitoUtil.setBaseDirectory(baseDirectory);
    }

    public void setRandomNumberGenerator (Random random) {
        MitoUtil.initializeRandomNumber(random);
    }

    public void setScenarioName (String scenarioName) {
        dataSet.setScenarioName(scenarioName);
    }


    public void feedData(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes, MitoHousehold[] households,
                         MitoPerson[] persons, int[] retailEmplByZone, int[] officeEmplByZone, int[] otherEmplByZone,
                         int[] totalEmplByZone, float[] sizeOfZonesInAcre) {
       manager.readFromFeed(zones, autoTravelTimes, transitTravelTimes, households, persons, retailEmplByZone, officeEmplByZone, otherEmplByZone, totalEmplByZone, sizeOfZonesInAcre );
    }

    public void initializeStandAlone() {
        // Read data if MITO is used as a stand-alone program and data are not fed from other program
        logger.info("  Reading input data for MITO");
        MitoUtil.initializeRandomNumber();
        manager.readAsStandAlone();
    }

    public DataSet getTravelDemand() {
        return dataSet;
    }
}
