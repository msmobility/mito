package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistributionLogsumEVnoEV;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.*;
import java.util.stream.IntStream;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public final class NhbwNhboDistributionLogsum extends RandomizableConcurrentFunction<Void> {

    private final static double VARIANCE_DOUBLED = 500 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);
    private final boolean USE_BUDGETS_IN_DESTINATION_CHOICE;

    private final static Logger logger = Logger.getLogger(HbsHboDistributionLogsum.class);

    private final double peakHour;

    private final Purpose purpose;
    private final List<Purpose> priorPurposes;
    private final MitoOccupationStatus relatedMitoOccupationStatus;
    private final EnumMap<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>> baseProbabilities;
    private final TravelTimes travelTimes;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;

    private double mean;

    private NhbwNhboDistributionLogsum(boolean useBudgetsInDestinationChoice, Purpose purpose, List<Purpose> priorPurposes, MitoOccupationStatus relatedMitoOccupationStatus,
                                       EnumMap<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>> baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                       TravelTimes travelTimes, double peakHour) {
        super(MitoUtil.getRandomObject().nextLong());
        USE_BUDGETS_IN_DESTINATION_CHOICE = useBudgetsInDestinationChoice;
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedMitoOccupationStatus = relatedMitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(zones);
        this.travelTimes = travelTimes;
        this.peakHour = peakHour;
        this.householdPartition = householdPartition;
    }

    public static NhbwNhboDistributionLogsum nhbw(EnumMap<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>> baseProbabilites, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                                  TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new NhbwNhboDistributionLogsum(useBudgetsInDestinationChoice, Purpose.NHBW, Collections.singletonList(Purpose.HBW),
                MitoOccupationStatus.WORKER, baseProbabilites, householdPartition, zones, travelTimes, peakHour);
    }

    public static NhbwNhboDistributionLogsum nhbo(EnumMap<Purpose, Tuple<IndexedDoubleMatrix2D, IndexedDoubleMatrix2D>> baseProbabilites, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                                  TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new NhbwNhboDistributionLogsum(useBudgetsInDestinationChoice, Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS, HBR),
                null, baseProbabilites, householdPartition, zones, travelTimes, peakHour);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MitoHousehold household : householdPartition) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose
                        + "\nIdeal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
            }
            if (hasTripsForPurpose(household)) {
                IndexedDoubleMatrix2D selectedMatrix = household.isHasEV() ? baseProbabilities.get(purpose).getFirst() : baseProbabilities.get(purpose).getSecond();
                if (USE_BUDGETS_IN_DESTINATION_CHOICE){
                    if (hasBudgetForPurpose(household)) {
                        updateBudgets(household);
                        for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                            Location origin = findOrigin(household, trip);
                            if (origin == null) {
                                logger.debug("No origin found for trip" + trip);
                                TripDistributionLogsumEVnoEV.failedTripsCounter.incrementAndGet();
                                continue;
                            }
                            trip.setTripOrigin(origin);

                            if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)&& (origin instanceof MitoZone)){
                                Coord originCoord = CoordUtils.createCoord(((MitoZone)origin).getRandomCoord(MitoUtil.getRandomObject()));
                                trip.setOriginCoord(originCoord);
                            }

                            MitoZone destination = findDestination(trip.getTripOrigin().getZoneId(),selectedMatrix);
                            trip.setTripDestination(destination);

                            if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)){
                                Coord destinationCoord = CoordUtils.createCoord(destination.getRandomCoord(MitoUtil.getRandomObject()));
                                trip.setDestinationCoord(destinationCoord);
                            }

                            if (destination == null) {
                                logger.debug("No destination found for trip" + trip);
                                TripDistributionLogsumEVnoEV.failedTripsCounter.incrementAndGet();
                                continue;
                            }
                            postProcessTrip(trip);
                            TripDistributionLogsumEVnoEV.distributedTripsCounter.incrementAndGet();
                        }
                    } else {
                        TripDistributionLogsumEVnoEV.failedTripsCounter.incrementAndGet();
                    }
                } else {
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        Location origin = findOrigin(household, trip);
                        if (origin == null) {
                            logger.debug("No origin found for trip" + trip);
                            TripDistributionLogsumEVnoEV.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        trip.setTripOrigin(origin);
                        if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)&& (origin instanceof MitoZone)){
                            Coord originCoord = CoordUtils.createCoord(((MitoZone)origin).getRandomCoord(MitoUtil.getRandomObject()));
                            trip.setOriginCoord(originCoord);
                        }

                        MitoZone destination = findDestinationWithoutBudget(trip.getTripOrigin().getZoneId(), selectedMatrix);
                        trip.setTripDestination(destination);

                        if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)){
                            Coord destinationCoord = CoordUtils.createCoord(destination.getRandomCoord(MitoUtil.getRandomObject()));
                            trip.setDestinationCoord(destinationCoord);
                        }

                        if (destination == null) {
                            logger.debug("No destination found for trip" + trip);
                            TripDistributionLogsumEVnoEV.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        TripDistributionLogsumEVnoEV.distributedTripsCounter.incrementAndGet();
                    }
                }

            }
            counter++;
        }
        logger.info("Ideal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
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
    private boolean hasBudgetForPurpose(MitoHousehold household) {
        return household.getTravelTimeBudgetForPurpose(purpose) > 0.;
    }

    private void updateBudgets(MitoHousehold household) {
        double ratio;
        if (idealBudgetSum == actualBudgetSum) {
            ratio = 1;
        } else {
            ratio = idealBudgetSum / actualBudgetSum;
        }
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(purpose) / household.getTripsForPurpose(purpose).size();
        mean = hhBudgetPerTrip * ratio;
    }

    private Location findOrigin(MitoHousehold household, MitoTrip trip) {
        final List<Location> possibleBaseZones = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(purpose)) {
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

    private MitoZone findDestination(int origin,IndexedDoubleMatrix2D matrix) {
        final IndexedDoubleMatrix1D row = matrix.viewRow(origin);
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


    private MitoZone findDestinationWithoutBudget(int origin, IndexedDoubleMatrix2D matrix) {
        final IndexedDoubleMatrix1D row = matrix.viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        int destinationInternalId = MitoUtil.select(baseProbs, random);
        return zonesCopy.get(row.getIdForInternalIndex(destinationInternalId));
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistributionLogsumEVnoEV.completelyRandomNhbTrips.incrementAndGet();
        IndexedDoubleMatrix2D matrix = household.isHasEV() ? baseProbabilities.get(priorPurpose).getFirst() : baseProbabilities.get(priorPurpose).getSecond();
        final IndexedDoubleMatrix1D originProbabilities = matrix.viewRow(household.getHomeZone().getId());
        final int destinationInternalId = MitoUtil.select(originProbabilities.toNonIndexedArray(), random);
        return zonesCopy.get(originProbabilities.getIdForInternalIndex(destinationInternalId));
    }

    private void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin(),
                trip.getTripDestination(), peakHour, "car");
        idealBudgetSum += hhBudgetPerTrip;
    }
}
