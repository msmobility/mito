package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.io.input.readers.LogsumReader.convertArrayListToIntArray;

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

        for (Purpose purpose : purposes) {

            int[] zoneIds = convertArrayListToIntArray(dataSet.getZones().values());

            long tripsByPurp = 0 ;
            for (int origin : zoneIds){
                for (int destination : zoneIds){
                    tripsByPurp += dataSet.getAggregateTripMatrix().get(Mode.taxi).getIndexed(origin, destination);
                }
            }
            //long tripsByPurp = dataSet.getHouseholds().values().stream().mapToInt(household -> household.getTripsForPurpose(purpose).size()).sum();
            double attrSum = dataSet.getZones().values().stream().mapToDouble(zone -> zone.getTripAttraction(purpose)).sum();
            if (tripsByPurp == 0) {
                logger.warn("No trips for purpose " + purpose + " were generated.");
                continue;
            }

            double factor = Resources.instance.getDouble(Properties.SCALE_FACTOR_FOR_TRIP_GENERATION, 1.0);

            double ratio = tripsByPurp / factor / attrSum;
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
