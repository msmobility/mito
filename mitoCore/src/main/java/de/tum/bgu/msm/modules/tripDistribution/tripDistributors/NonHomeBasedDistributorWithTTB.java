package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Nico
 */
public class NonHomeBasedDistributorWithTTB extends NonHomeBasedDistributor {

    private final static double VARIANCE_DOUBLED = 500 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);
    private final TravelTimes travelTimes;
    private final IndexedDoubleMatrix2D probabilityMatrix;
    private final double peakHour;
    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private double adjustedBudget;

    private int counter = 0;

    public NonHomeBasedDistributorWithTTB(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                          EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData) {
        super(purpose, householdCollection, dataSet, distributionData, null);
        if(distributionData.get(purpose).size() > 1) {
            throw new RuntimeException("TTB distributors cannot be used with multi-category distribution!");
        }
        this.probabilityMatrix = distributionData.get(purpose).get(0).getUtilityMatrix();
        this.travelTimes = dataSet.getTravelTimes();
        this.peakHour = dataSet.getPeakHour();
    }

    @Override
    protected boolean initialiseHousehold(MitoHousehold household) {

        if(LongMath.isPowerOfTwo(counter)) {
            logger.info(counter + " households done for Purpose " + super.purpose
                    + ".\nIdeal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
        }
        counter++;

        if(!household.getTripsForPurpose(purpose).isEmpty() && (household.getTravelTimeBudgetForPurpose(purpose) > 0.)) {
            updateTravelTimeBudgets(household);
            return true;
        } else {
            return false;
        }
    }

    protected void updateTravelTimeBudgets(MitoHousehold household) {
        double ratio;
        if (idealBudgetSum == actualBudgetSum) {
            ratio = 1;
        } else {
            ratio = idealBudgetSum / actualBudgetSum;
        }
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(purpose) / household.getTripsForPurpose(purpose).size();
        adjustedBudget = hhBudgetPerTrip * ratio;
    }

    @Override
    protected Location findDestination(MitoTrip trip, int categoryIndex) {
        int origin = trip.getTripOrigin().getZoneId();
        final IndexedDoubleMatrix1D row = probabilityMatrix.viewRow(origin);
        double[] destinationProbabilities = row.toNonIndexedArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            //divide travel time by 2 as home based trips' budget account for the return trip as well
            double diff = travelTimes.getTravelTime(zonesCopy.get(origin), zonesCopy.get(row.getIdForInternalIndex(i)), peakHour, "car") - adjustedBudget;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinationProbabilities[i] = destinationProbabilities[i] * factor;
        });

        int destinationInternalId = MitoUtil.select(destinationProbabilities, random);
        return zonesCopy.get(row.getIdForInternalIndex(destinationInternalId));
    }

    @Override
    protected void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin(),
                trip.getTripDestination(), peakHour, "car");
        idealBudgetSum += hhBudgetPerTrip;
    }
}
