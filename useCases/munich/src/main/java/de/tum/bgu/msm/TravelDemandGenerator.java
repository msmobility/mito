package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.DataSetImpl;
import de.tum.bgu.msm.data.MitoTripFactoryImpl;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.output.*;
import de.tum.bgu.msm.modules.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.personTripAssignment.PersonTripAssignment;
import de.tum.bgu.msm.modules.plansConverter.MatsimPopulationGenerator;
import de.tum.bgu.msm.modules.plansConverter.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripDistribution.tripDistributors.TripDistributorType;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneratorType;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public final class TravelDemandGenerator {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);

    private static final List<Purpose> PURPOSES = List.of(HBW,HBE,HBS,HBR,HBO,NHBW,NHBO);
    private final DataSet dataSet;

    private final Module tripGeneration;
    private final Module personTripAssignment;
    private final Module travelTimeBudget;
    private final Module distribution;
    private final Module modeChoice;
    private final Module timeOfDayChoice;
    private final Module tripScaling;
    private final Module matsimPopulationGenerator;
    private final Module longDistanceTraffic;

    private TravelDemandGenerator(
            DataSet dataSet,
            Module tripGeneration,
            Module personTripAssignment,
            Module travelTimeBudget,
            Module distribution,
            Module modeChoice,
            Module timeOfDayChoice,
            Module tripScaling,
            Module matsimPopulationGenerator,
            Module longDistanceTraffic) {

        this.dataSet = dataSet;

        this.tripGeneration = tripGeneration;
        this.personTripAssignment = personTripAssignment;
        this.travelTimeBudget = travelTimeBudget;
        this.distribution = distribution;
        this.modeChoice = modeChoice;
        this.timeOfDayChoice = timeOfDayChoice;
        this.tripScaling = tripScaling;
        this.matsimPopulationGenerator = matsimPopulationGenerator;
        this.longDistanceTraffic = longDistanceTraffic;
    }


    public static class Builder {

        private final DataSet dataSet;

        private Module tripGeneration;
        private Module personTripAssignment;
        private Module travelTimeBudget;
        private Module distribution;
        private Module modeChoice;
        private Module timeOfDayChoice;
        private Module tripScaling;
        private Module matsimPopulationGenerator;
        private Module longDistanceTraffic;

        public Builder(DataSetImpl dataSet) {
            this.dataSet = dataSet;
            List<Purpose> purposes = PURPOSES; // todo: specify this in properties file

            tripGeneration = new TripGeneration(dataSet, purposes);
            purposes.forEach(purpose -> {
                ((TripGeneration) tripGeneration).registerTripGenerator(purpose, new MitoTripFactoryImpl(), TripGeneratorType.SampleEnumeration,null, new AttractionCalculatorImpl(dataSet,purpose));
            });

            personTripAssignment = new PersonTripAssignment(dataSet, purposes);
            travelTimeBudget = new TravelTimeBudgetModule(dataSet, purposes);
            distribution = new TripDistribution(dataSet, purposes);
            Purpose.getAllPurposes().forEach(purpose -> ((TripDistribution) distribution).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorImpl(purpose)));

            // Override discretionary purposes with TTB version
            List<Purpose> discretionaryPurposes = new ArrayList<>(purposes);
            discretionaryPurposes.retainAll(Purpose.getDiscretionaryPurposes());

            List<Purpose> homeBasedDiscretionaryPurposes = new ArrayList<>(discretionaryPurposes);
            homeBasedDiscretionaryPurposes.retainAll(Purpose.getHomeBasedPurposes());
            homeBasedDiscretionaryPurposes.forEach(purpose -> ((TripDistribution) distribution).registerDestinationUtilityCalculator(purpose, TripDistributorType.HomeBasedDiscretionaryWithTTB, new DestinationUtilityCalculatorImpl(purpose)));

            List<Purpose> nonHomeBasedDisretionaryPurposes = new ArrayList<>(discretionaryPurposes);
            nonHomeBasedDisretionaryPurposes.removeAll(Purpose.getHomeBasedPurposes());
            nonHomeBasedDisretionaryPurposes.forEach(purpose -> ((TripDistribution) distribution).registerDestinationUtilityCalculator(purpose, TripDistributorType.NonHomeBasedDiscretionaryWithTTB, new DestinationUtilityCalculatorImpl(purpose)));


            modeChoice = new ModeChoice(dataSet, purposes);
            for (Purpose purpose : purposes){
                ((ModeChoice) modeChoice).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorImpl(), dataSet.getModeChoiceCalibrationData()));
                logger.info("Registering mode choice calculators based on 2008 MiD survey");
            }
            timeOfDayChoice = new TimeOfDayChoice(dataSet, purposes);
            tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator(dataSet, purposes);
            if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
                longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)), purposes);
            }
        }

        public TravelDemandGenerator build() {
            return new TravelDemandGenerator(dataSet,
                    tripGeneration,
                    personTripAssignment,
                    travelTimeBudget,
                    distribution,
                    modeChoice,
                    timeOfDayChoice,
                    tripScaling,
                    matsimPopulationGenerator,
                    longDistanceTraffic);
        }

        public void setTripGeneration(Module tripGeneration) {
            this.tripGeneration = tripGeneration;
        }

        public void setPersonTripAssignment(Module personTripAssignment) {
            this.personTripAssignment = personTripAssignment;
        }

        public void setTravelTimeBudget(Module travelTimeBudget) {
            this.travelTimeBudget = travelTimeBudget;
        }

        public void setDistribution(Module distribution) {
            this.distribution = distribution;
        }

        public void setModeChoice(Module modeChoice) {
            this.modeChoice = modeChoice;
        }

        public void setTimeOfDayChoice(Module timeOfDayChoice) {
            this.timeOfDayChoice = timeOfDayChoice;
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
            return tripGeneration;
        }

        public Module getPersonTripAssignment() {
            return personTripAssignment;
        }

        public Module getTravelTimeBudget() {
            return travelTimeBudget;
        }

        public Module getDistribution() {
            return distribution;
        }

        public Module getModeChoice() {
            return modeChoice;
        }

        public Module getTimeOfDayChoice() {
            return timeOfDayChoice;
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
        tripGeneration.run();
        if (dataSet.getTrips().isEmpty()) {
            logger.warn("No trips created. End of program.");
            return;
        }


        logger.info("Running Module: Person to Trip Assignment");
        personTripAssignment.run();

        //new Telework(dataSet, Purpose.getAllPurposes(), 0.5).run();

        logger.info("Running Module: Travel Time Budget Calculation");
        travelTimeBudget.run();

        logger.info("Running Module: Microscopic Trip Distribution");
        distribution.run();

        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoice.run();

        logger.info("Running time of day choice");
        timeOfDayChoice.run();

        logger.info("Running trip scaling");
        tripScaling.run();

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
