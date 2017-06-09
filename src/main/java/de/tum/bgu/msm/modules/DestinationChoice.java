package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.data.TripDataManager;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Runs destination choice for each trip for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel, Ana Moreno
 * Created on June 8, 2017 in Munich, Germany
 *
 */
public class DestinationChoice {

    private static Logger logger = Logger.getLogger(DestinationChoice.class);
    private ResourceBundle rb;
    private MitoData mitoData;
    private TripDataManager tripDataManager;



    public DestinationChoice(ResourceBundle rb, MitoData td, TripDataManager tripDataManager) {
        this.rb = rb;
        this.mitoData = td;
        this.tripDataManager = tripDataManager;
    }


    public void selectTripDestinations () {
        // Run destination choice model
        logger.info("  Started Destination Choice");
        //todo: write destination choice model
        logger.info("  Finished Destination Choice");
    }
}
