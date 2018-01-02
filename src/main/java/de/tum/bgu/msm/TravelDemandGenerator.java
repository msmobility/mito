package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.io.output.TripGenerationWriter;
import de.tum.bgu.msm.modules.modeChoice.ModeChoice;
import de.tum.bgu.msm.modules.personTripAssignment.PersonTripAssignment;
import de.tum.bgu.msm.modules.travelTimeBudget.TravelTimeBudgetModule;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Generates travel demand for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TravelDemandGenerator {

    private static final Logger logger = Logger.getLogger(TravelDemandGenerator.class);
    private final DataSet dataSet;

    public TravelDemandGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void generateTravelDemand () {

        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        if(dataSet.getTrips().isEmpty()){
            logger.warn("No trips created. End of program.");
            return;
        }
        logger.info("Running Module: Travel Time Budget Calculation");
        TravelTimeBudgetModule ttb = new TravelTimeBudgetModule(dataSet);
        ttb.run();
        logger.info("Running Module: Person to Trip Assignment");
        PersonTripAssignment personTripAssignment = new PersonTripAssignment(dataSet);
        personTripAssignment.run();
        logger.info("Running Module: Microscopic Trip Distribution");
        TripDistribution distribution = new TripDistribution(dataSet);
        distribution.run();
        logger.info("Running Module: Trip to Mode Assignment (Mode Choice)");
        ModeChoice modeChoice = new ModeChoice(dataSet);
        modeChoice.run();

        TripGenerationWriter.writeTripsByPurposeAndZone(dataSet);
        SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
        if(Resources.INSTANCE.getBoolean(Properties.CREATE_DESTINATION_CHOICE_HISTOGRAMS, false)){
            SummarizeData.writeHistograms(dataSet);
        }
    }
}
