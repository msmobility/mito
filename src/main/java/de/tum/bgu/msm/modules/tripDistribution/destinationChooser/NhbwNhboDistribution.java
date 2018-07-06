package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
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
    private final Occupation relatedOccupation;
    private final EnumMap<Purpose, DoubleMatrix2D> baseProbabilities;
    private final double[] destinationProbabilities;
    private final DataSet dataSet;
    private final TravelTimes travelTimes;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private final Map<Integer, MitoZone> zonesCopy;
    private double mean;

    private NhbwNhboDistribution(Purpose purpose, List<Purpose> priorPurposes, Occupation relatedOccupation,
                                 EnumMap<Purpose, DoubleMatrix2D> baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedOccupation = relatedOccupation;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
        this.destinationProbabilities = new double[baseProbabilities.values().iterator().next().columns()];
        this.travelTimes = dataSet.getTravelTimes();
        this.peakHour = dataSet.getPeakHour();
    }

    public static NhbwNhboDistribution nhbw(EnumMap<Purpose, DoubleMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBW, Collections.singletonList(Purpose.HBW),
                Occupation.WORKER, baseProbabilites, dataSet);
    }

    public static NhbwNhboDistribution nhbo(EnumMap<Purpose, DoubleMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS),
                null, baseProbabilites, dataSet);
    }

    @Override
    public Void call() throws Exception {
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
                        MitoZone origin = findOrigin(household, trip);
                        if (origin == null) {
                            logger.debug("No origin found for trip" + trip);
                            TripDistribution.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        trip.setTripOrigin(origin);
                        trip.setTripOriginCoord(origin.getRandomCoord());
                        MitoZone destination = findDestination(trip.getTripOrigin().getId());
                        trip.setTripDestination(destination);
                        trip.setTripDestinationCoord(destination.getRandomCoord());
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

    private MitoZone findOrigin(MitoHousehold household, MitoTrip trip) {
        final List<MitoZone> possibleBaseZones = new ArrayList<>();
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
        if (trip.getPerson().getOccupation() == relatedOccupation && trip.getPerson().getOccupationZone() != null) {
            return trip.getPerson().getOccupationZone();
        }

        final Purpose selectedPurpose = MitoUtil.select(random, priorPurposes);
        return findRandomOrigin(household, selectedPurpose);
    }

    private MitoZone findDestination(int origin) {
        double[] baseProbs = baseProbabilities.get(purpose).viewRow(origin).toArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            double factor;
            //divide travel time by 2 as home based trips' budget account for the return trip as well
            double diff = travelTimes.getTravelTime(origin, i, peakHour, "car") - mean;
            factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinationProbabilities[i] = baseProbs[i] * factor;
        });

        int destination = MitoUtil.select(baseProbs, random);
        return zonesCopy.get(destination);
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.completelyRandomNhbTrips.incrementAndGet();
        final DoubleMatrix1D originProbabilities = baseProbabilities.get(priorPurpose).viewRow(household.getHomeZone().getId());
        final int destination = MitoUtil.select(originProbabilities.toArray(), random);
        return zonesCopy.get(destination);
    }

    private void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin().getId(),
                trip.getTripDestination().getId(), peakHour, "car");
        idealBudgetSum += hhBudgetPerTrip;
    }
}
