package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs destination choice for each trip for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel, Ana Moreno, Nico Kuehnel
 * Created on June 8, 2017 in Munich, Germany
 */
public class DestinationChoice extends Module {

    private static final Logger logger = Logger.getLogger(DestinationChoice.class);

    private final float ALPHA_SHOP = 0.01f;
    private final float BETA_SHOP = 0.01f;

    public DestinationChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        selectTripDestinations();
    }


    private void selectTripDestinations() {
        logger.info("  Started Destination Choice");
        for (MitoTrip trip : dataSet.getTrips().values()) {
            if (trip.getTripPurpose() == dataSet.getPurposeIndex("HBS")) {
                processHBSTrip(trip);
            }
        }
        logger.info("  Finished Destination Choice");
    }


    private void processHBSTrip(MitoTrip trip) {

        Map<Integer, Double> probabilities = new HashMap<>();
        for (Zone zone : dataSet.getZones().values()) {
            double utility = calculateUtility(trip.getTripOrigin(), zone);
            double probability = Math.exp(utility);
            if(Double.isInfinite(probability)) {
                System.out.println("blopp");
            }
            probabilities.put(zone.getZoneId(), probability);
        }
        MitoUtil.scaleMapTo(probabilities, 1000);
        trip.setTripDestination(MitoUtil.select(probabilities));
    }

    private float calculateUtility(int tripOrigin, Zone zone) {
        float distance = dataSet.getDistanceFromTo(tripOrigin, zone.getZoneId());
        float shopEmpls = zone.getRetailEmpl();
        return ALPHA_SHOP *shopEmpls + BETA_SHOP * distance;
    }
}
