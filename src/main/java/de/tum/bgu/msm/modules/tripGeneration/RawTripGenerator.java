package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * Created by Nico on 20.07.2017.
 */
public class RawTripGenerator {

    private static final Logger logger = Logger.getLogger(RawTripGenerator.class);

    final static AtomicInteger DROPPED_TRIPS_AT_BORDER_COUNTER = new AtomicInteger();
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();

    private final DataSet dataSet;
    private TripsByPurposeGeneratorFactory tripsByPurposeGeneratorFactory;
    private final List<Purpose> purposes;

    //private final EnumSet<Purpose> PURPOSES = EnumSet.of(HBW, HBE, HBS, HBO, NHBW, NHBO);

    public RawTripGenerator(DataSet dataSet, TripsByPurposeGeneratorFactory tripsByPurposeGeneratorFactory, List<Purpose> purposes) {
        this.dataSet = dataSet;
        this.tripsByPurposeGeneratorFactory = tripsByPurposeGeneratorFactory;
        this.purposes = purposes;
    }

    public void run (double scaleFactorForGeneration) {
        generateByPurposeMultiThreaded(scaleFactorForGeneration);
        logTripGeneration();
    }

    private void generateByPurposeMultiThreaded(double scaleFactorForGeneration) {
        final ConcurrentExecutor<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> executor =
                ConcurrentExecutor.fixedPoolService(purposes.size());
        List<Callable<Tuple<Purpose, Map<MitoHousehold,List<MitoTrip>>>>> tasks = new ArrayList<>();
        for(Purpose purpose: purposes) {
            tasks.add(tripsByPurposeGeneratorFactory.createTripGeneratorForThisPurpose(dataSet, purpose, scaleFactorForGeneration));
        }
        final List<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> results = executor.submitTasksAndWaitForCompletion(tasks);
        for(Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> result: results) {
            final Purpose purpose = result.getFirst();
            final int sum = result.getSecond().values().stream().flatMapToInt(e -> IntStream.of(e.size())).sum();
            logger.info("Created " + sum + " trips for " + purpose);
            final Map<MitoHousehold, List<MitoTrip>> tripsByHouseholds = result.getSecond();
            for(Map.Entry<MitoHousehold, List<MitoTrip>> tripsByHousehold: tripsByHouseholds.entrySet()) {
                List<MitoTrip> tripsInThisHousehold = tripsByHousehold.getValue();
                tripsByHousehold.getKey().setTripsByPurpose(tripsInThisHousehold, purpose);
                dataSet.addTrips(tripsInThisHousehold);
                for (MitoTrip mitoTrip : tripsInThisHousehold) {
                    MitoPerson person = mitoTrip.getPerson();
                    person.addTrip(mitoTrip);
                }
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
