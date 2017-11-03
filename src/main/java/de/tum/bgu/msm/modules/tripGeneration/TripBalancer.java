package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.Purpose;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;

public class TripBalancer {

    private static final Logger logger = Logger.getLogger(TripBalancer.class);

    private final DataSet dataSet;
    private final Map<Integer, EnumMap<Purpose, Float>> tripAttractionByZoneAndPurpose;

    public TripBalancer(DataSet dataSet, Map<Integer, EnumMap<Purpose, Float>> tripAttractionByZoneAndPurpose) {
        this.dataSet = dataSet;
        this.tripAttractionByZoneAndPurpose = tripAttractionByZoneAndPurpose;
    }

    public void run() {
        balanceTripGeneration();
    }

    private void balanceTripGeneration() {

        logger.info("  Balancing trip production and attractions");

        for (Purpose purpose: Purpose.values()) {
            long tripsByPurp = dataSet.getHouseholds().values().stream().mapToInt(household -> household.getTripsForPurpose(purpose).size()).sum();
            float attrSum = 0;
            for (Zone zone : dataSet.getZones().values()) {
                attrSum += tripAttractionByZoneAndPurpose.get(zone.getZoneId()).get(purpose);
            }
            if (attrSum == 0) {
                logger.warn("No trips for purpose " + purpose + " were generated.");
                continue;
            }
            // adjust attractions (or productions for NHBW and NHBO)
            for (Zone zone : dataSet.getZones().values()) {
                final float attrSumFinal = attrSum;
                tripAttractionByZoneAndPurpose.get(zone.getZoneId()).replaceAll((k, v) -> v * tripsByPurp / attrSumFinal);
            }
        }
    }
}
