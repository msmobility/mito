package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public class HbsHboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    protected final Purpose purpose;
    final DoubleMatrix2D baseProbabilities;
    protected final TravelTimes travelTimes;
    protected final DataSet dataSet;
    private final Map<Integer, MitoZone> zonesCopy;
    private DoubleMatrix1D destinationProbabilities;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double adjustedBudget;
    private double hhBudgetPerTrip;

    private final NormalDistribution distribution = new NormalDistribution(100, 50);
    private final Map<Integer, Double> densityByDeviation = new HashMap<>();

    private HbsHboDistribution(Purpose purpose, DoubleMatrix2D baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.travelTimes = dataSet.getTravelTimes();
        this.purpose = purpose;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static HbsHboDistribution hbs(DoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbsHboDistribution(Purpose.HBS, baseprobabilities, dataSet);
    }

    public static HbsHboDistribution hbo(DoubleMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbsHboDistribution(Purpose.HBO, baseprobabilities, dataSet);
    }

    @Override
    public Void call() throws Exception {
        long counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if (hasTripsForPurpose(household)) {
                if(hasBudgetForPurpose(household)) {
                    copyBaseDestinationProbabilities(household);
                    updateBudgets(household);
                    adjustDestinationProbabilities(household.getHomeZone());
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        trip.setTripOrigin(household.getHomeZone());
                        trip.setTripDestination(findDestination());
                        postProcessTrip(trip);
                        TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
                    }
                } else {
                    TripDistribution.FAILED_TRIPS_COUNTER.incrementAndGet();
                }
            }
            counter++;
        }
        return null;
    }

    private void adjustDestinationProbabilities(MitoZone origin){
        for (int i = 0; i < destinationProbabilities.size(); i++) {
            final int deviation = (int) (travelTimes.getTravelTime(origin.getId(), i, dataSet.getPeakHour(), "car") / adjustedBudget);
            destinationProbabilities.setQuick(i, destinationProbabilities.getQuick(i) * getDensity(deviation));
        }
    }

    /**
     * Checks if members of this household perform trips of the set purpose
     * @return true if trips are available, false otherwise
     */
    private boolean hasTripsForPurpose(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    /**
     * Checks if this household has been allocated travel time budget for the set purpose
     * @return true if budget was allocated, false otherwise
     */
    private boolean hasBudgetForPurpose(MitoHousehold household) {
        return household.getTravelTimeBudgetForPurpose(purpose) > 0.;
    }

    private void postProcessTrip(MitoTrip trip) {
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(),
                dataSet.getPeakHour(), TransportMode.car);
        idealBudgetSum += hhBudgetPerTrip;
    }

    /**
     * Copy probabilities for every destination for the current home origin (only applies to home based purposes)
     * @param household
     */
    private void copyBaseDestinationProbabilities(MitoHousehold household) {
        destinationProbabilities = baseProbabilities.viewRow(household.getHomeZone().getId()).copy();
    }

    private void updateBudgets(MitoHousehold household) {
        double ratio;
        if(idealBudgetSum == actualBudgetSum) {
            ratio = 1;
        } else {
            ratio = idealBudgetSum / actualBudgetSum;
        }
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(purpose) / household.getTripsForPurpose(purpose).size();
        adjustedBudget = (hhBudgetPerTrip * ratio) / 100;
    }

    private MitoZone findDestination() {
        final int destination = MitoUtil.select(destinationProbabilities.toArray(), random, destinationProbabilities.zSum());
        return zonesCopy.get(destination);
    }

    private double getDensity(int deviation) {
        if (densityByDeviation.containsKey(deviation)) {
            return densityByDeviation.get(deviation);
        } else {
            double density = distribution.density(deviation);
            densityByDeviation.put(deviation, density);
            return density;
        }
    }
}

