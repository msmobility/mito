package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.sawdust.calculator.Function1;
import com.pb.sawdust.util.array.ArrayUtil;
import com.pb.sawdust.util.concurrent.ForkJoinPoolFactory;
import com.pb.sawdust.util.concurrent.IteratorAction;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGenerator {

    private static Logger logger = Logger.getLogger(RawTripGenerator.class);

    static AtomicInteger counterDroppedTripsAtBorder = new AtomicInteger();
    static AtomicInteger currentTripId = new AtomicInteger();

    private final DataSet dataSet;

    public RawTripGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () {

        // Multi-threading code
        Function1<String,Void> tripGenByPurposeMethod = new ByPurposeGenerator(dataSet);
        // Generate trips for each purpose
        Iterator<String> tripPurposeIterator = ArrayUtil.getIterator(dataSet.getPurposes());
        IteratorAction<String> itTask = new IteratorAction<>(tripPurposeIterator, tripGenByPurposeMethod);
        ForkJoinPool pool = ForkJoinPoolFactory.getForkJoinPool();
        pool.execute(itTask);
        itTask.waitForCompletion();

        logTripGeneration();
    }

    private void logTripGeneration() {
        int rawTrips = dataSet.getTrips().size() + counterDroppedTripsAtBorder.get();
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (counterDroppedTripsAtBorder.get() > 0)
            logger.info(MitoUtil.customFormat("  " + "###,###", counterDroppedTripsAtBorder.get()) + " trips were dropped at boundary of study area.");
    }



}
