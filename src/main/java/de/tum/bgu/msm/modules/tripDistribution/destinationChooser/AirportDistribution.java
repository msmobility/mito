package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import com.vividsolutions.jts.geom.Coordinate;

public class AirportDistribution extends RandomizableConcurrentFunction<Void> {

    private static final Logger logger = Logger.getLogger(AirportDistribution.class);
    private final MitoZone airportZone;
    Purpose purpose = Purpose.AIRPORT;
    private final DataSet dataSet;

    private MicroLocation airport;



    protected AirportDistribution(long randomSeed, DataSet dataSet) {
        super(randomSeed);
        this.dataSet = dataSet;
        this.airportZone = dataSet.getZones().get(Resources.INSTANCE.getInt(Properties.AIRPORT_ZONE));
    }

    public static AirportDistribution airportDistribution(DataSet dataSet) {
        return new AirportDistribution(Resources.INSTANCE.getInt(Properties.RANDOM_SEED), dataSet);
    }


    @Override
    public Void call() throws Exception {
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if (hasTripsForPurpose(household)) {
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {

                    airport = new MicroLocation() {
                        @Override
                        public Coordinate getCoordinate() {
                            return new Coordinate(Resources.INSTANCE.getInt(Properties.AIRPORT_X),
                                    Resources.INSTANCE.getInt(Properties.AIRPORT_Y));
                        }
                        @Override
                        public int getZoneId() {
                            return airportZone.getZoneId();
                        }
                    };

                    if (MitoUtil.getRandomObject().nextDouble() < 0.5) {
                        trip.setTripOrigin(household.getHomeZone());
                        trip.setTripDestination(airport);
                    } else {
                        trip.setTripOrigin(airport);
                        trip.setTripDestination(household.getHomeZone());
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
