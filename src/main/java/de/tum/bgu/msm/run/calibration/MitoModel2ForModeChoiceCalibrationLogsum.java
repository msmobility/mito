package de.tum.bgu.msm.run.calibration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceLogsum;
import de.tum.bgu.msm.modules.modeChoice.calculators.*;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImpl2;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImplLogsum;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripDistribution.TripDistributionLogsumEVnoEV;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGeneratorFactoryPersonBasedHurdle;
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
public final class MitoModel2ForModeChoiceCalibrationLogsum {

    private static final Logger logger = Logger.getLogger(MitoModel2ForModeChoiceCalibrationLogsum.class);
    private final String scenarioName;

    private DataSet dataSet;

    private MitoModel2ForModeChoiceCalibrationLogsum(DataSet dataSet, String scenarioName) {
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        MitoUtil.initializeRandomNumber();
    }

    public static MitoModel2ForModeChoiceCalibrationLogsum standAloneModel(String propertiesFile, ImplementationConfig config) {
        logger.info(" Creating standalone version of MITO ");
        Resources.initializeResources(propertiesFile);
        MitoModel2ForModeChoiceCalibrationLogsum model = new MitoModel2ForModeChoiceCalibrationLogsum(new DataSet(), Resources.instance.getString(Properties.SCENARIO_NAME));
        model.readStandAlone(config);
        return model;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        runForThisPurposes(Purpose.getMandatoryPurposes());

        runForThisPurposes(Purpose.getDiscretionaryPurposes());

        dataSet.getModeChoiceCalibrationData().close();

    }

    private void runForThisPurposes(List<Purpose> purposes) {
        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), purposes);
        tg.run();
        if (dataSet.getTrips().isEmpty()) {
            logger.warn("No trips created. End of program.");
            return;
        }

        logger.info("Running Module: Microscopic Trip Distribution");
        TripDistributionLogsumEVnoEV distribution = new TripDistributionLogsumEVnoEV(dataSet, purposes, false, new DestinationUtilityCalculatorFactoryImplLogsum());
        distribution.run();

        ModeChoiceLogsum modeChoice = new ModeChoiceLogsum(dataSet, purposes);
        for(Purpose purpose: purposes) {

            final CalibratingModeChoiceCalculatorImplLogsum baseCalculator;
            if(purpose == Purpose.AIRPORT) {
                baseCalculator = new CalibratingModeChoiceCalculatorImplLogsum(new AirportModeChoiceCalculatorLogsum(),
                        dataSet.getModeChoiceCalibrationData());
            } else {
                baseCalculator = new CalibratingModeChoiceCalculatorImplLogsum(new ModeChoiceCalculator2017ImplLogsum(purpose, dataSet),
                        dataSet.getModeChoiceCalibrationData());
            }
            modeChoice.registerModeChoiceCalculatorLogsum(purpose,
                    baseCalculator);
        }

        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");

        for (int iteration = 0; iteration < Resources.instance.getInt(Properties.MC_CALIBRATION_ITERATIONS, 1); iteration++){
            modeChoice.run();
            dataSet.getModeChoiceCalibrationData().updateCalibrationCoefficients(dataSet, iteration, purposes);
            logger.info("Finish iteration " + iteration);
        }
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
