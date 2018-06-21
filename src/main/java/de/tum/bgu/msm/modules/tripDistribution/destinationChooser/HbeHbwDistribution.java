package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public final class HbeHbwDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final Occupation occupation;
    private final DoubleMatrix2D baseProbabilities;

    private final DataSet dataSet;
    private final Map<Integer, MitoZone> zonesCopy;

    private HbeHbwDistribution(Purpose purpose, Occupation occupation, DoubleMatrix2D baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.occupation = occupation;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static HbeHbwDistribution hbe(DoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBE, Occupation.STUDENT, baseprobabilities, dataSet);
    }

    public static HbeHbwDistribution hbw(DoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBW, Occupation.WORKER, baseprobabilities, dataSet);
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
                    trip.setTripOrigin(household.getHomeZone());
                    trip.setTripOriginCoord(household.getHomeCoord());
                    findDestination(household, trip);
                    TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
                }
            }
            counter++;
        }
        return null;
    }

    private void findDestination(MitoHousehold household, MitoTrip trip) {
        if (isFixedByOccupation(trip)) {
            trip.setTripDestination(trip.getPerson().getOccupationZone());
            trip.setTripDestinationCoord(trip.getPerson().getOccupationCoord());
        } else {
            TripDistribution.RANDOM_OCCUPATION_DESTINATION_TRIPS.incrementAndGet();
            DoubleMatrix1D probabilities = baseProbabilities.viewRow(household.getHomeZone().getId());
            final MitoZone destination = zonesCopy.get(MitoUtil.select(probabilities.toArray(), random, probabilities.zSum()));
            trip.setTripDestination(destination);
            trip.setTripDestinationCoord(destination.getRandomCoord());
        }
    }

    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private boolean isFixedByOccupation(MitoTrip trip) {
        if (trip.getPerson().getOccupation() == occupation) {
            if (trip.getPerson().getOccupationZone() != null) {
                return true;
            }
        }
        return false;
    }
}
