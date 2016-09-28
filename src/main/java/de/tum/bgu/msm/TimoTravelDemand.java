package de.tum.bgu.msm;

import de.tum.bgu.msm.tripGeneration.TripGeneration;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Generates travel demand for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TimoTravelDemand {

    private static Logger logger = Logger.getLogger(TimoTravelDemand.class);
    private TimoData td;
    private ResourceBundle rb;

    public TimoTravelDemand(ResourceBundle rb, TimoData td) {
        this.rb = rb;
        this.td = td;
    }


    public void generateTravelDemand () {
        // main class to run travel demand

        // microscopic trip generation
        TripGeneration tg = new TripGeneration(rb, td);
        tg.generateTrips();
    }
}
