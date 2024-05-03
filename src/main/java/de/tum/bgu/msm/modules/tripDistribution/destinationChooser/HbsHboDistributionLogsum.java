package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
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
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Nico
 */
public class HbsHboDistributionLogsum extends RandomizableConcurrentFunction<Void> {

    private final static double VARIANCE_DOUBLED = 30 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);

    private final static Logger logger = Logger.getLogger(HbsHboDistributionLogsum.class);
    private final boolean USE_BUDGETS_IN_DESTINATION_CHOICE;

    private final double peakHour;
    private final Purpose purpose;
    private final IndexedDoubleMatrix2D baseProbabilitiesEV;
    private final IndexedDoubleMatrix2D baseProbabilitiesNoEV;
    private final TravelTimes travelTimes;

    private final Collection<MitoHousehold> householdPartition;
    private final Map<Integer, MitoZone> zonesCopy;

    private final double[] destinationProbabilities;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;
    private double adjustedBudget;

    private HbsHboDistributionLogsum(boolean useBudgetsInDestinationChoice, Purpose purpose, IndexedDoubleMatrix2D baseProbabilitiesEV,
                                     IndexedDoubleMatrix2D baseProbabilitiesNoEV,
                                     Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                     TravelTimes travelTimes, double peakHour) {
        super(MitoUtil.getRandomObject().nextLong());
        USE_BUDGETS_IN_DESTINATION_CHOICE = useBudgetsInDestinationChoice;
        this.purpose = purpose;
        this.householdPartition = householdPartition;
        this.baseProbabilitiesEV = baseProbabilitiesEV;
        this.baseProbabilitiesNoEV = baseProbabilitiesNoEV;
        this.zonesCopy = new HashMap<>(zones);
        this.destinationProbabilities = new double[baseProbabilitiesEV.columns()];
        this.travelTimes = travelTimes;
        this.peakHour = peakHour;
    }

    public static HbsHboDistributionLogsum hbs(IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                               TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistributionLogsum(useBudgetsInDestinationChoice, Purpose.HBS, baseProbabilitiesEV, baseProbabilitiesNoEV, householdPartition, zones, travelTimes, peakHour);
    }

    public static HbsHboDistributionLogsum hbo(IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                               TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistributionLogsum(useBudgetsInDestinationChoice, Purpose.HBO, baseProbabilitiesEV, baseProbabilitiesNoEV, householdPartition, zones, travelTimes, peakHour);
    }

    public static HbsHboDistributionLogsum hbr(IndexedDoubleMatrix2D baseProbabilitiesEV, IndexedDoubleMatrix2D baseProbabilitiesNoEV, Collection<MitoHousehold> householdPartition, Map<Integer, MitoZone> zones,
                                               TravelTimes travelTimes, double peakHour, boolean useBudgetsInDestinationChoice) {
        return new HbsHboDistributionLogsum(useBudgetsInDestinationChoice, Purpose.HBR, baseProbabilitiesEV, baseProbabilitiesNoEV, householdPartition, zones, travelTimes, peakHour);
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
                        updateDestinationProbabilities(household.getHomeZone().getId(), household.isHasEV());
                        for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                            trip.setTripOrigin(household);
                            MitoZone zone = findDestination(household.isHasEV());
                            trip.setTripDestination(zone);

                            if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)){
                                Coord destinationCoord = CoordUtils.createCoord(zone.getRandomCoord(MitoUtil.getRandomObject()));
                                trip.setDestinationCoord(destinationCoord);
                            }

                            if (zone == null) {
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
                        trip.setTripOrigin(household);
                        updateDestinationProbabilitiesWithoutBudgets(household.getHomeZone().getId(), household.isHasEV());
                        MitoZone zone = findDestination(household.isHasEV());
                        trip.setTripDestination(zone);

                        if(Resources.instance.getBoolean(Properties.FILL_MICRO_DATA_WITH_MICROLOCATION,false)){
                            Coord destinationCoord = CoordUtils.createCoord(zone.getRandomCoord(MitoUtil.getRandomObject()));
                            trip.setDestinationCoord(destinationCoord);
                        }

                        if (zone == null) {
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

    private void updateDestinationProbabilities(int origin, boolean isHouseholdEV) {
        IndexedDoubleMatrix2D baseProbabilities = isHouseholdEV ? baseProbabilitiesEV : baseProbabilitiesNoEV;
        final IndexedDoubleMatrix1D row = baseProbabilities.viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            //multiply travel time by 2 as home based trips' budget account for the return trip as well

            double diff = travelTimes.getTravelTime(zonesCopy.get(origin), zonesCopy.get(row.getIdForInternalIndex(i)), peakHour, "car") * 2 - adjustedBudget;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinationProbabilities[i] = baseProbs[i] * factor;
        });
    }

    private void updateDestinationProbabilitiesWithoutBudgets(int origin, boolean isHouseholdEV) {
        IndexedDoubleMatrix2D baseProbabilities = isHouseholdEV ? baseProbabilitiesEV : baseProbabilitiesNoEV;
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

    private MitoZone findDestination(boolean isHouseholdEV) {

        final int destinationInternalIndex = MitoUtil.select(destinationProbabilities, random);

        IndexedDoubleMatrix2D baseProbabilities = isHouseholdEV ? baseProbabilitiesEV : baseProbabilitiesNoEV;

        // Get the actual zone ID using the internal index from the base probabilities matrix
        int zoneID = baseProbabilities.getIdForInternalColumnIndex(destinationInternalIndex);

        // Retrieve and return the MitoZone object corresponding to the zone ID from zonesCopy map
        return zonesCopy.get(zoneID);
    }
}

