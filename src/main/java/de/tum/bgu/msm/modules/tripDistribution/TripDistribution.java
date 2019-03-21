package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.AirportDistribution;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.HbeHbwDistribution;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.HbsHboDistribution;
import de.tum.bgu.msm.modules.tripDistribution.destinationChooser.NhbwNhboDistribution;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public final class TripDistribution extends Module {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);
    public final static AtomicInteger completelyRandomNhbTrips = new AtomicInteger(0);

    private final EnumMap<Purpose, IndexedDoubleMatrix2D> utilityMatrices = new EnumMap<>(Purpose.class);

    private final static Logger logger = Logger.getLogger(TripDistribution.class);

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
        List<Callable<Pair<Purpose,IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (Purpose purpose : Purpose.values()) {
            utilityCalcTasks.add(new DestinationUtilityByPurposeGenerator(purpose, dataSet));
        }
        ConcurrentExecutor<Pair<Purpose, IndexedDoubleMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Pair<Purpose,IndexedDoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Pair<Purpose, IndexedDoubleMatrix2D> result: results) {
            utilityMatrices.put(result.getKey(), result.getValue());
        }
    }

    private void distributeTrips() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        homeBasedTasks.add(HbsHboDistribution.hbs(utilityMatrices.get(HBS), dataSet));
        homeBasedTasks.add(HbsHboDistribution.hbo(utilityMatrices.get(HBO), dataSet));
        homeBasedTasks.add(HbeHbwDistribution.hbw(utilityMatrices.get(HBW), dataSet));
        homeBasedTasks.add(HbeHbwDistribution.hbe(utilityMatrices.get(HBE), dataSet));
        executor.submitTasksAndWaitForCompletion(homeBasedTasks);


        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();
        nonHomeBasedTasks.add(NhbwNhboDistribution.nhbw(utilityMatrices, dataSet));
        nonHomeBasedTasks.add(NhbwNhboDistribution.nhbo(utilityMatrices, dataSet));
        nonHomeBasedTasks.add(AirportDistribution.airportDistribution(dataSet));
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);

        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
        if(randomOccupationDestinationTrips.get() > 0) {
            logger.info("There have been " + randomOccupationDestinationTrips.get() +
                    " HBW or HBE trips not done by a worker or student or missing occupation zone. " +
                    "Picked a destination by random utility instead.");
        }
        if(completelyRandomNhbTrips.get() > 0) {
            logger.info("There have been " + completelyRandomNhbTrips + " NHBO or NHBW trips" +
                    "by persons who don't have a matching home based trip. Assumed a destination for a suitable home based"
                    + " trip as either origin or destination for the non-home-based trip.");
        }
    }
}
