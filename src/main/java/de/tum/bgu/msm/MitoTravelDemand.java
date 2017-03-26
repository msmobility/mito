package de.tum.bgu.msm;

import de.tum.bgu.msm.data.TripDataManager;
import de.tum.bgu.msm.tripGeneration.TripGeneration;
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
    private MitoData mitoData;
    private TripDataManager tripDataManager;
    private ResourceBundle rb;

    public MitoTravelDemand(ResourceBundle rb, MitoData td, TripDataManager tripDataManager) {
        this.rb = rb;
        this.mitoData = td;
        this.tripDataManager = tripDataManager;
    }


    public void generateTravelDemand () {
        // main class to run travel demand

        // microscopic trip generation
        TripGeneration tg = new TripGeneration(rb, mitoData, tripDataManager);
        tg.generateTrips();
    }
}
