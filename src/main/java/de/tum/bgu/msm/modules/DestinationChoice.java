package de.tum.bgu.msm.modules;

import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.TripDataManager;
import omx.OmxFile;
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

    private Matrix distanceMatrix;

    private final double ALPHA_SHOP = 1;
    private final double BETA_SHOP = 1;
    private final double GAMMA_SHOP = 1;



    public DestinationChoice(ResourceBundle rb, MitoData td, TripDataManager tripDataManager) {
        this.rb = rb;
        this.mitoData = td;
        this.tripDataManager = tripDataManager;
    }


    public void selectTripDestinations () {
        // Run destination choice model
        logger.info("  Started Destination Choice");



        for(MitoTrip trip: MitoTrip.getTripArray()) {
            if(trip.getTripPurpose() == mitoData.getPurposeIndex("shop")) {
                for(Integer i: mitoData.getZones()) {

                    float distance = mitoData.getDistances(trip.getTripOrigin(), i);
                    float shopEmpls = mitoData.getRetailEmplByZone(i);
                    double utility = ALPHA_SHOP * shopEmpls+ BETA_SHOP * distance + GAMMA_SHOP;


                }
            }
        }



        //todo: write destination choice model
        logger.info("  Finished Destination Choice");
    }


}
