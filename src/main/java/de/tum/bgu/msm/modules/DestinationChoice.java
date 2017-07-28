package de.tum.bgu.msm.modules;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

/**
 * Runs destination choice for each trip for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel, Ana Moreno
 * Created on June 8, 2017 in Munich, Germany
 *
 */
public class DestinationChoice extends Module{

    private static final Logger logger = Logger.getLogger(DestinationChoice.class);

    private Matrix distanceMatrix;

    private final double ALPHA_SHOP = 1;
    private final double BETA_SHOP = 1;
//    private final double GAMMA_SHOP = 1;



    public DestinationChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        selectTripDestinations();
    }


    private void selectTripDestinations () {
        // Run destination choice model
        logger.info("  Started Destination Choice");

        for(MitoTrip trip: dataSet.getTrips().values()) {
            if(trip.getTripPurpose() == dataSet.getPurposeIndex("HBS")) {
                double[] probabilities = new double[dataSet.getZones().size()-1];
                for(Zone zone: dataSet.getZones().values()) {

                    float distance = dataSet.getDistanceFromFromTo(trip.getTripOrigin(), zone.getZoneId());
                    float shopEmpls = zone.getRetailEmpl();
                    double utility = ALPHA_SHOP * shopEmpls+ BETA_SHOP * distance;
                    logger.info(utility);
                }
            }
        }


        //todo: write destination choice model
        logger.info("  Finished Destination Choice");
    }


}
