package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;


/**
 * Created by Nico on 20.07.2017.
 */
public class TripBalancer {

    private static final Logger logger = Logger.getLogger(TripBalancer.class);

    private final DataSet dataSet;
    private final Map<Integer, Map<String, Float>> tripAttr;

    public TripBalancer(DataSet dataSet, Map<Integer, Map<String, Float>> tripAttr) {
        this.dataSet = dataSet;
        this.tripAttr = tripAttr;
    }

    public void run() {
        balanceTripGeneration();
    }

    private Map<Integer, Map<String, Float>> balanceTripGeneration() {

        logger.info("  Balancing trip production and attractions");

        for (int purp = 0; purp < dataSet.getPurposes().length; purp++) {
            int tripsByPurp = getTotalNumberOfTripsGeneratedByPurpose(purp, dataSet.getTrips().values());
            float attrSum = 0;
            String purpose = dataSet.getPurposes()[purp];
            for (Zone zone : dataSet.getZones().values()) {
                attrSum += tripAttr.get(zone.getZoneId()).get(purpose);
            }
            if (attrSum == 0) {
                logger.warn("No trips for purpose " + dataSet.getPurposes()[purp] + " were generated.");
                continue;
            }
            // adjust attractions (or productions for NHBW and NHBO)
            for (Zone zone : dataSet.getZones().values()) {
                final float attrSumFinal = attrSum;
                tripAttr.get(zone.getZoneId()).replaceAll((k, v) -> v * tripsByPurp / attrSumFinal);

                // for NHBW and NHBO, we have more confidence in total production, as it is based on the household
                // travel survey. The distribution, however, is better represented by attraction rates. Therefore,
                // attractions are first scaled to productions (step above) and then productions are replaced with
                // zonal level attractions (step below).
                // todo: fix scaling towards attractions. Difficult, because individual households need to give up trips
                // or add trips to match attractions. Maybe it is alright to rely on productions instead.
//                if (tripPurposes.values()[purp] == tripPurposes.NHBW || tripPurposes.values()[purp] == tripPurposes.NHBO)
//                    tripProd[zone][purp][mstmInc] = tripAttr[zone][purp][mstmInc];
            }
        }
        return tripAttr;
    }

    public int getTotalNumberOfTripsGeneratedByPurpose(int purpose, Collection<MitoTrip> trips) {
        int prodSum = 0;
        for (MitoTrip trip : trips) {
            if (trip.getTripPurpose() == purpose) {
                prodSum++;
            }
        }
        return prodSum;
    }
}
