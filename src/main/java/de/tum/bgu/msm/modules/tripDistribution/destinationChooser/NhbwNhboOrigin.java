package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.collect.ImmutableList;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.util.*;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public final class NhbwNhboOrigin extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final List<Purpose> priorPurposes;
    private final MitoOccupationStatus relatedMitoOccupationStatus;
    private final EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;
    private int noDestinationErrorTrip;

    private NhbwNhboOrigin(Purpose purpose, List<Purpose> priorPurposes, MitoOccupationStatus relatedMitoOccupationStatus,
                           EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedMitoOccupationStatus = relatedMitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(zones);
        this.householdPartition = householdPartition;
    }

    public static NhbwNhboOrigin nhbw(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones) {
        return new NhbwNhboOrigin(Purpose.NHBW, Collections.singletonList(Purpose.HBW),
                MitoOccupationStatus.WORKER, baseProbabilites, householdPartition, zones);
    }

    public static NhbwNhboOrigin nhbo(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones) {
        return new NhbwNhboOrigin(Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS),
                null, baseProbabilites, householdPartition, zones);
    }

    @Override
    public Void call() {
        for (MitoHousehold household : householdPartition) {
            if (hasTripsForPurpose(household)) {
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                    findOrigin(household, trip);
                }
            }
        }
        logger.error(noDestinationErrorTrip + " prior trips have no destination.");
        return null;
    }

    /**
     * Checks if members of this household perform trips of the set purpose
     *
     * @return true if trips are available, false otherwise
     */
    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    /**
     * Checks if this household has been allocated travel time budget for the set purpose
     *
     * @return true if budget was allocated, false otherwise
     */

    private void findOrigin(MitoHousehold household, MitoTrip trip) {
        final List<MitoTrip> possibleBaseTrips = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    if(priorTrip.getTripDestination()==null){
                        logger.error("Trip id: " +trip.getId() + "|Purpose: " + trip.getTripPurpose() + ", select trip: " + priorTrip.getId() + " as prior trip, but it has no trip destination." + priorTrip.getTripPurpose() + "|" +priorTrip.getTripDestinationMopedZone());
                        noDestinationErrorTrip++;
                    }else {
                        possibleBaseTrips.add(priorTrip);
                    }
                }
            }
        }

        if (!possibleBaseTrips.isEmpty()) {
            MitoTrip selectedTrip = MitoUtil.select(random, possibleBaseTrips);
            trip.setTripOrigin(selectedTrip.getTripDestination());
            trip.setTripOriginMopedZone(selectedTrip.getTripDestinationMopedZone());
            return;
        }

        if (trip.getPerson().getMitoOccupationStatus() == relatedMitoOccupationStatus &&
            trip.getPerson().getOccupation() != null) {
            trip.setTripOrigin(trip.getPerson().getOccupation());
            return;
        }

        final Purpose selectedPurpose = MitoUtil.select(random, priorPurposes);
        trip.setTripOrigin(findRandomOrigin(household, selectedPurpose));
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.completelyRandomNhbTrips.incrementAndGet();
        final IndexedDoubleMatrix1D originProbabilities = baseProbabilities.get(priorPurpose).viewRow(household.getHomeZone().getId());
        final int destinationInternalId = MitoUtil.select(originProbabilities.toNonIndexedArray(), random);
        return zonesCopy.get(originProbabilities.getIdForInternalIndex(destinationInternalId));
    }
}
