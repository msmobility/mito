package uk.cam.mrc.phm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.output.*;
import de.tum.bgu.msm.modules.DayOfWeekChoice;
import de.tum.bgu.msm.modules.MatsimPopulationGenerator7days;
import de.tum.bgu.msm.modules.ModeSetChoice;
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
import org.apache.log4j.Logger;
import uk.cam.mrc.phm.calculators.*;
import uk.cam.mrc.phm.io.SummarizeData7daysMCR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.data.Purpose.RRT;
import static de.tum.bgu.msm.data.Purpose.getListedPurposes;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public final class TravelDemandGeneratorMCR {

    private static final Logger logger = Logger.getLogger(TravelDemandGeneratorMCR.class);

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

    private TravelDemandGeneratorMCR(
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
            mandatoryPurposes.forEach(purpose -> ((TripGeneration) tripGenerationMandatory).registerTripGenerator(purpose, new MitoTripFactory7days(), TripGeneratorType.PersonBasedHurdlePolr,new TripGenCalculatorMCR(dataSet),
                    new AttractionCalculatorMCR(dataSet,purpose)));

            distributionMandatory = new TripDistribution(dataSet, mandatoryPurposes);
            mandatoryPurposes.forEach(purpose -> ((TripDistribution) distributionMandatory).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorMCR(purpose)));

            tripGenerationDiscretionary = new TripGeneration(dataSet, discretionaryPurposes);
            discretionaryPurposes.forEach(purpose -> ((TripGeneration) tripGenerationDiscretionary).registerTripGenerator(purpose, new MitoTripFactory7days(),TripGeneratorType.PersonBasedHurdleNegBin,new TripGenCalculatorMCR(dataSet),
                    new AttractionCalculatorMCR(dataSet,purpose)));

            modeSetChoice = new ModeSetChoice(dataSet, purposes, new ModeSetCalculatorMCR(dataSet));

            distributionDiscretionary = new TripDistribution(dataSet, discretionaryPurposes);
            // Register ALL purposes here, because we need the mandatory purpose matrices for NHBW / NHBO
            purposes.forEach(purpose -> ((TripDistribution) distributionDiscretionary).registerDestinationUtilityCalculator(purpose, new DestinationUtilityCalculatorMCR(purpose)));
            //Override the calculator for RRT, because the categorisePerson is different
            ((TripDistribution) distributionDiscretionary).registerDestinationUtilityCalculator(RRT, new DestinationUtilityCalculatorRrtMCR(RRT));

            modeChoice = new ModeChoice(dataSet, purposes);
            purposes.forEach(purpose -> ((ModeChoice) modeChoice).registerModeChoiceCalculator(purpose, new CalibratingModeChoiceCalculatorImpl(new ModeChoiceCalculatorMCR(purpose, dataSet), dataSet.getModeChoiceCalibrationData())));
            //Override the calculator for RRT, because so far we don't have calibration factors for RRT
            ((ModeChoice) modeChoice).registerModeChoiceCalculator(RRT, new ModeChoiceCalculatorRrtMCR(dataSet));

            dayOfWeekChoice = new DayOfWeekChoice(dataSet, purposes);

            timeOfDayChoice = new TimeOfDayChoice(dataSet, purposes);
            //until here it must be divided into two blocks - mandatory and discretionary

            tripScaling = new TripScaling(dataSet, purposes);
            matsimPopulationGenerator = new MatsimPopulationGenerator7days(dataSet, purposes);
            if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
                longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)), purposes);
            }
        }

        public TravelDemandGeneratorMCR build() {
            return new TravelDemandGeneratorMCR(dataSet,
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

        tripGenerationDiscretionary.run();
        //logger.info("Running Module: Person to Trip Assignment");
        //personTripAssignmentDiscretionary.run();
        logger.info("Running Module: Travel Time Budget Calculation");
//        travelTimeBudgetDiscretionary.run();
//        ((TravelTimeBudgetModule) travelTimeBudgetDiscretionary).adjustDiscretionaryPurposeBudgets();


        logger.info("Running Module: Mode set choice");
        if(Resources.instance.getBoolean(Properties.RUN_MODESET,false)) {
            modeSetChoice.run();
        }

        logger.info("Running Module: Microscopic Trip Distribution");
        distributionDiscretionary.run();

        // Trip distribution calibration example: todo: specify these in properties file, keep in generator and do similar for mode choice.
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBW, new double[] {5.195,8.976,11.525});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBE, new double[] {2.546,3.395,2.617});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBS, new double[] {2.579,4.19,4.919});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBO, new double[] {3.641,5.633,7.071});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBR, new double[] {3.592,5.793,6.842});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.NHBO, new double[] {1.588,4.179,4.916});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.NHBW, new double[] {1.764,5.172,7.817});
//       ((TripDistribution) distributionDiscretionary).calibrate(Purpose.RRT, new double[] {0.622,1.427,1.427});
//        ((TripDistribution) distributionDiscretionary).calibrate(Purpose.HBA, new double[] {1.383,3.940,3.850});




        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        modeChoice.run();

        // MODE CHOICE CALIBRATION CODE
        if(Resources.instance.getBoolean(Properties.RUN_CALIBRATION_MC,false)) {
            int modeChoiceCalibrationIterations = Resources.instance.getInt(Properties.MC_CALIBRATION_ITERATIONS, 0);
            if (modeChoiceCalibrationIterations > 0) {
                ModeChoiceCalibrationData modeChoiceCalibrationData = dataSet.getModeChoiceCalibrationData();
                for (int i = 1; i <= modeChoiceCalibrationIterations; i++) {
                    modeChoiceCalibrationData.updateCalibrationCoefficients(dataSet, i,getListedPurposes(Resources.instance.getString(Properties.TRIP_PURPOSES)));
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



        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData7daysMCR.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData7daysMCR.writeAllTrips(dataSet,scenarioName);
            /*for(Day day : Day.values()){
                for(Mode mode : Mode.values()){
                    Collection<MitoTrip> tripsToPrint = dataSet.getTrips().values().stream().filter(tt -> day.equals(((MitoTrip7days)tt).getDepartureDay()) & mode.equals(tt.getTripMode())).collect(Collectors.toList());
                    if(tripsToPrint.size()>0){
                        SummarizeData7daysMCR.writeOutTripsByDayByMode(dataSet,scenarioName,day,mode,tripsToPrint);
                    }else{
                        logger.info("No trips for mode: " + mode + ",day: " + day);
                    }

                }
            }*/
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            DistancePlots.writeDistanceDistributions(dataSet, scenarioName);
            ModeChoicePlots.writeModeChoice(dataSet, scenarioName);
            SummarizeData7daysMCR.writeCharts(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.WRITE_MATSIM_POPULATION, true)) {
            SummarizeData7daysMCR.writeMatsimPlans(dataSet, scenarioName);
        }
    }
}
