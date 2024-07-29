package de.tum.bgu.msm.scenarios.mito7days;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.SummarizeData7days;
import de.tum.bgu.msm.io.output.*;
import de.tum.bgu.msm.modules.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.modeChoice.CalibratingModeChoiceCalculatorImpl;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import de.tum.bgu.msm.modules.plansConverter.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneratorType;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.scenarios.mito7days.calculators.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public final class TravelDemandGenerator7days {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator7days.class);

    private final DataSet dataSet;

    private final Module tripGenerationMandatory;
    private final Module personTripAssignmentMandatory;
    private final Module travelTimeBudgetMandatory;
    private final Module distributionMandatory;
    private final Module tripGenerationDiscretionary;
    private final Module personTripAssignmentDiscretionary;
    private final Module travelTimeBudgetDiscretionary;
    private final Module modeSetChoice;
    private final Module distributionDiscretionary;
    private final Module modeChoice;
    private final Module dayOfWeekChoice;
    private final Module timeOfDayChoice;
    private final Module tripScaling;
    private final Module matsimPopulationGenerator;
    private final Module longDistanceTraffic;

    private TravelDemandGenerator7days(
            DataSet dataSet,
            Module tripGenerationMandatory,
            Module personTripAssignmentMandatory,
            Module travelTimeBudgetMandatory,
            Module distributionMandatory,
            Module tripGenerationDiscretionary,
            Module personTripAssignmentDiscretionary,
            Module travelTimeBudgetDiscretionary,
            Module modeSetChoice,
            Module distributionDiscretionary,
            Module modeChoice,
            Module dayOfWeekChoice,
            Module timeOfDayChoice,
            Module tripScaling,
            Module matsimPopulationGenerator,
            Module longDistanceTraffic) {

        this.dataSet = dataSet;
        this.tripGenerationMandatory = tripGenerationMandatory;
        this.personTripAssignmentMandatory = personTripAssignmentMandatory;
        this.travelTimeBudgetMandatory = travelTimeBudgetMandatory;
        this.distributionMandatory = distributionMandatory;
        this.tripGenerationDiscretionary = tripGenerationDiscretionary;
        this.personTripAssignmentDiscretionary = personTripAssignmentDiscretionary;
        this.travelTimeBudgetDiscretionary = travelTimeBudgetDiscretionary;
        this.distributionDiscretionary = distributionDiscretionary;
        this.modeChoice = modeChoice;
        this.dayOfWeekChoice = dayOfWeekChoice;
        this.timeOfDayChoice = timeOfDayChoice;
        this.tripScaling = tripScaling;
        this.matsimPopulationGenerator = matsimPopulationGenerator;
        this.longDistanceTraffic = longDistanceTraffic;
        this.modeSetChoice = modeSetChoice;
    }


    public static class Builder {

        private final DataSet dataSet;

        private Module tripGenerationMandatory;
        private Module personTripAssignmentMandatory;
        private Module travelTimeBudgetMandatory;
        private Module distributionMandatory;
        private Module tripGenerationDiscretionary;
        private Module modeSetChoice;
        private Module personTripAssignmentDiscretionary;
        private Module travelTimeBudgetDiscretionary;
        private Module distributionDiscretionary;
        private Module modeChoice;
        private Module dayOfWeekChoice;
        private Module timeOfDayChoice;
        private Module tripScaling;
        private Module matsimPopulationGenerator;
        private Module longDistanceTraffic;


        public Builder(DataSet dataSet) {
            this.dataSet = dataSet;

            List<Purpose> purposes = Purpose.getListedPurposes(Resources.instance.getString(Properties.TRIP_PURPOSES));
            logger.info("Simulating trips for the following purposes: " + purposes.stream().map(Enum::toString).collect(Collectors.joining(",")));

            List<Purpose> mandatoryPurposes = new ArrayList<>(purposes);
            mandatoryPurposes.retainAll(Purpose.getMandatoryPurposes());
            List<Purpose> discretionaryPurposes = new ArrayList<>(purposes);
            discretionaryPurposes.removeAll(mandatoryPurposes);

            //from here
            tripGenerationMandatory = new TripGeneration(dataSet, mandatoryPurposes);
            mandatoryPurposes.forEach(purpose -> ((TripGeneration) tripGenerationMandatory).registerTripGenerator(purpose, new MitoTripFactory7days(), TripGeneratorType.PersonBasedHurdlePolr,new TripGenCalculator7days(dataSet),new AttractionCalculatorImpl(dataSet,purpose)));

            distributionMandatory = new TripDistribution(dataSet, mandatoryPurposes);
            mandatoryPurposes.forEach(purpose -> ((TripDistribution) distributionMandatory).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorImpl7days(purpose)));

            tripGenerationDiscretionary = new TripGeneration(dataSet, discretionaryPurposes);
            discretionaryPurposes.forEach(purpose -> ((TripGeneration) tripGenerationDiscretionary).registerTripGenerator(purpose, new MitoTripFactory7days(),TripGeneratorType.PersonBasedHurdleNegBin,new TripGenCalculator7days(dataSet),new AttractionCalculatorImpl(dataSet,purpose)));

            modeSetChoice = new ModeSetChoice(dataSet, purposes, new ModeSetCalculator7days(dataSet));

            distributionDiscretionary = new TripDistribution(dataSet, discretionaryPurposes);
            // Register ALL purposes here, because we need the mandatory purpose matrices for NHBW / NHBO
            purposes.forEach(purpose -> ((TripDistribution) distributionDiscretionary).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorImpl7days(purpose)));
            //Override the calculator for RRT, because the categorisePerson is different
            ((TripDistribution) distributionDiscretionary).registerDestinationUtilityCalculator(RRT, new DestinationUtilityCalculatorImplRrt7days(RRT));

            modeChoice = new ModeChoice(dataSet, purposes);
            purposes.forEach(purpose -> ((ModeChoice) modeChoice).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculator2017NewImpl(purpose, dataSet), dataSet.getModeChoiceCalibrationData())));
            //Override the calculator for RRT, because so far we don't have calibration factors for RRT
            ((ModeChoice) modeChoice).registerModeChoiceCalculator(RRT, new ModeChoiceCalculatorRrtImpl(dataSet));

            dayOfWeekChoice = new DayOfWeekChoice(dataSet, purposes);

            timeOfDayChoice = new TimeOfDayChoice(dataSet, purposes);
            //until here it must be divided into two blocks - mandatory and discretionary

            tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator7days(dataSet, purposes);
            if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
                longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)), purposes);
            }
        }

        public TravelDemandGenerator7days build() {
            return new TravelDemandGenerator7days(dataSet,
                    tripGenerationMandatory,
                    personTripAssignmentMandatory,
                    travelTimeBudgetMandatory,
                    distributionMandatory,
                    tripGenerationDiscretionary,
                    personTripAssignmentDiscretionary,
                    travelTimeBudgetDiscretionary,
                    modeSetChoice,
                    distributionDiscretionary,
                    modeChoice,
                    dayOfWeekChoice,
                    timeOfDayChoice,
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
            this.modeChoice = modeChoice;
        }

        public void setTimeOfDayChoice(Module timeOfDayChoice) {
            this.timeOfDayChoice= timeOfDayChoice;
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

        tripGenerationMandatory.run();

        //new Telework(dataSet, Purpose.getMandatoryPurposes(), 0.5).run();

        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentMandatory.run();
//        logger.info("Running Module: Travel Time Budget Calculation");
//        travelTimeBudgetMandatory.run();
        //((TravelTimeBudgetModule) travelTimeBudget).adjustDiscretionaryPurposeBudgets(Purpose.getMandatoryPurposes());
        logger.info("Running Module: Microscopic Trip Distribution");
        distributionMandatory.run();

        //((TripDistribution) distributionMandatory).calibrate(Purpose.HBW, new double[] {11.57,17.44,22.20});
        //((TripDistribution) distributionMandatory).calibrate(Purpose.HBE, new double[] {14.18,11.32,12.17});

        tripGenerationDiscretionary.run();
        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
//        travelTimeBudgetDiscretionary.run();
//        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();


        logger.info("Running Module: Mode set choice");
        modeSetChoice.run();

        logger.info("Running Module: Microscopic Trip Distribution");
        distributionDiscretionary.run();

        // Trip distribution calibration example: todo: specify these in properties file, keep in generator and do similar for mode choice.
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBO, new double[] {8.46,11.58,11.97});
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBR, new double[] {9.58,12.24,13.38});
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.NHBW, new double[] {9.58,13.62,17.89});
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.NHBO, new double[] {5.56,8.46,9.71});
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBS, new double[] {2.43,4.86,5.73});
        //((TripDistribution) distributionDiscretionary).calibrate(Purpose.RRT, new double[] {3.58,6.79,15.5});



        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoice.run();

        if(Resources.instance.getBoolean(Properties.RUN_CALIBRATION_MC,false)) {
            int modeChoiceCalibrationIterations = Resources.instance.getInt(Properties.MC_CALIBRATION_ITERATIONS, 0);
            if (modeChoiceCalibrationIterations > 0) {
                ModeChoiceCalibrationData modeChoiceCalibrationData = dataSet.getModeChoiceCalibrationData();
                for (int i = 1; i <= modeChoiceCalibrationIterations; i++) {
                    modeChoiceCalibrationData.updateCalibrationCoefficients(dataSet, i, getAllPurposes());
                    modeChoice.run();
                }
                modeChoiceCalibrationData.close();
            }
        }

        logger.info("Running day of week choice");
        dayOfWeekChoice.run();

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



        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, false)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            for(Day day : Day.values()){
                for(Mode mode : Mode.values()){
                    Collection<MitoTrip> tripsToPrint = dataSet.getTrips().values().stream().filter(tt -> day.equals(((MitoTrip7days)tt).getDepartureDay()) & mode.equals(tt.getTripMode())).collect(Collectors.toList());
                    if(tripsToPrint.size()>0){
                        SummarizeData7days.writeOutTripsByDayByMode(dataSet,scenarioName,day,mode,tripsToPrint);
                    }else{
                        logger.info("No trips for mode: " + mode + ",day: " + day);
                    }

                }
            }
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, false)) {
            DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
            ModeChoicePlots.writeModeChoice(dataSet, scenarioName);
            SummarizeData.writeCharts(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.WRITE_MATSIM_POPULATION, false)) {
            SummarizeData.writeMatsimPlans(dataSet, scenarioName);
        }
    }
}
