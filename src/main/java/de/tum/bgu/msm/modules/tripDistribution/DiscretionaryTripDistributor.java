package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Table;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DiscretionaryTripDistributor extends TripDistributor {

    private final static Logger logger = Logger.getLogger(DiscretionaryTripDistributor.class);

    private final TravelTimes travelTimes;

    private final NormalDistribution distribution = new NormalDistribution(100, 50);
    private final Map<Integer, Double> densityByDeviation = new HashMap<>();

    private double ratio = 1;
    private double idealBudgetSum = 0;
    private double actualBudgetSum = 0;

    private double hhBudgetPerTrip;
    private double adjustedBudget;
    private int originZoneId;

    private final Map<Integer, double[]> destinationProbabilities;
    private final Map<Integer, double[]> destinationTravelTimes;
    private final int[] destinations;

    private final double[] tempProbabilities;
    private int index;

    private int noBudgetFailures = 0;

    public DiscretionaryTripDistributor(Purpose purpose, DataSet dataSet, Table<Integer, Integer, Double> utilityMatrix) {
        super(purpose, dataSet, utilityMatrix);
        this.travelTimes = dataSet.getTravelTimes("car");
        destinationProbabilities = new HashMap<>(utilityMatrix.columnKeySet().size());
        destinationTravelTimes = new HashMap<>(utilityMatrix.columnKeySet().size());
        destinations = new int[utilityMatrix.columnMap().size()];
        tempProbabilities = new double[utilityMatrix.columnMap().size()];
        int i = 0;
        for (Integer destination : utilityMatrix.columnMap().keySet()) {
            destinations[i] = destination;
            i++;
        }
        for (Integer origin : utilityMatrix.rowKeySet()) {
            double[] arrayProbabilities = new double[utilityMatrix.columnMap().size()];
            double[] arrayTravelTimes = new double[utilityMatrix.columnMap().size()];
            for (int j = 0; j < destinations.length; j++) {
                arrayProbabilities[j] = utilityMatrix.get(origin, destinations[j]);
                arrayTravelTimes[j] = travelTimes.getTravelTime(origin, destinations[j]);
            }
            destinationProbabilities.put(origin, arrayProbabilities);
            destinationTravelTimes.put(origin, arrayTravelTimes);
        }
    }

    @Override
    protected void handleHousehold(MitoHousehold household) {
        hhBudgetPerTrip = household.getTravelTimeBudgetForPurpose(purpose) / household.getTripsForPurpose(purpose).size();
        if (hhBudgetPerTrip == 0.) {
            noBudgetFailures++;
            return;
        }
        originZoneId = household.getHomeZone().getZoneId();
        adjustedBudget = (hhBudgetPerTrip * ratio) / 100;
        updateProbabilities();
        super.handleHousehold(household);
        ratio = (idealBudgetSum / actualBudgetSum);
    }

    @Override
    protected void distributeHouseholdTrips(MitoHousehold household) {
        for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
            trip.setTripOrigin(household.getHomeZone());
            index = MitoUtil.select(tempProbabilities, random);
            trip.setTripDestination(dataSet.getZones().get(destinations[index]));
            TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
            actualBudgetSum += destinationTravelTimes.get(originZoneId)[index];
            idealBudgetSum += hhBudgetPerTrip;
        }
    }

    private void updateProbabilities() {
        for (int i = 0; i < tempProbabilities.length; i++) {
            tempProbabilities[i] = destinationProbabilities.get(originZoneId)[i]; //* getDensity((int)((destinationTravelTimes.get(originZoneId)[i]) / adjustedBudget));
        }
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
