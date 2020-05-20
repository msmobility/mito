package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

public class TripBalancer {

    private static final Logger logger = Logger.getLogger(TripBalancer.class);

    private final DataSet dataSet;

    public TripBalancer(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {
        balanceTripGeneration();
    }

    private void balanceTripGeneration() {

        logger.info("  Balancing trip production and attractions");

        for (Purpose purpose : Purpose.values()) {
            long tripsByPurp = dataSet.getHouseholds().values().stream().mapToInt(household -> household.getTripsForPurpose(purpose).size()).sum();
            double attrSum = dataSet.getZones().values().stream().mapToDouble(zone -> zone.getTripAttraction(purpose)).sum();
            if (tripsByPurp == 0) {
                logger.warn("No trips for purpose " + purpose + " were generated.");
                continue;
            }

            double factor = Resources.instance.getDouble(Properties.SCALE_FACTOR_FOR_TRIP_GENERATION, 1.0);

            double ratio = tripsByPurp/ factor / attrSum;
            adjustAttractions(ratio, purpose);
        }
    }

    private void adjustAttractions(double ratio, Purpose purpose) {
        for (MitoZone zone : dataSet.getZones().values()) {
            double oldValue = zone.getTripAttraction(purpose);
            zone.setTripAttraction(purpose, oldValue * ratio);
        }
    }
}
