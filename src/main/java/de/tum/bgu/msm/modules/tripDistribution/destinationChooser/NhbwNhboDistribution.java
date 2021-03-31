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

    private final static double VARIANCE_DOUBLED = 10 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final double peakHour;

    private final Purpose purpose;
    private final List<Purpose> priorPurposes;
    private final MitoOccupationStatus relatedMitoOccupationStatus;
    private final EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities;
    private final DataSet dataSet;
    private final TravelTimes travelTimes;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private final Map<Integer, MitoZone> zonesCopy;
    private double mean;

    private NhbwNhboDistribution(Purpose purpose, List<Purpose> priorPurposes, MitoOccupationStatus relatedMitoOccupationStatus,
                                 EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedMitoOccupationStatus = relatedMitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
        this.travelTimes = dataSet.getTravelTimes();
        this.peakHour = dataSet.getPeakHour();
    }

    public static NhbwNhboDistribution nhbw(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBW, Collections.singletonList(Purpose.HBW),
                MitoOccupationStatus.WORKER, baseProbabilites, dataSet);
    }

    public static NhbwNhboDistribution nhbo(EnumMap<Purpose, IndexedDoubleMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS),
                null, baseProbabilites, dataSet);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose
                        + "\nIdeal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
            }
            if (hasTripsForPurpose(household)) {
                if (hasBudgetForPurpose(household)) {
                    updateBudgets(household);
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        if (!Mode.walk.equals(trip.getTripMode())) {
                            if(trip.getTripOrigin() == null) {
                                Location origin = findOrigin(household, trip);
                                if (origin == null) {
                                    logger.debug("No origin found for trip" + trip);
                                    TripDistribution.failedTripsCounter.incrementAndGet();
                                    continue;
                                }
                                trip.setTripOrigin(origin);
                            }
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
                    }
                } else {
                    TripDistribution.failedTripsCounter.incrementAndGet();
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

    private MitoZone findDestination(int origin) {
        final IndexedDoubleMatrix1D row = baseProbabilities.get(purpose).viewRow(origin);
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
