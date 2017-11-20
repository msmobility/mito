package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunctionExecutor;
import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGenerator {

    private static final Logger logger = Logger.getLogger(RawTripGenerator.class);

    static final AtomicInteger counterDroppedTripsAtBorder = new AtomicInteger();
    static final AtomicInteger currentTripId = new AtomicInteger();

    private final DataSet dataSet;

    private final EnumSet<Purpose> PURPOSES = EnumSet.of(HBW, HBE, HBS, HBO, NHBW, NHBO);

    public RawTripGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () {
        generateByPurposeMultiThreaded();
        logTripGeneration();
    }

    private void generateByPurposeMultiThreaded() {
        ConcurrentFunctionExecutor executor = new ConcurrentFunctionExecutor();
        for(Purpose purpose: PURPOSES) {
            executor.addFunction(new TripsByPurposeGenerator(dataSet, purpose));
        }
        executor.execute();
    }

    private void logTripGeneration() {
        long rawTrips = dataSet.getTrips().size() + counterDroppedTripsAtBorder.get();
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (counterDroppedTripsAtBorder.get() > 0) {
            logger.info(MitoUtil.customFormat("  " + "###,###", counterDroppedTripsAtBorder.get()) + " trips were dropped at boundary of study area.");
        }
    }
}
