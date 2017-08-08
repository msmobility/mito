package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Purpose;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;


/**
 * Created by Nico on 20.07.2017.
 */
public class TripBalancer {

    private static final Logger logger = Logger.getLogger(TripBalancer.class);

    private final DataSet dataSet;
    private final Map<Integer, Map<Purpose, Float>> tripAttractionByZoneAndPurpose;

    public TripBalancer(DataSet dataSet, Map<Integer, Map<Purpose, Float>> tripAttractionByZoneAndPurpose) {
        this.dataSet = dataSet;
        this.tripAttractionByZoneAndPurpose = tripAttractionByZoneAndPurpose;
    }

    public void run() {
        balanceTripGeneration();
    }

    private void balanceTripGeneration() {

        logger.info("  Balancing trip production and attractions");

        for (Purpose purpose: Purpose.values()) {
            int tripsByPurp = getTotalNumberOfTripsGeneratedByPurpose(purpose, dataSet.getTrips().values());
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

                // for NHBW and NHBO, we have more confidence in total production, as it is based on the household
                // travel survey. The distribution, however, is better represented by attraction rates. Therefore,
                // attractions are first scaled to productions (step above) and then productions are replaced with
                // zonal level attractions (step below).
                // todo: fix scaling towards attractions. Difficult, because individual households need to give up trips
                // or add trips to match attractions. Maybe it is alright to rely on productions instead.
//                if (tripPurposes.values()[purp] == tripPurposes.NHBW || tripPurposes.values()[purp] == tripPurposes.NHBO)
//                    tripProd[zone][purp][mstmInc] = tripAttractionByZoneAndPurpose[zone][purp][mstmInc];
            }
        }
    }

    private int getTotalNumberOfTripsGeneratedByPurpose(Purpose purpose, Collection<MitoTrip> trips) {
        int prodSum = 0;
        for (MitoTrip trip : trips) {
            if (trip.getTripPurpose().equals(purpose)) {
                prodSum++;
            }
        }
        return prodSum;
    }
}
