package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.List;

public class TripBalancer {

    private static final Logger logger = Logger.getLogger(TripBalancer.class);

    private final DataSet dataSet;
    private final List<Purpose> purposes;

    public TripBalancer(DataSet dataSet, List<Purpose> purposes) {
        this.dataSet = dataSet;
        this.purposes = purposes;
    }

    public void run() {
        balanceTripGeneration();
    }

    private void balanceTripGeneration() {

        logger.info("  Balancing trip production and attractions");

        for (Purpose activityPurpose : purposes) {
            long tripsByPurp = dataSet.getHouseholds().values().stream().mapToInt(household -> household.getTripsForPurpose(activityPurpose).size()).sum();
            double attrSum = dataSet.getZones().values().stream().mapToDouble(zone -> zone.getTripAttraction(activityPurpose)).sum();
            if (tripsByPurp == 0) {
                logger.warn("No trips for activityPurpose " + activityPurpose + " were generated.");
                continue;
            }

            double factor = Resources.instance.getDouble(Properties.SCALE_FACTOR_FOR_TRIP_GENERATION, 1.0);

            double ratio = tripsByPurp / factor / attrSum;
            adjustAttractions(ratio, activityPurpose);
        }
    }

    private void adjustAttractions(double ratio, Purpose activityPurpose) {
        for (MitoZone zone : dataSet.getZones().values()) {
            double oldValue = zone.getTripAttraction(activityPurpose);
            zone.setTripAttraction(activityPurpose, oldValue * ratio);
        }
    }
}
