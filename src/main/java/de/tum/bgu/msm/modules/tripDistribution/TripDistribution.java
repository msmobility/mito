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
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Purpose.*;


public final class TripDistribution extends Module {

    public final static AtomicInteger DISTRIBUTED_TRIPS_COUNTER = new AtomicInteger(0);
    public final static AtomicInteger FAILED_TRIPS_COUNTER = new AtomicInteger(0);

    public final static AtomicInteger RANDOM_OCCUPATION_DESTINATION_TRIPS = new AtomicInteger(0);
    public final static AtomicInteger COMPLETELY_RANDOM_NHB_TRIPS = new AtomicInteger(0);

    private final EnumMap<Purpose, DoubleMatrix2D> utilityMatrices = new EnumMap<>(Purpose.class);

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

    private final ConcurrentExecutor executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();

        logger.info("Distributing trips for households...");
        distributeTrips();
    }

    private void buildMatrices() {
        List<Callable<Pair<Purpose,DoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (Purpose purpose : Purpose.values()) {
            utilityCalcTasks.add(new DestinationUtilityByPurposeGenerator(purpose, dataSet));
        }
        List<Pair<Purpose,DoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Pair<Purpose, DoubleMatrix2D> result: results) {
            utilityMatrices.put(result.getKey(), result.getValue());
        }
    }

    private void distributeTrips() {
        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        homeBasedTasks.add(new BasicDestinationChooser(HBS, utilityMatrices, dataSet));
        homeBasedTasks.add(new BasicDestinationChooser(HBO, utilityMatrices, dataSet));
        homeBasedTasks.add(new MandatoryTripDestinationChooser(HBW, Occupation.WORKER, utilityMatrices, dataSet));
        homeBasedTasks.add(new MandatoryTripDestinationChooser(HBE, Occupation.STUDENT, utilityMatrices, dataSet));
        executor.submitTasksAndWaitForCompletion(homeBasedTasks);


        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();
        nonHomeBasedTasks.add(new NonHomeBasedDestinationChooser(NHBW, Collections.singletonList(HBW), Occupation.WORKER, utilityMatrices, dataSet));
        nonHomeBasedTasks.add(new NonHomeBasedDestinationChooser(NHBO, ImmutableList.of(HBO, HBE, HBS), Occupation.STUDENT, utilityMatrices, dataSet));
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);

        logger.info("Distributed: " + DISTRIBUTED_TRIPS_COUNTER + ", failed: " + FAILED_TRIPS_COUNTER);
        if(RANDOM_OCCUPATION_DESTINATION_TRIPS.get() > 0) {
            logger.info("There have been " + RANDOM_OCCUPATION_DESTINATION_TRIPS.get() +
                    " HBW or HBE trips not done by a worker or student or missing occupation zone. " +
                    "Picked a destination by random utility instead.");
        }
        if(COMPLETELY_RANDOM_NHB_TRIPS.get() > 0) {
            logger.info("There have been " + COMPLETELY_RANDOM_NHB_TRIPS + " NHBO or NHBW trips" +
                    "by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                    + " trip as either origin or destination for the non-home-based trip.");
        }
    }
}
