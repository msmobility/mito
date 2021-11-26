package de.tum.bgu.msm.run.calibration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImpl2;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripDistribution.TripDistributionCalibration;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGeneratorFactoryPersonBasedHurdle;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.ImplementationConfig;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implements the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 * <p>
 * To run MITO, the following data need either to be passed in from another program or
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
 */
public final class CalibrateDestinationChoice2 {

    private static final Logger logger = Logger.getLogger(CalibrateDestinationChoice2.class);
    private final String scenarioName;

    private DataSet dataSet;

    public static void main(String[] args) {
        CalibrateDestinationChoice2 model = CalibrateDestinationChoice2.standAloneModel(args[0], MunichImplementationConfig.get());
        model.run();
    }

    private CalibrateDestinationChoice2(DataSet dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static CalibrateDestinationChoice2 standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        CalibrateDestinationChoice2 model = new CalibrateDestinationChoice2(new DataSet(), Resources.instance.getScenarioName());
        model.readStandAlone(config);
        return model;
    }

    public static CalibrateDestinationChoice2 initializeModelFromSilo(String propertiesFile, DataSet dataSet, String scenarioName) {
        logger.info(" Initializing MITO from SILO");
        Resources.initializeResources(propertiesFile);
        CalibrateDestinationChoice2 model = new CalibrateDestinationChoice2(dataSet, scenarioName);
        new OmxSkimsReader(dataSet).readOnlyTransitTravelTimes();
        new OmxSkimsReader(dataSet).readSkimDistancesNMT();
        new OmxSkimsReader(dataSet).readSkimDistancesAuto();
        model.readAdditionalData();
        return model;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        Module tripGenerationMandatory;
        Module personTripAssignmentMandatory;
        Module travelTimeBudgetMandatory;
        Module distributionMandatory;
        Module modeChoiceMandatory;
        Module timeOfDayChoiceMandatory;

        Module tripGenerationDiscretionary;
        Module personTripAssignmentDiscretionary;
        Module travelTimeBudgetDiscretionary;
        Module distributionDiscretionary;
        Module modeChoiceDiscretionary;
        Module timeOfDayChoiceDiscretionary;

        List<Purpose> purposes = Purpose.getAllPurposes();
        tripGenerationMandatory = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getMandatoryPurposes());
        tripGenerationMandatory.run();

        travelTimeBudgetMandatory = new TravelTimeBudgetModule(dataSet, Purpose.getMandatoryPurposes());
        travelTimeBudgetMandatory.run();

        Map<Purpose, Double> travelDistanceCalibrationParameters = new HashMap<>();
        Map<Purpose, Double> impedanceCalibrationParameters = new HashMap<>();

        Purpose.getMandatoryPurposes().forEach(p -> {
            travelDistanceCalibrationParameters.put(p, 1.0);
            impedanceCalibrationParameters.put(p, 1.0);
        });

        distributionMandatory = new TripDistribution(dataSet, Purpose.getMandatoryPurposes(),
                travelDistanceCalibrationParameters,
                impedanceCalibrationParameters, false, new DestinationUtilityCalculatorFactoryImpl2());
        distributionMandatory.run();

        TripDistributionCalibration tripDistributionCalibrationMandatory =
                new TripDistributionCalibration(dataSet, Purpose.getMandatoryPurposes(),
                travelDistanceCalibrationParameters, impedanceCalibrationParameters);

        int iterations = 20;
        for (int iteration = 0; iteration < iterations; iteration++) {
            tripDistributionCalibrationMandatory.update(iteration);
            distributionMandatory = new TripDistribution(dataSet, Purpose.getMandatoryPurposes(),
                    tripDistributionCalibrationMandatory.getTravelDistanceParameters(),
                    tripDistributionCalibrationMandatory.getImpendanceParameters(), false, new DestinationUtilityCalculatorFactoryImpl2());
            distributionMandatory.run();
        }

