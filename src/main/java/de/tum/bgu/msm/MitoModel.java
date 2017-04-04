package de.tum.bgu.msm;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.TripDataManager;
import de.tum.bgu.msm.modules.TravelTimeBudget;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.ResourceBundle;

/**
 * Implements the Microsimulation Transport Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 * To run TIMO, the following data need either to be passed in (using methods feedData) from another program or
 * need to be read from files and passed in:
 * - zones:              public void setZones(int[] zones)
 * - autoTravelTimes:    public void setAutoTravelTimes (Matrix autoTravelTimes)
 * - transitTravelTimes: public void setTransitTravelTimes (Matrix transitTravelTimes)
 * - timoHouseholds:     public void setHouseholds(MitoHousehold[] timoHouseholds)
 * - retailEmplByZone:   public void setRetailEmplByZone(int[] retailEmplByZone)
 * - officeEmplByZone:   public void setOfficeEmplByZone(int[] officeEmplByZone)
 * - otherEmplByZone:    public void setOtherEmplByZone(int[] otherEmplByZone)
 * - totalEmplByZone:    public void setTotalEmplByZone(int[] totalEmplByZone)
 * - sizeOfZonesInAcre:  public void setSizeOfZonesInAcre(float[] sizeOfZonesInAcre)
 * All other data are read by function MitoData.readInputData().
 */

public class MitoModel {

    private static Logger logger = Logger.getLogger(MitoModel.class);
    private MitoData mitoData;
    private TripDataManager tripDataManager;
    private TravelTimeBudget ttbModel;
    private ResourceBundle rb;

    public MitoModel(ResourceBundle rb) {
        this.rb = rb;
        this.mitoData = new MitoData(rb);
        this.tripDataManager = new TripDataManager();
    }


    public void feedData(int[] zones, Matrix autoTravelTimes, Matrix transitTravelTimes, MitoHousehold[] mitoHouseholds,
                         int[] retailEmplByZone, int[] officeEmplByZone, int[] otherEmplByZone, int[] totalEmplByZone,
                         float[] sizeOfZonesInAcre) {
        // Feed data from other program. Need to write new methods to read these data from files if MitoModel is used as
        // stand-alone program.
        mitoData.setZones(zones);                           // zone are stored consecutively starting at position 0
        mitoData.setAutoTravelTimes(autoTravelTimes);
        mitoData.setTransitTravelTimes(transitTravelTimes);
        mitoData.setHouseholds(mitoHouseholds);
        mitoData.setRetailEmplByZone(retailEmplByZone);       // All employment and acre values are stored in the position of
        mitoData.setOfficeEmplByZone(officeEmplByZone);       // the zone ID. Position 0 will be empty, data for zone 1 is
        mitoData.setOtherEmplByZone(otherEmplByZone);         // stored in position 1, for zone 5 in position 5, etc.
        mitoData.setTotalEmplByZone(totalEmplByZone);         //
        mitoData.setSizeOfZonesInAcre(sizeOfZonesInAcre);     //
        // todo: the household travel survey should not be read every year the model runs, but only in the first year.
        // todo: It was difficult, however, to get this to work with Travis-CI, not sure why (RM, 25-Mar-2017)
        mitoData.readHouseholdTravelSurvey();
    }

    public void setBaseDirectory (String baseDirectory) {
        MitoUtil.setBaseDirectory(baseDirectory);
    }

    public void setRandomNumberGenerator (Random random) {
        MitoUtil.initializeRandomNumber(random);
    }

    public void setScenarioName (String scenarioName) {
        mitoData.setScenarioName(scenarioName);
    }

    public void readData() {
        // Read data if MITO is used as a stand-alone program and data are not fed from other program
        logger.info("  Reading input data for MITO");
        MitoUtil.initializeRandomNumber(rb);
        mitoData.readHouseholdTravelSurvey();
        mitoData.readZones();
        mitoData.readSkims();
        mitoData.readHouseholdData();
        mitoData.readPersonData();
        mitoData.readEmploymentData();
    }


    public void runModel() {
        // initialize MitoModel from other program
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        // setup
        mitoData.readInputData();

        // generate travel demand
        MitoTravelDemand ttd = new MitoTravelDemand(rb, mitoData, tripDataManager);
        ttd.generateTravelDemand();

        String trips = MitoUtil.customFormat("  " + "###,###", tripDataManager.getTotalNumberOfTrips());
        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }


    public MitoData getTravelDemand() {
        return mitoData;
    }
}
