package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Nico
 */
public class HbsHboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static double VARIANCE_DOUBLED = 30 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);
    private final boolean USE_BUDGETS_IN_DESTINATION_CHOICE;

    private final double peakHour;
    private final Purpose purpose;
    private final IndexedDoubleMatrix2D baseProbabilities;
    private final TravelTimes travelTimes;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;

    private final double[] destinationProbabilities;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;
    private double adjustedBudget;

    private HbsHboDistribution(boolean useBudgetsInDestinationChoice, Purpose purpose, IndexedDoubleMatrix2D baseProbabilities,
                               Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                               TravelTimes travelTimes, double peakHour) {
        super(MitoUtil.getRandomObject().nextLong());
        USE_BUDGETS_IN_DESTINATION_CHOICE = useBudgetsInDestinationChoice;
        this.purpose = purpose;
        this.householdPartition = householdPartition;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(zones);
        this.destinationProbabilities = new double[baseProbabilities.columns()];
        this.travelTimes = travelTimes;
        this.peakHour = peakHour;
    }

    public static HbsHboDistribution hbs(IndexedDoubleMatrix2D baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                         TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistribution(useBudgetsInDestinationChoice, Purpose.HBS, baseProbabilities, householdPartition, zones, travelTimes, peakHour);
    }

    public static HbsHboDistribution hbo(IndexedDoubleMatrix2D baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                         TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistribution(useBudgetsInDestinationChoice, Purpose.HBO, baseProbabilities, householdPartition, zones, travelTimes, peakHour);
    }

    public static HbsHboDistribution hbr(IndexedDoubleMatrix2D baseProbabilities, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                         TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistribution(useBudgetsInDestinationChoice, Purpose.HBR, baseProbabilities, householdPartition, zones, travelTimes, peakHour);
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
                if (USE_BUDGETS_IN_DESTINATION_CHOICE){
                    if (hasBudgetForPurpose(household)) {
                        updateBudgets(household);
                        updateDestinationProbabilities(household.getHomeZone().getId());
                        for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                            if(!Mode.walk.equals(trip.getTripMode())) {
                                trip.setTripOrigin(household);
                                MitoZone zone = findDestination();
                                trip.setTripDestination(zone);
                                if (zone == null) {
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
                } else {
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        if(!Mode.walk.equals(trip.getTripMode())) {
                            trip.setTripOrigin(household);
                            updateDestinationProbabilitiesWithoutBudgets(household.getHomeZone().getId());
                            MitoZone zone = findDestination();
                            trip.setTripDestination(zone);
                            if (zone == null) {
                                logger.debug("No destination found for trip" + trip);
                                TripDistribution.failedTripsCounter.incrementAndGet();
                                continue;
                            }
                            TripDistribution.distributedTripsCounter.incrementAndGet();
                        }
                    }
                }
            }
            counter++;
        }
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

    private void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin(),
                trip.getTripDestination(), peakHour, "car") * 2;
        idealBudgetSum += hhBudgetPerTrip;
    }

    /**
     * Copy probabilities for every destination for the current home origin
     */
    private void updateDestinationProbabilities(int origin) {
        final IndexedDoubleMatrix1D row = baseProbabilities.viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            //multiply travel time by 2 as home based trips' budget account for the return trip as well

            double diff = travelTimes.getTravelTime(zonesCopy.get(origin), zonesCopy.get(row.getIdForInternalIndex(i)), peakHour, "car") * 2 - adjustedBudget;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinationProbabilities[i] = baseProbs[i] * factor;
        });
    }

    private void updateDestinationProbabilitiesWithoutBudgets(int origin) {
        final IndexedDoubleMatrix1D row = baseProbabilities.viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            destinationProbabilities[i] = baseProbs[i];
                });

    }

    private void updateBudgets(MitoHousehold household) {
        double ratio;
        if (idealBudgetSum == actualBudgetSum) {
            ratio = 1;
        } else {
            ratio = idealBudgetSum / actualBudgetSum;
        }
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(purpose) / household.getTripsForPurpose(purpose).size();
        adjustedBudget = hhBudgetPerTrip * ratio;
    }

    private MitoZone findDestination() {
        final int destinationInternalIndex = MitoUtil.select(destinationProbabilities, random);
        return zonesCopy.get(baseProbabilities.getIdForInternalColumnIndex(destinationInternalIndex));
    }
}

