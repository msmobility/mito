package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.SummarizeDataToVisualize;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.PedestrianModel;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceWithMoped;
import de.tum.bgu.msm.modules.plansConverter.MatsimPopulationGenerator;
import de.tum.bgu.msm.modules.plansConverter.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.DestinationUtilityCalculatorFactoryImpl2;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.AirportDistribution;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.NhbwNhboDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGeneratorFactoryPersonBasedHurdle;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static de.tum.bgu.msm.data.Purpose.NHBO;
import static de.tum.bgu.msm.data.Purpose.NHBW;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public final class TravelDemandGenerator2WithMoped {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator2WithMoped.class);
    private final DataSet dataSet;

    private final Module tripGenerationMandatory;
    private final Module personTripAssignmentMandatory;
    private final Module travelTimeBudgetMandatory;
    private final Module distributionMandatory;
    private final Module modeChoiceMandatory;
    private final Module tripGenerationDiscretionary;
    private final Module personTripAssignmentDiscretionary;
    private final Module travelTimeBudgetDiscretionary;
    private final Module distributionHomeBasedDiscretionary;
    private final Module modeChoiceHomeBasedDiscretionary;
    private final Module distributionNonHomeBased;
    private final Module modeChoiceNonHomeBased;
    private final Module timeOfDayChoiceMandatory;
    private final Module timeOfDayChoiceDiscretionary;
    private final Module tripScaling;
    private final Module matsimPopulationGenerator;
    private final Module longDistanceTraffic;

    private TravelDemandGenerator2WithMoped(
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
            Module distributionHomeBasedDiscretionary,
            Module modeChoiceHomeBasedDiscretionary,
            Module distributionNonHomeBased,
            Module modeChoiceNonHomeBased,
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
        this.distributionHomeBasedDiscretionary = distributionHomeBasedDiscretionary;
        this.modeChoiceHomeBasedDiscretionary = modeChoiceHomeBasedDiscretionary;
        this.distributionNonHomeBased = distributionNonHomeBased;
        this.modeChoiceNonHomeBased = modeChoiceNonHomeBased;
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
        private Module distributionHomeBasedDiscretionary;
        private Module modeChoiceHomeBasedDiscretionary;
        private Module distributionNonHomeBased;
        private Module modeChoiceNonHomeBased;

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
                    new DestinationUtilityCalculatorFactoryImpl2());
            modeChoiceMandatory = new ModeChoiceWithMoped(dataSet, Purpose.getMandatoryPurposes());
            timeOfDayChoiceMandatory = new TimeOfDayChoice(dataSet, Purpose.getMandatoryPurposes());

            tripGenerationDiscretionary = new TripGeneration(dataSet, new TripsByPurposeGeneratorFactoryPersonBasedHurdle(), Purpose.getDiscretionaryPurposes());
            //personTripAssignmentDiscretionary = new PersonTripAssignment(dataSet, Purpose.getDiscretionaryPurposes());
            travelTimeBudgetDiscretionary = new TravelTimeBudgetModule(dataSet, Purpose.getDiscretionaryPurposes());

            distributionHomeBasedDiscretionary = new TripDistribution(dataSet, Purpose.getHomeBasedDiscretionaryPurposes(), false,
                    new DestinationUtilityCalculatorFactoryImpl2());
            modeChoiceHomeBasedDiscretionary = new ModeChoiceWithMoped(dataSet, Purpose.getHomeBasedDiscretionaryPurposes());

            distributionNonHomeBased = new TripDistribution(dataSet, Purpose.getNonHomeBasedPurposes(), false,
                    new DestinationUtilityCalculatorFactoryImpl2());
            modeChoiceNonHomeBased = new ModeChoiceWithMoped(dataSet, Purpose.getNonHomeBasedPurposes());

            timeOfDayChoiceDiscretionary = new TimeOfDayChoice(dataSet, Purpose.getDiscretionaryPurposes());
            //until here it must be divided into two blocks - mandatory and discretionary

            tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator(dataSet, purposes);
            if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
                longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)), purposes);
            }
        }

        public TravelDemandGenerator2WithMoped build() {
            return new TravelDemandGenerator2WithMoped(dataSet,
                    tripGenerationMandatory,
                    personTripAssignmentMandatory,
                    travelTimeBudgetMandatory,
                    distributionMandatory,
                    modeChoiceMandatory,
                    timeOfDayChoiceMandatory,
                    tripGenerationDiscretionary,
                    personTripAssignmentDiscretionary,
                    travelTimeBudgetDiscretionary,
                    distributionHomeBasedDiscretionary,
                    modeChoiceHomeBasedDiscretionary,
                    distributionNonHomeBased,
                    modeChoiceNonHomeBased,
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

        //new Telework(dataSet, Purpose.getMandatoryPurposes(), 0.5).run();

        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentMandatory.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetMandatory.run();
        //((TravelTimeBudgetModule) travelTimeBudget).adjustDiscretionaryPurposeBudgets(Purpose.getMandatoryPurposes());
        logger.info("Running Module: Microscopic Trip Distribution");
        distributionMandatory.run();

        boolean runMoped = Resources.instance.getBoolean(Properties.RUN_MOPED, false);;
        PedestrianModel pedestrianModel = new PedestrianModel(dataSet);
        if (runMoped) {
            logger.info("Running Module: Moped Pedestrian Model - Home based Mandatory trips");
            pedestrianModel.initializeMoped();
            pedestrianModel.runMopedMandatory();
        }

        modeChoiceMandatory.run();
        logger.info("Running time of day choice");
        timeOfDayChoiceMandatory.run();



        tripGenerationDiscretionary.run();
        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudgetDiscretionary.run();
        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();


        if (runMoped) {
            logger.info("Running Module: Moped Pedestrian Model - Home based discretionary trips");
            pedestrianModel.runMopedHomeBasedDiscretionary();
        }

        logger.info("Running Module: Microscopic Trip Distribution");
        distributionHomeBasedDiscretionary.run();
        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceHomeBasedDiscretionary.run();

        ((TripDistribution)distributionNonHomeBased).setUp();
        if (runMoped) {
            logger.info("Running Module: Moped Pedestrian Model - non Home based trips");
            pedestrianModel.runMopedNonHomeBased();
        }

        logger.info("Running Module: Microscopic Trip Distribution");
        distributionNonHomeBased.run();
        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoiceNonHomeBased.run();

        logger.info("Running time of day choice");
        timeOfDayChoiceDiscretionary.run();

        logger.info("Running trip scaling");
        if (Resources.instance.getBoolean(Properties.RUN_TRIP_SCALING, false)) {
            tripScaling.run();
        }else{
            dataSet.getTrips().values().forEach(trip -> dataSet.addTripToSubsample(trip));
        }


        matsimPopulationGenerator.run();

        if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
            longDistanceTraffic.run();
        }

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);

        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            //DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
            //ModeChoicePlots.writeModeChoice(dataSet, scenarioName);
            SummarizeData.writeCharts(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.WRITE_MATSIM_POPULATION, true)) {
            //SummarizeData.writeMatsimPlans(dataSet, scenarioName);
        }
    }
}
