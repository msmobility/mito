package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

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

    private final double peakHour;
    private final Purpose purpose;
    private final DoubleMatrix2D baseProbabilities;
    private final TravelTimes travelTimes;
    private final DataSet dataSet;
    private final Map<Integer, MitoZone> zonesCopy;
    private final double[] destinationProbabilities;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double hhBudgetPerTrip;
    private double adjustedBudget;

    private HbsHboDistribution(Purpose purpose, DoubleMatrix2D baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
        this.destinationProbabilities = new double[baseProbabilities.columns()];
        this.travelTimes = dataSet.getTravelTimes();
        this.peakHour = dataSet.getPeakHour();
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
            Coord coord = new Coord(household.getHomeLocation().getCoordinate().x, household.getHomeLocation().getCoordinate().y);
            if (hasTripsForPurpose(household)) {
                if(hasBudgetForPurpose(household)) {
                    updateBudgets(household);
                    updateDestinationProbabilities(household.getHomeZone().getId());
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        trip.setTripOrigin(household.getHomeZone());
                        
                        trip.setTripOriginCoord(coord);
                        MitoZone zone = findDestination();
                        trip.setTripDestination(zone);
                        trip.setTripDestinationCoord(zone.getRandomCoord());
                        if(zone == null) {
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
        actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(), peakHour, "car") * 2;
        idealBudgetSum += hhBudgetPerTrip;
    }

    /**
     * Copy probabilities for every destination for the current home origin
     */
    private void updateDestinationProbabilities(int origin) {
        double[] baseProbs = baseProbabilities.viewRow(origin).toArray();
        IntStream.range(0, destinationProbabilities.length).parallel().forEach(i -> {
            //multiply travel time by 2 as home based trips' budget account for the return trip as well
            double diff = travelTimes.getTravelTime(origin, i, peakHour, "car") *2 - adjustedBudget;
            double factor = SQRT_INV * FastMath.exp(-(diff * diff) / VARIANCE_DOUBLED);
            destinationProbabilities[i] =  baseProbs[i] * factor;
        });
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
        final int destination = MitoUtil.select(destinationProbabilities, random);
        return zonesCopy.get(destination);
    }
}

