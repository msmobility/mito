package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.SummarizeDataToVisualize;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.personTripAssignment.PersonTripAssignment;
import de.tum.bgu.msm.modules.plansConverter.MatsimPopulationGenerator;
import de.tum.bgu.msm.modules.plansConverter.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.modules.scaling.TripScaling;
import de.tum.bgu.msm.modules.timeOfDay.TimeOfDayChoice;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Generates travel demand for the Microscopic Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 */
public class TravelDemandGenerator {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);
    private final DataSet dataSet;

    TravelDemandGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    void generateTravelDemand(String scenarioName) {

        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        if (dataSet.getTrips().isEmpty()) {
            logger.warn("No trips created. End of program.");
            return;
        }

        logger.info("Running Module: Person to Trip Assignment");
        PersonTripAssignment personTripAssignment = new PersonTripAssignment(dataSet);
        personTripAssignment.run();

        logger.info("Running Module: Travel Time Budget Calculation");
        TravelTimeBudgetModule ttb = new TravelTimeBudgetModule(dataSet);
        ttb.run();

        logger.info("Running Module: Microscopic Trip Distribution");
        TripDistribution distribution = new TripDistribution(dataSet);
        distribution.run();

        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        ModeChoice modeChoice = new ModeChoice(dataSet);
        modeChoice.run();

        logger.info("Running time of day choice");
        TimeOfDayChoice timeOfDayChoice = new TimeOfDayChoice(dataSet);
        timeOfDayChoice.run();

        logger.info("Running trip scaling");
        TripScaling tripScaling = new TripScaling(dataSet);
        tripScaling.run();

        MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator(dataSet);
        matsimPopulationGenerator.run();

        if (Resources.instance.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
            LongDistanceTraffic longDistanceTraffic = new LongDistanceTraffic(dataSet, Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
            longDistanceTraffic.run();
        }

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet, scenarioName);
        SummarizeDataToVisualize.writeFinalSummary(dataSet, scenarioName);

        if (Resources.instance.getBoolean(Properties.PRINT_MICRO_DATA, true)) {
            SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
            SummarizeData.writeOutTrips(dataSet, scenarioName);
        }
        if (Resources.instance.getBoolean(Properties.CREATE_CHARTS, true)) {
            SummarizeData.writeCharts(dataSet, scenarioName);
        }
    }
}
