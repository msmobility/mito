package de.tum.bgu.msm.calibration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.DataSetImpl;
import de.tum.bgu.msm.data.MitoTripFactoryImpl;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.io.output.*;
import de.tum.bgu.msm.modules.AttractionCalculatorImpl;
import de.tum.bgu.msm.modules.DestinationUtilityCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.calculators.AirportModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.personTripAssignment.PersonTripAssignment;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneratorType;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.ImplementationConfig;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.List;
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
public final class MitoModelForModeChoiceCalibration {

    private static final Logger logger = Logger.getLogger(MitoModelForModeChoiceCalibration.class);
    private final String scenarioName;

    private DataSetImpl dataSet;

    private MitoModelForModeChoiceCalibration(DataSetImpl dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static MitoModelForModeChoiceCalibration standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        MitoModelForModeChoiceCalibration model = new MitoModelForModeChoiceCalibration(new DataSetImpl(), Resources.instance.getString(Properties.SCENARIO_NAME));
        model.readStandAlone(config);
        return model;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        List<Purpose> purposes = Purpose.getAllPurposes();

        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet, purposes);
        purposes.forEach(purpose -> {
            ((TripGeneration) tg).registerTripGenerator(purpose, new MitoTripFactoryImpl(), TripGeneratorType.SampleEnumeration,null,new AttractionCalculatorImpl(dataSet,purpose));
        });

        tg.run();
        if (dataSet.getTrips().isEmpty()) {
            logger.warn("No trips created. End of program.");
            return;
        }

        logger.info("Running Module: Person to Trip Assignment");
        PersonTripAssignment personTripAssignment = new PersonTripAssignment(dataSet, purposes);
        personTripAssignment.run();

        logger.info("Running Module: Travel Time Budget Calculation");
        TravelTimeBudgetModule ttb = new TravelTimeBudgetModule(dataSet, purposes);
        ttb.run();

        logger.info("Running Module: Microscopic Trip Distribution");
        TripDistribution distribution = new TripDistribution(dataSet, purposes);
        purposes.forEach(purpose -> {
            ((TripDistribution) distribution).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorImpl(purpose));
        });
        distribution.run();

        ModeChoice modeChoice = new ModeChoice(dataSet, purposes);
        for(Purpose purpose: Purpose.values()) {

            final CalibratingModeChoiceCalculatorImpl baseCalculator;
            if(purpose == Purpose.AIRPORT) {
                baseCalculator = new CalibratingModeChoiceCalculatorImpl(new AirportModeChoiceCalculator(),
                        dataSet.getModeChoiceCalibrationData());
            } else {
                baseCalculator = new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(),
                        dataSet.getModeChoiceCalibrationData());
            }
            modeChoice.registerModeChoiceCalculator(purpose,
                    baseCalculator);
        }

        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");

        for (int iteration = 0; iteration < Resources.instance.getInt(Properties.MC_CALIBRATION_ITERATIONS, 1); iteration++){
            modeChoice.run();
            dataSet.getModeChoiceCalibrationData().updateCalibrationCoefficients(dataSet, iteration, purposes);
            logger.info("Finish iteration " + iteration);
        }

        dataSet.getModeChoiceCalibrationData().close();

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);

        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            ModeChoicePlots.writeModeChoice(dataSet, scenarioName);
            DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
            SummarizeData.writeCharts(dataSet, scenarioName);
        }
        printOutline(startTime);
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
        new ModeChoiceInputReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
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
