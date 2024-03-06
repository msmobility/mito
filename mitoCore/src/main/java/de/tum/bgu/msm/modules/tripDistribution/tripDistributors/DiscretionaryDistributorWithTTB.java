package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.commons.math3.util.FastMath;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Nico
 */
public class DiscretionaryDistributorWithTTB extends AbstractDistributor {

    private final static double VARIANCE_DOUBLED = 30 * 2;
    private final static double SQRT_INV = 1.0 / Math.sqrt(Math.PI * VARIANCE_DOUBLED);
    private final TravelTimes travelTimes;
    private final IndexedDoubleMatrix2D probabilityMatrix;
    private final double peakHour;
    private double[] transformedDestinationProbabilities;
    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;
    private double adjustedBudget;
    private int counter = 0;

    public DiscretionaryDistributorWithTTB(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                           EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData) {
        super(purpose, householdCollection, dataSet, distributionData, null);
        if(distributionData.get(purpose).size() > 1) {
            throw new RuntimeException("TTB distributors cannot be combined with multi-category distribution!");
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
            transformedDestinationProbabilities = getTransformedDestinationProbabilities(household);
            return true;
        } else {
            transformedDestinationProbabilities = null;
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

    protected double[] getTransformedDestinationProbabilities(MitoHousehold household) {
        int origin = household.getHomeZone().getId();
        final IndexedDoubleMatrix1D row = probabilityMatrix.viewRow(origin);
        double[] baseProbs = row.toNonIndexedArray();
        double[] destinatinonProbs = new double[baseProbs.length];
        IntStream.range(0, destinatinonProbs.length).parallel().forEach(i -> {
            //multiply travel time by 2 as home based trips' budget account for the return trip as well
            double diff = travelTimes.getTravelTime(zonesCopy.get(origin), zonesCopy.get(row.getIdForInternalIndex(i)), peakHour, "car") * 2 - adjustedBudget;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinatinonProbs[i] = baseProbs[i] * factor;
        });
        return destinatinonProbs;
    }

    @Override
    protected Location findDestination(MitoTrip trip, int categoryIndex) {
        final int destinationInternalIndex = MitoUtil.select(transformedDestinationProbabilities, random);
        return zonesCopy.get(probabilityMatrix.getIdForInternalColumnIndex(destinationInternalIndex));
    }

    @Override
    protected void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin(),
                trip.getTripDestination(), peakHour, "car") * 2;
        idealBudgetSum += hhBudgetPerTrip;
    }

}

