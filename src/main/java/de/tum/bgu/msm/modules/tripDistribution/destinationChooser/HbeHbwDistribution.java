package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public final class HbeHbwDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final MitoOccupationStatus mitoOccupationStatus;
    private final IndexedDoubleMatrix2D baseProbabilitiesEV;
    private final IndexedDoubleMatrix2D baseProbabilitiesNoEV;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;

    private HbeHbwDistribution(Purpose purpose, MitoOccupationStatus mitoOccupationStatus,
                               IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition,
                               Map<Integer, MitoZone> zones) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.mitoOccupationStatus = mitoOccupationStatus;
        this.baseProbabilitiesEV = baseProbabilitiesEV;
        this.baseProbabilitiesNoEV = baseProbabilitiesNoEV;
        this.householdPartition = householdPartition;
        this.zonesCopy = new HashMap<>(zones);
    }

    public static HbeHbwDistribution hbe(IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones) {
        return new HbeHbwDistribution(Purpose.HBE, MitoOccupationStatus.STUDENT, baseProbabilitiesEV, baseProbabilitiesNoEV, householdPartition, zones);
    }

    public static HbeHbwDistribution hbw(IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones) {
        return new HbeHbwDistribution(Purpose.HBW, MitoOccupationStatus.WORKER, baseProbabilitiesEV, baseProbabilitiesNoEV, householdPartition, zones);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MitoHousehold household : householdPartition) {
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
        IndexedDoubleMatrix2D baseProbabilities = household.isHasEV() ? baseProbabilitiesEV : baseProbabilitiesNoEV;
        if (isFixedByOccupation(trip)) {
                trip.setTripDestination(trip.getPerson().getOccupation());
        } else {
            TripDistribution.randomOccupationDestinationTrips.incrementAndGet();
            IndexedDoubleMatrix1D probabilities = baseProbabilities.viewRow(household.getHomeZone().getId());
            final int internalIndex = MitoUtil.select(probabilities.toNonIndexedArray(), random, probabilities.zSum());
            final MitoZone destination = zonesCopy.get(probabilities.getIdForInternalIndex(internalIndex));
            trip.setTripDestination(destination);
            if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)){
                Coord destinationCoord = CoordUtils.createCoord(destination.getRandomCoord(MitoUtil.getRandomObject()));
                trip.setDestinationCoord(destinationCoord);
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
