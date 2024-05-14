package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGeneratorAggregate {

    private static final Logger logger = Logger.getLogger(RawTripGeneratorAggregate.class);

    final static AtomicInteger DROPPED_TRIPS_AT_BORDER_COUNTER = new AtomicInteger();
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();

    private final DataSet dataSet;
    private TripsByPurposeGeneratorFactoryAggregate tripsByPurposeGeneratorFactory;
    private final List<Purpose> purposes;

    private final MitoAggregatePersona persona;
    //private final EnumSet<Purpose> PURPOSES = EnumSet.of(HBW, HBE, HBS, HBO, NHBW, NHBO);

    public RawTripGeneratorAggregate(DataSet dataSet, TripsByPurposeGeneratorFactoryAggregate tripsByPurposeGeneratorFactory, List<Purpose> purposes , MitoAggregatePersona persona) {
        this.dataSet = dataSet;
        this.tripsByPurposeGeneratorFactory = tripsByPurposeGeneratorFactory;
        this.purposes = purposes;
        this.persona = persona;
    }

    public void run (double scaleFactorForGeneration, MitoAggregatePersona persona) {
        generateByPurposeMultiThreaded(scaleFactorForGeneration);
        logTripGeneration();
    }

    private void generateByPurposeMultiThreaded(double scaleFactorForGeneration) {
        for (Purpose purpose : purposes) {
            try {
                tripsByPurposeGeneratorFactory.createTripGeneratorForThisPurpose(dataSet, purpose, scaleFactorForGeneration, persona).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void logTripGeneration() {
        long rawTrips = dataSet.getTrips().size() + DROPPED_TRIPS_AT_BORDER_COUNTER.get();
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (DROPPED_TRIPS_AT_BORDER_COUNTER.get() > 0) {
            logger.info(MitoUtil.customFormat("  " + "###,###", DROPPED_TRIPS_AT_BORDER_COUNTER.get()) +
                    " trips were dropped at boundary of study area.");
        }
    }
}
