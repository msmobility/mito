package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.SummarizeDataToVisualize;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.calculators.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculator2017Impl;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.plansConverter.MatsimPopulationGenerator;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
//import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImpl2;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImplGermany;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGeneratorFactoryPersonBasedHurdle;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.core.population.PopulationUtils;

import java.util.List;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public final class TravelDemandGeneratorGermany {

    private static final Logger logger = Logger.getLogger(TravelDemandGeneratorGermany.class);
    private final DataSet dataSet;

    private final Module tripGenerationMandatory;
    private final Module personTripAssignmentMandatory;
    private final Module travelTimeBudgetMandatory;
    private final Module distributionMandatory;
    private final Module modeChoiceMandatory;
    private final Module tripGenerationDiscretionary;
    private final Module personTripAssignmentDiscretionary;
    private final Module travelTimeBudgetDiscretionary;
    private final Module distributionDiscretionary;
    private final Module modeChoiceDiscretionary;
    private final Module timeOfDayChoiceMandatory;
    private final Module timeOfDayChoiceDiscretionary;
    private final Module tripScaling;
    private final Module matsimPopulationGenerator;
    private final Module longDistanceTraffic;

    private TravelDemandGeneratorGermany(
            DataSet dataSet,
            Module tripGenerationMandatory,
            Module personTripAssignmentMandatory,
            Module travelTimeBudgetMandatory,
            Module distributionMandatory,
            Module modeChoiceMandatory,
            Module timeOfDayChoiceMandatory,
            Module tripGenerationDiscretionary,
            Module personTripAssignmentDiscretionary,
            Module travelTimeBudgetDiscretionary,
            Module distributionDiscretionary,
            Module modeChoiceDiscretionary,
            Module timeOfDayChoiceDiscretionary,
            Module tripScaling,
            Module matsimPopulationGenerator,
            Module longDistanceTraffic) {

        this.dataSet = dataSet;
        this.tripGenerationMandatory = tripGenerationMandatory;
        this.personTripAssignmentMandatory = personTripAssignmentMandatory;
        this.travelTimeBudgetMandatory = travelTimeBudgetMandatory;
        this.distributionMandatory = distributionMandatory;
        this.modeChoiceMandatory = modeChoiceMandatory;
        this.timeOfDayChoiceMandatory = timeOfDayChoiceMandatory;
        this.tripGenerationDiscretionary = tripGenerationDiscretionary;
        this.personTripAssignmentDiscretionary = personTripAssignmentDiscretionary;
        this.travelTimeBudgetDiscretionary = travelTimeBudgetDiscretionary;
        this.distributionDiscretionary = distributionDiscretionary;
        this.modeChoiceDiscretionary = modeChoiceDiscretionary;
        this.timeOfDayChoiceDiscretionary = timeOfDayChoiceDiscretionary;
        this.tripScaling = tripScaling;
        this.matsimPopulationGenerator = matsimPopulationGenerator;
        this.longDistanceTraffic = longDistanceTraffic;
    }


    public static class Builder {

        private final DataSet dataSet;

        private Module tripGenerationMandatory;
        private Module personTripAssignmentMandatory;
        private Module travelTimeBudgetMandatory;
        private Module distributionMandatory;
        private Module modeChoiceMandatory;
        private Module timeOfDayChoiceMandatory;

        private Module tripGenerationDiscretionary;
        private Module personTripAssignmentDiscretionary;
        private Module travelTimeBudgetDiscretionary;
        private Module distributionDiscretionary;
        private Module modeChoiceDiscretionary;
        private Module timeOfDayChoiceDiscretionary;

        private Module tripScaling;
        private Module matsimPopulationGenerator;
        private Module longDistanceTraffic;

        public Builder(DataSet dataSet) {
            this.dataSet = dataSet;
            //from here
            List<Purpose> purposes = Purpose.getAllPurposes();
            tripGenerationMandatory = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getMandatoryPurposes());
            //personTripAssignmentMandatory = new PersonTripAssignment(dataSet, Purpose.getMandatoryPurposes());
            travelTimeBudgetMandatory = new TravelTimeBudgetModule(dataSet, Purpose.getMandatoryPurposes());
            distributionMandatory = new TripDistribution(dataSet, Purpose.getMandatoryPurposes(), false,
                    new DestinationUtilityCalculatorFactoryImplGermany());
            modeChoiceMandatory = new ModeChoice(dataSet, Purpose.getMandatoryPurposes());
            Purpose.getMandatoryPurposes().forEach(purpose -> {
                ((ModeChoice) modeChoiceMandatory).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(purpose, dataSet), dataSet.getModeChoiceCalibrationData()));
            });
            timeOfDayChoiceMandatory = new TimeOfDayChoice(dataSet, Purpose.getMandatoryPurposes());

            tripGenerationDiscretionary = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getDiscretionaryPurposes());
            //personTripAssignmentDiscretionary = new PersonTripAssignment(dataSet, Purpose.getDiscretionaryPurposes());
            travelTimeBudgetDiscretionary = new TravelTimeBudgetModule(dataSet, Purpose.getDiscretionaryPurposes());
            distributionDiscretionary = new TripDistribution(dataSet, Purpose.getDiscretionaryPurposes(), false,
                    new DestinationUtilityCalculatorFactoryImplGermany());
            modeChoiceDiscretionary = new ModeChoice(dataSet, Purpose.getDiscretionaryPurposes());
            Purpose.getDiscretionaryPurposes().forEach(purpose -> {
                ((ModeChoice) modeChoiceDiscretionary).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017Impl(purpose, dataSet), dataSet.getModeChoiceCalibrationData()));
            });
            timeOfDayChoiceDiscretionary = new TimeOfDayChoice(dataSet, Purpose.getDiscretionaryPurposes());
            //until here it must be divided into two blocks - mandatory and discretionary

            tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator(dataSet, purposes);
        }

        public TravelDemandGeneratorGermany build() {
            return new TravelDemandGeneratorGermany(dataSet,
                    tripGenerationMandatory,
                    personTripAssignmentMandatory,
                    travelTimeBudgetMandatory,
                    distributionMandatory,
                    modeChoiceMandatory,
                    timeOfDayChoiceMandatory,
                    tripGenerationDiscretionary,
                    personTripAssignmentDiscretionary,
                    travelTimeBudgetDiscretionary,
                    distributionDiscretionary,
                    modeChoiceDiscretionary,
                    timeOfDayChoiceDiscretionary,
                    tripScaling,
                    matsimPopulationGenerator,
                    longDistanceTraffic);
        }

        public void setTripGeneration(Module tripGeneration) {
            this.tripGenerationMandatory = tripGeneration;
        }

        public void setPersonTripAssignment(Module personTripAssignment) {
            this.personTripAssignmentMandatory = personTripAssignment;
        }

        public void setTravelTimeBudget(Module travelTimeBudget) {
            this.travelTimeBudgetMandatory = travelTimeBudget;
        }

        public void setDistribution(Module distribution) {
            this.distributionMandatory = distribution;
        }

        public void setModeChoice(Module modeChoice) {
            this.modeChoiceMandatory = modeChoice;
        }

        public void setTimeOfDayChoiceMandatory(Module timeOfDayChoiceMandatory) {
            this.timeOfDayChoiceMandatory = timeOfDayChoiceMandatory;
        }

        public void setTripScaling(Module tripScaling) {
            this.tripScaling = tripScaling;
        }

        public void setMatsimPopulationGenerator(Module matsimPopulationGenerator) {
            this.matsimPopulationGenerator = matsimPopulationGenerator;
        }

        public void setLongDistanceTraffic(Module longDistanceTraffic) {
            this.longDistanceTraffic = longDistanceTraffic;
        }

        public DataSet getDataSet() {
            return dataSet;
        }

        public Module getTripGeneration() {
            return tripGenerationMandatory;
        }

        public Module getPersonTripAssignment() {
            return personTripAssignmentMandatory;
        }

        public Module getTravelTimeBudget() {
            return travelTimeBudgetMandatory;
        }

        public Module getDistribution() {
            return distributionMandatory;
        }

        public Module getModeChoice() {
            return modeChoiceMandatory;
        }

        public Module getTimeOfDayChoiceMandatory() {
            return timeOfDayChoiceMandatory;
        }

        public Module getTripScaling() {
            return tripScaling;
        }

        public Module getMatsimPopulationGenerator() {
            return matsimPopulationGenerator;
        }

        public Module getLongDistanceTraffic() {
            return longDistanceTraffic;
        }
    }



    public void generateTravelDemand(String scenarioName) {

        logger.info("Running Module: Microscopic Trip Generation");

        tripGenerationMandatory.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetMandatory.run();
        logger.info("Running Module: Microscopic Trip Distribution");
        distributionMandatory.run();
        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceMandatory.run();
        logger.info("Running time of day choice");
        timeOfDayChoiceMandatory.run();

        tripGenerationDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetDiscretionary.run();
        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();
        logger.info("Running Module: Microscopic Trip Distribution");
        distributionDiscretionary.run();
        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceDiscretionary.run();
        logger.info("Running time of day choice");
        timeOfDayChoiceDiscretionary.run();


        logger.info("Running trip scaling");
        tripScaling.run();

        matsimPopulationGenerator.run();

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);

        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            SummarizeData.writeCharts(dataSet, scenarioName);
        }

        String populationFile = Resources.instance.getBaseDirectory().toString() + "/" + "scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/plans_sd.xml.gz";
        PopulationUtils.writePopulation(dataSet.getPopulation(), populationFile);
    }
}
