package de.tum.bgu.msm.modules.tripDistribution;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.google.common.collect.ImmutableList;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.BasicDestinationChooser;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.MandatoryTripDestinationChooser;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.NonHomeBasedDestinationChooser;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Purpose.*;


public final class TripDistribution extends Module {

    public final static AtomicInteger DISTRIBUTED_TRIPS_COUNTER = new AtomicInteger(0);
    public final static AtomicInteger FAILED_TRIPS_COUNTER = new AtomicInteger(0);

    public final static AtomicInteger RANDOM_OCCUPATION_DESTINATION_TRIPS = new AtomicInteger(0);
    public final static AtomicInteger COMPLETELY_RANDOM_NHB_TRIPS = new AtomicInteger(0);

    private final EnumMap<Purpose, DoubleMatrix2D> utilityMatrices = new EnumMap<>(Purpose.class);

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();

        logger.info("Assigning trips for households...");

        ConcurrentFunctionExecutor homeBasedDistributor = new ConcurrentFunctionExecutor();
        homeBasedDistributor.addFunction(new BasicDestinationChooser(HBS, utilityMatrices, dataSet));
        homeBasedDistributor.addFunction(new BasicDestinationChooser(HBO, utilityMatrices, dataSet));
        homeBasedDistributor.addFunction(new MandatoryTripDestinationChooser(HBW, Occupation.WORKER, utilityMatrices, dataSet));
        homeBasedDistributor.addFunction(new MandatoryTripDestinationChooser(HBE, Occupation.STUDENT, utilityMatrices, dataSet));
        homeBasedDistributor.execute();

        ConcurrentFunctionExecutor nonHomeBasedDistributor = new ConcurrentFunctionExecutor();
        nonHomeBasedDistributor.addFunction(new NonHomeBasedDestinationChooser(NHBW, Collections.singletonList(HBW), Occupation.WORKER, utilityMatrices, dataSet));
        nonHomeBasedDistributor.addFunction(new NonHomeBasedDestinationChooser(NHBO, ImmutableList.of(HBO, HBE, HBS), Occupation.STUDENT, utilityMatrices, dataSet));
        nonHomeBasedDistributor.execute();

        logger.info("Distributed: " + DISTRIBUTED_TRIPS_COUNTER + ", failed: " + FAILED_TRIPS_COUNTER);
        if(RANDOM_OCCUPATION_DESTINATION_TRIPS.get() > 0) {
            logger.info("There have been " + RANDOM_OCCUPATION_DESTINATION_TRIPS.get() + " HBW or HBE trips not done by a worker or student. " +
                  "Picked a destination by random utility instead.");
        }
        if(COMPLETELY_RANDOM_NHB_TRIPS.get() > 0) {
            logger.info("There have been " + COMPLETELY_RANDOM_NHB_TRIPS + " NHBO or NHBW trips" +
                    "by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                    + " trip as either origin or destination for the non-home-based trip.");
        }
    }

    public void buildMatrices() {
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for (Purpose purpose : Purpose.values()) {
            executor.addFunction(new DestinationUtilityByPurposeGenerator(purpose, dataSet, utilityMatrices, dataSet.getPeakHour()));
        }
        executor.execute();
    }
}
