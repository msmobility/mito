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
    private final ResourceBundle resources;

    public MitoTravelDemand(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.resources = resources;
    }

    public void generateTravelDemand () {
        // main class to run travel demand

        // microscopic trip generation
        TripGeneration tg = new TripGeneration(dataSet);
        tg.run();
        // calculate travel time budgets
        TravelTimeBudget ttb = new TravelTimeBudget(dataSet, resources);
        ttb.run();
        // microscopic destination choice
        DestinationChoice dc = new DestinationChoice(dataSet);
        dc.run();
    }
}
