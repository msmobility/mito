package de.tum.bgu.msm.modules.tripDistribution.destinationChooser;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.*;

import static de.tum.bgu.msm.data.Purpose.*;

public final class NhbwNhboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Normal distribution = new Normal(1, 0.15, DoubleRandomEngine.makeDefault());

    private final Purpose purpose;
    private final List<Purpose> priorPurposes;
    private final Occupation relatedOccupation;
    private final EnumMap<Purpose, DoubleMatrix2D> baseProbabilities;
    private DoubleMatrix1D destinationProbabilities;
    private final DataSet dataSet;
    private final TravelTimes travelTimes;

    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;
    private double adjustedBudget;
    private double hhBudgetPerTrip;

    private final Map<Integer, MitoZone> zonesCopy;

    private NhbwNhboDistribution(Purpose purpose, List<Purpose> priorPurposes, Occupation relatedOccupation,
                                 EnumMap<Purpose, DoubleMatrix2D> baseProbabilities, DataSet dataSet) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedOccupation = relatedOccupation;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
        travelTimes = dataSet.getTravelTimes();
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
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if (hasTripsForPurpose(household)) {
                if(hasBudgetForPurpose(household)) {
                    updateBudgets(household);
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        trip.setTripOrigin(findOrigin(household, trip));
                        trip.setTripDestination(findDestination(trip));
                        postProcessTrip(trip);
                        TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
                    }
                } else {
                    TripDistribution.FAILED_TRIPS_COUNTER.incrementAndGet();
                }
            }
            counter++;
        }
        logger.info("Ideal budget sum: " + idealBudgetSum + " | actual budget sum: " + actualBudgetSum);
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

    private MitoZone findDestination(MitoTrip trip) {
        destinationProbabilities = baseProbabilities.get(purpose).viewRow(trip.getTripOrigin().getId()).copy();
        adjustDestinationProbabilities(trip.getTripOrigin());
        final int destination = MitoUtil.select(destinationProbabilities.toArray(), random, destinationProbabilities.zSum());
        return zonesCopy.get(destination);
    }

    private void adjustDestinationProbabilities(MitoZone origin){
        for (int i = 0; i < destinationProbabilities.size(); i++) {
            double travelTime = (travelTimes.getTravelTime(origin.getId(), i, dataSet.getPeakHour(), "car"));
            final double ratio = travelTime / adjustedBudget;
            final double density = distribution.pdf(ratio);
            final double newProb = Math.max(destinationProbabilities.getQuick(i) * density, Double.MIN_VALUE);

            destinationProbabilities.setQuick(i, newProb);
        }
    }

    private MitoZone findRandomOrigin(MitoHousehold household, Purpose priorPurpose) {
        TripDistribution.COMPLETELY_RANDOM_NHB_TRIPS.incrementAndGet();
        final DoubleMatrix1D originProbabilities = baseProbabilities.get(priorPurpose).viewRow(household.getHomeZone().getId());
        final int destination = MitoUtil.select(originProbabilities.toArray(), random);
        return zonesCopy.get(destination);
    }

    private void postProcessTrip(MitoTrip trip) {
        try {
            actualBudgetSum += travelTimes.getTravelTime(trip.getTripOrigin().getId(), trip.getTripDestination().getId(),
                    dataSet.getPeakHour(), TransportMode.car);
        } catch (Exception e){
            logger.info("Not found destination for " + trip.getId());
        }

            idealBudgetSum += hhBudgetPerTrip;
    }
}
