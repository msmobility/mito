package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public class HbsHboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final DoubleMatrix2D baseProbabilities;
    private final TravelTimes travelTimes;
    private final DataSet dataSet;
    private final Map<Integer, MitoZone> zonesCopy;
    private DoubleMatrix1D destinationProbabilities;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;

    private final Normal distribution = new Normal(0, 0.2, DoubleRandomEngine.makeDefault());
    private double adjustedBudget;

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
                logger.info(counter + " households done for Purpose " + purpose
                        + "\nIdeal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
            }
            if (hasTripsForPurpose(household)) {
                if(hasBudgetForPurpose(household)) {
                    copyBaseDestinationProbabilities(household);
                    updateBudgets(household);
                    adjustDestinationProbabilities(household.getHomeZone());
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        trip.setTripOrigin(household.getHomeZone());
                        MitoZone destination = findDestination();
                        trip.setTripDestination(destination);
                        if(destination == null) {
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
        return null;
    }

    private void adjustDestinationProbabilities(MitoZone origin){
        for (int i = 0; i < destinationProbabilities.size(); i++) {
            //divide travel time by 2 as home based trips' budget account for the return trip as well
            double travelTime = travelTimes.getTravelTime(origin.getId(), i, dataSet.getPeakHour(), "car") / 2.;
            final double density = distribution.pdf((adjustedBudget - travelTime) / adjustedBudget);
            destinationProbabilities.setQuick(i, destinationProbabilities.getQuick(i) * density);
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
     * Copy probabilities for every destination for the current home origin
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
        adjustedBudget = hhBudgetPerTrip * ratio;
    }

    private MitoZone findDestination() {
        final int destination = MitoUtil.select(destinationProbabilities.toArray(), random, destinationProbabilities.zSum());
        return zonesCopy.get(destination);
    }
}

