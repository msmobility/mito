package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.InputFeed;
import de.tum.bgu.msm.io.input.InputManager;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 * <p>
 * To run MITO, the following data need either to be passed in (using methods feedData) from another program or
 * need to be read from files and passed in (using method initializeStandAlone):
 * - zones
 * - autoTravelTimes
 * - transitTravelTimes
 * - timoHouseholds
 * - retailEmplByZone
 * - officeEmplByZone
 * - otherEmplByZone
 * - totalEmplByZone
 * - sizeOfZonesInAcre
 * All other data are read by function  manager.readAdditionalData();
 */

public final class MitoModel {

    private static final Logger logger = Logger.getLogger(MitoModel.class);
    private static String scenarioName;

    private final InputManager manager;
    private final DataSet dataSet;

    private MitoModel(String propertiesFile, Implementation implementation) {
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
        try {
            Resources.initializeResources(propertiesFile, implementation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MitoUtil.initializeRandomNumber();
    }

    public static MitoModel standAloneModel(String propertiesFile, Implementation implementation) {
        logger.info(" Creating standalone version of MITO ");
        MitoModel model = new MitoModel(propertiesFile, implementation);
        model.manager.readAsStandAlone();
        model.manager.readAdditionalData();
        return model;
    }

    public static MitoModel createModelWithInitialFeed(String propertiesFile, Implementation implementation, InputFeed feed) {
        MitoModel model = new MitoModel(propertiesFile, implementation);
        model.manager.readFromFeed(feed);
        model.manager.readAdditionalData();
        return model;
    }

    public void feedData(InputFeed feed) {
        manager.readFromFeed(feed);
    }

    public void runModel() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        TravelDemandGenerator ttd = new TravelDemandGenerator(dataSet);
        ttd.generateTravelDemand();

        printOutline(startTime);
    }

    private void printOutline(long startTime) {
        String trips = MitoUtil.customFormat("  " + "###,###", dataSet.getTrips().size());
        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000.f), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    public DataSet getData() {
        return dataSet;
    }

    public void setBaseDirectory(String baseDirectory) {
        MitoUtil.setBaseDirectory(baseDirectory);
    }

    public static String getScenarioName() {
        return scenarioName;
    }

    public static void setScenarioName(String setScenarioName) {
        scenarioName = setScenarioName;
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }
}
