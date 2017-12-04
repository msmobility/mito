package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Table;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class TripDistributor extends RandomizableConcurrentFunction {

    private final static Logger logger = Logger.getLogger(TripDistributor.class);

    protected final DataSet dataSet;
    protected final Purpose purpose;

    protected final Table<Integer, Integer, Double> utilityMatrix;
    protected Map<Integer, Double> probabilities;

    private long counter = 0;


    public TripDistributor(Purpose purpose, DataSet dataSet, Table<Integer, Integer, Double> utilityMatrix) {
        this.purpose = purpose;
        this.dataSet = dataSet;
        this.utilityMatrix = utilityMatrix;
        this.probabilities = new HashMap<>(utilityMatrix.columnKeySet().size());
    }

    @Override
    public void execute() {
        for(MitoHousehold household: dataSet.getHouseholds().values()) {
            if(LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if(household.getTripsForPurpose(purpose).isEmpty()) {
                counter++;
                continue;
            }
            handleHousehold(household);
        }
    }

    protected void handleHousehold(MitoHousehold household) {
        distributeHouseholdTrips(household);
        counter++;
    }

    protected abstract void distributeHouseholdTrips(MitoHousehold household);

    protected void selectDestination(MitoTrip trip) {
        if (probabilities.isEmpty()) {
            logger.warn("Could not find destination for trip " + trip);
            TripDistribution.FAILED_TRIPS_COUNTER.incrementAndGet();
            return;
        }
        Integer destination = MitoUtil.select(probabilities, random);
        trip.setTripDestination(dataSet.getZones().get(destination));
        TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
    }
}
