package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

public class AirportDistribution extends RandomizableConcurrentFunction<Void> {

    private static final Logger logger = Logger.getLogger(AirportDistribution.class);
    Purpose purpose = Purpose.AIRPORT;
    private final DataSet dataSet;

    private final MitoZone airportZone;


    protected AirportDistribution(long randomSeed, DataSet dataSet) {
        super(randomSeed);
        this.dataSet = dataSet;
        this.airportZone = dataSet.getZones().get(1659);
    }

    @Override
    public Void call() throws Exception {
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            Coord coord = new Coord(household.getHomeLocation().x, household.getHomeLocation().y);
            Coord airportCoord = dataSet.getZones().get(airportZone).getRandomCoord();
            if (hasTripsForPurpose(household)) {
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                    if (MitoUtil.getRandomObject().nextDouble() < 0.5) {
                        trip.setTripOrigin(household.getHomeZone());
                        trip.setTripOriginCoord(coord);
                        trip.setTripDestination(airportZone);
                        trip.setTripDestinationCoord(airportCoord);
                    } else {
                        trip.setTripOrigin(airportZone);
                        trip.setTripOriginCoord(airportCoord);
                        trip.setTripDestination(household.getHomeZone());
                        trip.setTripDestinationCoord(coord);
                    }

                    TripDistribution.distributedTripsCounter.incrementAndGet();
                }
            }
            counter++;
        }
        return null;
    }

    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }


}
