package de.tum.bgu.msm;

import de.tum.bgu.msm.modules.abm.PlanGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelTimes.DummyTravelTimesForABM;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.io.output.MatsimPlanWriter;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.ImplementationConfig;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.Random;


public final class AbmModel {

    private static final Logger logger = Logger.getLogger(AbmModel.class);
    private final String scenarioName;

    private static DataSet dataSet;

    private AbmModel(DataSet dataSetAbm, String scenarioName) {
        this.dataSet = dataSetAbm;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static AbmModel createModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of ABM");
        Resources.initializeResources(propertiesFile);
        AbmModel model = new AbmModel(new DataSet(), Resources.instance.getString(Properties.SCENARIO_NAME));
        new ZonesReader(dataSet).read();
        new JobReader(dataSet, config.getJobTypeFactory()).read();
        new SchoolsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new HouseholdsCoordReader(dataSet).read();
        new PersonsReader(dataSet).read();
//        new TripAttractionRatesReader(dataSet).read();
//        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
        new ActivityTimeDistributionsReader(dataSet).read();
//        new CalibrationDataReader(dataSet).read();
//        new CalibrationRegionMapReader(dataSet).read();

        dataSet.setTravelTimes(new DummyTravelTimesForABM());
        return model;
    }

    public void runModel() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        PlanGenerator pg = new PlanGenerator();
        pg.runPlanGenerator(dataSet);

        new MatsimPlanWriter(dataSet, 0.005).run("matsimPlans1percent.xml");
        printOutline(startTime);
    }

    private void printOutline(long startTime) {
        logger.info("Completed the ABM model");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000.f), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    public DataSet getData() {
        return dataSet;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }



}
