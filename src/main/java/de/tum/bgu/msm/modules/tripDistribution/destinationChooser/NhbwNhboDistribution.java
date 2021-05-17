package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public final class NhbwNhboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static double VARIANCE_DOUBLED = 500 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);
    private final boolean USE_BUDGETS_IN_DESTINATION_CHOICE;

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final double peakHour;

    private final Purpose activityPurpose;
    private final List<Purpose> priorPurposes;
    private final MitoOccupationStatus relatedMitoOccupationStatus;
    private final EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities;
    private final TravelTimes travelTimes;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;

    private double mean;

    private NhbwNhboDistribution(boolean useBudgetsInDestinationChoice, Purpose activityPurpose, List<Purpose> priorPurposes, MitoOccupationStatus relatedMitoOccupationStatus,
                                 EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                 TravelTimes travelTimes, double peakHour) {
        super(MitoUtil.getRandomObject().nextLong());
        USE_BUDGETS_IN_DESTINATION_CHOICE = useBudgetsInDestinationChoice;
        this.activityPurpose = activityPurpose;
        this.priorPurposes = priorPurposes;
        this.relatedMitoOccupationStatus = relatedMitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(zones);
        this.travelTimes = travelTimes;
        this.peakHour = peakHour;
        this.householdPartition = householdPartition;
    }

    public static NhbwNhboDistribution nhbw(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites,  Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                            TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new NhbwNhboDistribution(useBudgetsInDestinationChoice, Purpose.NHBW, Collections.singletonList(Purpose.HBW),
                MitoOccupationStatus.WORKER, baseProbabilites, householdPartition, zones, travelTimes, peakHour);
    }

    public static NhbwNhboDistribution nhbo(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites,  Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                            TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new NhbwNhboDistribution(useBudgetsInDestinationChoice, Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS),
                null, baseProbabilites, householdPartition, zones, travelTimes, peakHour);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MitoHousehold household : householdPartition) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + activityPurpose
                        + "\nIdeal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
            }
            if (hasTripsForPurpose(household)) {
                if (USE_BUDGETS_IN_DESTINATION_CHOICE){
                    if (hasBudgetForPurpose(household)) {
                        updateBudgets(household);
                        for (MitoTrip trip : household.getTripsForPurpose(activityPurpose)) {
                            Location origin = findOrigin(household, trip);
                            if (origin == null) {
                                logger.debug("No origin found for trip" + trip);
                                TripDistribution.failedTripsCounter.incrementAndGet();
                                continue;
                            }
                            trip.setTripOrigin(origin);
                            MitoZone destination = findDestination(trip.getTripOrigin().getZoneId());
                            trip.setTripDestination(destination);
                            if (destination == null) {
                                logger.debug("No destination found for trip" + trip);
                                TripDistribution.failedTripsCounter.incrementAndGet();
                                continue;
                            }
                            postProcessTrip(trip);
                            TripDistribution.distributedTripsCounter.incrementAndGet();
                        }
                    } else {
                        TripDistribution.failedTripsCounter.incrementAndGet();
                    }
                } else {
                    for (MitoTrip trip : household.getTripsForPurpose(activityPurpose)) {
                        Location origin = findOrigin(household, trip);
                        if (origin == null) {
                            logger.debug("No origin found for trip" + trip);
                            TripDistribution.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        trip.setTripOrigin(origin);
                        MitoZone destination = findDestinationWithoutBudget(trip.getTripOrigin().getZoneId());
                        trip.setTripDestination(destination);
                        if (destination == null) {
                            logger.debug("No destination found for trip" + trip);
                            TripDistribution.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        TripDistribution.distributedTripsCounter.incrementAndGet();
                    }
                }

            }
            counter++;
        }
        logger.info("Ideal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
        return null;
    }

    /**
     * Checks if members of this household perform trips of the set activityPurpose
     *
     * @return true if trips are available, false otherwise
     */
    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(activityPurpose).isEmpty();
    }

    /**
     * Checks if this household has been allocated travel time budget for the set activityPurpose
     *
     * @return true if budget was allocated, false otherwise
     */
    private boolean hasBudgetForPurpose(MitoHousehold household) {
        return household.getTravelTimeBudgetForPurpose(activityPurpose) > 0.;
    }

    private void updateBudgets(MitoHousehold household) {
        double ratio;
        if (idealBudgetSum == actualBudgetSum) {
            ratio = 1;
        } else {
            ratio = idealBudgetSum / actualBudgetSum;
        }
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(activityPurpose) / household.getTripsForPurpose(activityPurpose).size();
        mean = hhBudgetPerTrip * ratio;
    }

    private Location findOrigin(MitoHousehold household, MitoTrip trip) {
        final List<Location> possibleBaseZones = new ArrayList<>();
        for (Purpose activityPurpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(activityPurpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleBaseZones.isEmpty()) {
            return MitoUtil.select(random, possibleBaseZones);
        }
        if (trip.getPerson().getMitoOccupationStatus() == relatedMitoOccupationStatus &&
            trip.getPerson().getOccupation() != null) {
            return trip.getPerson().getOccupation();
        }

        final Purpose selectedPurpose = MitoUtil.select(random, priorPurposes);
        return findRandomOrigin(household, selectedPurpose);
    }

    private MitoZone findDestination(int origin) {
        final IndexedDoubleMatrix1D row = baseProbabilities.get(activityPurpose).viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        IntStream.range(0, baseProbs.length).parallel().forEach(i -> {
            //divide travel time by 2 as home based trips' budget account for the return trip as well
            double diff = travelTimes.getTravelTime(zonesCopy.get(origin), zonesCopy.get(row.getIdForInternalIndex(i)), peakHour, "car") - mean;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            baseProbs[i] = baseProbs[i] * factor;
        });

        int destinationInternalId = MitoUtil.select(baseProbs, random);
        return zonesCopy.get(row.getIdForInternalIndex(destinationInternalId));
    }


    private MitoZone findDestinationWithoutBudget(int origin) {
        final IndexedDoubleMatrix1D row = baseProbabilities.get(activityPurpose).viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        int destinationInternalId = MitoUtil.select(baseProbs, random);
        return zonesCopy.get(row.getIdForInternalIndex(destinationInternalId));
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.completelyRandomNhbTrips.incrementAndGet();
        final IndexedDoubleMatrix1D originProbabilities = baseProbabilities.get(priorPurpose).viewRow(household.getHomeZone().getId());
        final int destinationInternalId = MitoUtil.select(originProbabilities.toNonIndexedArray(), random);
        return zonesCopy.get(originProbabilities.getIdForInternalIndex(destinationInternalId));
    }

    private void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin(),
                trip.getTripDestination(), peakHour, "car");
        idealBudgetSum += hhBudgetPerTrip;
    }
}
