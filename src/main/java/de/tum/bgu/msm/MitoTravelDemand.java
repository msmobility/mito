package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.TripDataManager;
import de.tum.bgu.msm.modules.DestinationChoice;
import de.tum.bgu.msm.modules.TravelTimeBudget;
import de.tum.bgu.msm.modules.TripGeneration;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Generates travel demand for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class MitoTravelDemand {

    private static Logger logger = Logger.getLogger(MitoTravelDemand.class);
    private final DataSet dataSet;

    public MitoTravelDemand(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void generateTravelDemand () {

        logger.info("Running Module: Microscopic Trip Generation");
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        logger.info("Running Module: Travel Time Budget Calculation");
        TravelTimeBudget ttb = new TravelTimeBudget(dataSet);
        ttb.run();
        logger.info("Running Module: Microscopic Destination Choice");
        DestinationChoice dc = new DestinationChoice(dataSet);
        dc.run();
    }
}
