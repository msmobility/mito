package de.tum.bgu.msm.run.calibration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.io.output.DistancePlots;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculator2017Impl;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.*;
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
public final class CalibrateDestinationChoiceLogsum {

    private static final Logger logger = Logger.getLogger(CalibrateDestinationChoiceLogsum.class);
    private final String scenarioName;

    private DataSet dataSet;

    public static void main(String[] args) {
        CalibrateDestinationChoiceLogsum model = CalibrateDestinationChoiceLogsum.standAloneModel(args[0], MunichImplementationConfig.get());
        model.run();
    }

    private CalibrateDestinationChoiceLogsum(DataSet dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static CalibrateDestinationChoiceLogsum standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        CalibrateDestinationChoiceLogsum model = new CalibrateDestinationChoiceLogsum(new DataSet(), Resources.instance.getString(Properties.SCENARIO_NAME));
        model.readStandAlone(config);
        return model;
    }

    public static CalibrateDestinationChoiceLogsum initializeModelFromSilo(String propertiesFile, DataSet dataSet, String scenarioName) {
        logger.info(" Initializing MITO from SILO");
        Resources.initializeResources(propertiesFile);
        CalibrateDestinationChoiceLogsum model = new CalibrateDestinationChoiceLogsum(dataSet, scenarioName);
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

        Map<Purpose, Double> logsumCalibrationParameters = new HashMap<>();
        Map<Purpose, Double> attractionCalibrationParameters = new HashMap<>();

        Purpose.getMandatoryPurposes().forEach(p -> {
            logsumCalibrationParameters.put(p, 1.0);
            attractionCalibrationParameters.put(p, 1.0);
        });

        distributionMandatory = new TripDistributionLogsumEVnoEV(dataSet, Purpose.getMandatoryPurposes(),
                logsumCalibrationParameters,
                attractionCalibrationParameters, false, new DestinationUtilityCalculatorFactoryImplLogsum());
        distributionMandatory.run();

        TripDistributionCalibration tripDistributionCalibrationMandatory =
                new TripDistributionCalibration(dataSet, Purpose.getMandatoryPurposes(),
                logsumCalibrationParameters, attractionCalibrationParameters);

        int iterations = 30;
        for (int iteration = 0; iteration < iterations; iteration++) {
            tripDistributionCalibrationMandatory.update(iteration);
            distributionMandatory = new TripDistributionLogsumEVnoEV(dataSet, Purpose.getMandatoryPurposes(),
                    tripDistributionCalibrationMandatory.getLogsumParameters(),
                    tripDistributionCalibrationMandatory.getDistanceParameters(), false, new DestinationUtilityCalculatorFactoryImplLogsum());
            distributionMandatory.run();
            logger.info("Finish trip distribution mandatory calibration iteration " + iteration);
        }

        tripDistributionCalibrationMandatory.close();

        modeChoiceMandatory = new ModeChoice(dataSet, Purpose.getMandatoryPurposes());
        Purpose.getMandatoryPurposes().forEach(purpose -> {
            ((ModeChoice) modeChoiceMandatory).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(purpose, dataSet), dataSet.getModeChoiceCalibrationData()));
        });
        modeChoiceMandatory.run();

        timeOfDayChoiceMandatory = new TimeOfDayChoice(dataSet, Purpose.getMandatoryPurposes());
        timeOfDayChoiceMandatory.run();

        tripGenerationDiscretionary = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getDiscretionaryPurposes());
        //personTripAssignmentDiscretionary = new PersonTripAssignment(dataSet, Purpose.getDiscretionaryPurposes());
        travelTimeBudgetDiscretionary = new TravelTimeBudgetModule(dataSet, Purpose.getDiscretionaryPurposes());

        modeChoiceDiscretionary = new ModeChoice(dataSet, Purpose.getDiscretionaryPurposes());
        Purpose.getDiscretionaryPurposes().forEach(purpose -> {
            ((ModeChoice) modeChoiceDiscretionary).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(purpose, dataSet), dataSet.getModeChoiceCalibrationData()));
        });
        timeOfDayChoiceDiscretionary = new TimeOfDayChoice(dataSet, Purpose.getDiscretionaryPurposes());


        tripGenerationDiscretionary.run();
        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetDiscretionary.run();
        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();
        logger.info("Running Module: Microscopic Trip Distribution");


        Map<Purpose, Double> logsumCalibrationParametersDisc = new HashMap<>();
        Map<Purpose, Double> distanceCalibrationParametersDisc = new HashMap<>();

        Purpose.getDiscretionaryPurposes().forEach(p -> {
            logsumCalibrationParametersDisc.put(p, 1.0);
            distanceCalibrationParametersDisc.put(p, 1.0);
        });

        distributionDiscretionary = new TripDistributionLogsumEVnoEV(dataSet, Purpose.getDiscretionaryPurposes(),
                logsumCalibrationParametersDisc,
                distanceCalibrationParametersDisc, false,
                new DestinationUtilityCalculatorFactoryImplLogsum());
        distributionDiscretionary.run();


        TripDistributionCalibration tripDistributionCalibrationDiscretionary =
                new TripDistributionCalibration(dataSet, Purpose.getDiscretionaryPurposes(),
                        logsumCalibrationParametersDisc, distanceCalibrationParametersDisc);

        for (int iteration = 0; iteration < iterations; iteration++) {
            tripDistributionCalibrationDiscretionary.update(iteration);
            distributionDiscretionary = new TripDistributionLogsumEVnoEV(dataSet, Purpose.getDiscretionaryPurposes(),
                    tripDistributionCalibrationDiscretionary.getLogsumParameters(),
                    tripDistributionCalibrationDiscretionary.getDistanceParameters(), false,
                    new DestinationUtilityCalculatorFactoryImplLogsum());
            distributionDiscretionary.run();
            logger.info("Finish trip distribution discretionary calibration iteration " + iteration);
        }

        tripDistributionCalibrationDiscretionary.close();



        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceDiscretionary.run();
        logger.info("Running time of day choice");
        timeOfDayChoiceDiscretionary.run();

        DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
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
        new GiveHouseholdsWithoutDwellingsCoordinates(dataSet).read();
        new PersonsReader(dataSet).read();
        new VehicleReader(dataSet).read();
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
        new BicycleOwnershipReaderAndModel(dataSet).read();
        new LogsumReader(dataSet).read();

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
