package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.sawdust.calculator.Function1;
import com.pb.sawdust.util.array.ArrayUtil;
import com.pb.sawdust.util.concurrent.ForkJoinPoolFactory;
import com.pb.sawdust.util.concurrent.IteratorAction;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Purpose;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGenerator {

    private static final Logger logger = Logger.getLogger(RawTripGenerator.class);

    static final AtomicInteger counterDroppedTripsAtBorder = new AtomicInteger();
    static final AtomicInteger currentTripId = new AtomicInteger();

    private final DataSet dataSet;

    public RawTripGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () {
        generateByPurposeMultiThreaded();
        logTripGeneration();
    }

    private void generateByPurposeMultiThreaded() {

        Function1<Purpose,Void> tripGenByPurposeMethod = purpose -> {
            TripsByPurposeGenerator byPurposeGenerator = new TripsByPurposeGenerator(dataSet, purpose);
            List<MitoTrip> trips = byPurposeGenerator.generateTrips();
            for (MitoTrip trip: trips) {
                dataSet.getTrips().put(trip.getTripId(), trip);
                dataSet.getHouseholds().get(trip.getHouseholdId()).addTrip(trip);
            }
            return null;
        };

        Iterator<Purpose> tripPurposeIterator = ArrayUtil.getIterator(Purpose.values());
        IteratorAction<Purpose> itTask = new IteratorAction<>(tripPurposeIterator, tripGenByPurposeMethod);
        ForkJoinPool pool = ForkJoinPoolFactory.getForkJoinPool();
        pool.execute(itTask);
        itTask.waitForCompletion();
    }

    private void logTripGeneration() {
        int rawTrips = dataSet.getTrips().size() + counterDroppedTripsAtBorder.get();
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (counterDroppedTripsAtBorder.get() > 0) {
            logger.info(MitoUtil.customFormat("  " + "###,###", counterDroppedTripsAtBorder.get()) + " trips were dropped at boundary of study area.");
        }
    }
}
