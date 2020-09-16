package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public final class HbeHbwDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final MitoOccupationStatus mitoOccupationStatus;
    private final IndexedDoubleMatrix2D baseProbabilities;

    private final DataSet dataSet;
    private final Map<Integer, MitoZone> zonesCopy;

    private HbeHbwDistribution(Purpose purpose, MitoOccupationStatus mitoOccupationStatus, IndexedDoubleMatrix2D baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.mitoOccupationStatus = mitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static HbeHbwDistribution hbe(IndexedDoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBE, MitoOccupationStatus.STUDENT, baseprobabilities, dataSet);
    }

    public static HbeHbwDistribution hbw(IndexedDoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBW, MitoOccupationStatus.WORKER, baseprobabilities, dataSet);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if (hasTripsForPurpose(household)) {
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                    trip.setTripOrigin(household);
                    findDestination(household, trip);
                    TripDistribution.distributedTripsCounter.incrementAndGet();
                }
            }
            counter++;
        }
        return null;
    }

    private void findDestination(MitoHousehold household, MitoTrip trip) {


        //todo make this trips intrazonal until school file for Germany is ready
        if (isFixedByOccupation(trip)) {
            if (trip.getTripPurpose().equals(Purpose.HBE)) {
                trip.setTripDestination(trip.getTripOrigin());
            } else {
                trip.setTripDestination(trip.getPerson().getOccupation());
            }
        } else {
            if (trip.getTripPurpose().equals(Purpose.HBE)) {
                trip.setTripDestination(trip.getTripOrigin());
            } else {
                TripDistribution.randomOccupationDestinationTrips.incrementAndGet();
                IndexedDoubleMatrix1D probabilities = baseProbabilities.viewRow(household.getHomeZone().getId());
                final int internalIndex = MitoUtil.select(probabilities.toNonIndexedArray(), random, probabilities.zSum());
                final MitoZone destination = zonesCopy.get(probabilities.getIdForInternalIndex(internalIndex));
                trip.setTripDestination(destination);
            }
        }
    }

    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private boolean isFixedByOccupation(MitoTrip trip) {
        if (trip.getPerson().getMitoOccupationStatus() == mitoOccupationStatus) {
            return trip.getPerson().getOccupation() != null;
        }
        return false;
    }
}