        tripDistributionCalibrationMandatory.close();

        modeChoiceMandatory = new ModeChoice(dataSet, Purpose.getMandatoryPurposes());
        modeChoiceMandatory.run();

        timeOfDayChoiceMandatory = new TimeOfDayChoice(dataSet, Purpose.getMandatoryPurposes());
        timeOfDayChoiceMandatory.run();

        tripGenerationDiscretionary = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getDiscretionaryPurposes());
        //personTripAssignmentDiscretionary = new PersonTripAssignment(dataSet, Purpose.getDiscretionaryPurposes());
        travelTimeBudgetDiscretionary = new TravelTimeBudgetModule(dataSet, Purpose.getDiscretionaryPurposes());

        modeChoiceDiscretionary = new ModeChoice(dataSet, Purpose.getDiscretionaryPurposes());
        timeOfDayChoiceDiscretionary = new TimeOfDayChoice(dataSet, Purpose.getDiscretionaryPurposes());


        tripGenerationDiscretionary.run();
        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetDiscretionary.run();
        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();
        logger.info("Running Module: Microscopic Trip Distribution");


        Map<Purpose, Double> travelDistanceCalibrationParametersDisc = new HashMap<>();
        Map<Purpose, Double> impedanceCalibrationParametersDisc = new HashMap<>();

        Purpose.getDiscretionaryPurposes().forEach(p -> {
            travelDistanceCalibrationParametersDisc.put(p, 1.0);
            impedanceCalibrationParametersDisc.put(p, 1.0);
        });

        distributionDiscretionary = new TripDistribution(dataSet, Purpose.getDiscretionaryPurposes(),
                travelDistanceCalibrationParametersDisc,
                impedanceCalibrationParametersDisc, false,
                new DestinationUtilityCalculatorFactoryImpl2());
        distributionDiscretionary.run();


        TripDistributionCalibration tripDistributionCalibrationDiscretionary =
                new TripDistributionCalibration(dataSet, Purpose.getDiscretionaryPurposes(),
                        travelDistanceCalibrationParametersDisc, impedanceCalibrationParametersDisc);

        for (int iteration = 0; iteration < iterations; iteration++) {
            tripDistributionCalibrationDiscretionary.update(iteration);
            distributionDiscretionary = new TripDistribution(dataSet, Purpose.getDiscretionaryPurposes(),
                    tripDistributionCalibrationDiscretionary.getTravelDistanceParameters(),
                    tripDistributionCalibrationDiscretionary.getImpendanceParameters(), false,
                    new DestinationUtilityCalculatorFactoryImpl2());
            distributionDiscretionary.run();
        }

        tripDistributionCalibrationDiscretionary.close();



        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceDiscretionary.run();
        logger.info("Running time of day choice");
        timeOfDayChoiceDiscretionary.run();


    }

    private void readStandAlone(ImplementationConfig config) {
        dataSet.setYear(Resources.instance.getInt(Properties.SCENARIO_YEAR));
        new ZonesReader(dataSet).read();
        if (Resources.instance.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            new BorderDampersReader(dataSet).read();
        }
        new JobReader(dataSet, config.getJobTypeFactory()).read();
        new SchoolsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new HouseholdsCoordReader(dataSet).read();
        new PersonsReader(dataSet).read();
        dataSet.setTravelTimes(new SkimTravelTimes());
        new OmxSkimsReader(dataSet).read();
        readAdditionalData();
    }

    private void readAdditionalData() {
        new TripAttractionRatesReader(dataSet).read();
        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
        new TimeOfDayDistributionsReader(dataSet).read();
        new CalibrationDataReader(dataSet).read();
        new CalibrationRegionMapReader(dataSet).read();

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

    public String getScenarioName() {
        return scenarioName;
    }

    public void setRandomNumberGenerator(Random random) {
        MitoUtil.initializeRandomNumber(random);
    }


}
