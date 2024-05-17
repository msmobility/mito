package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
        summarizeResults();
    }

    private void summarizeResults() {
        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "_TripGen_"+ purposes.get(0) +"_results.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);

        IndexedDoubleMatrix2D destinationChoice = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
        for (MitoZone origin : dataSet.getZones().values()){
            pwh.print(origin.getId());
            pwh.print(",");
        }
        pwh.println();

        for (MitoZone origin : dataSet.getZonesByAreaType().values()){
            for(MitoZone destination : dataSet.getZones().values()) {
                pwh.print(destinationChoice.getIndexed(origin.getId(), destination.getId()));
                pwh.print(",");
            }
            pwh.println();
        }
        pwh.close();
    }

    private void generateByPurposeMultiThreaded(double scaleFactorForGeneration) {
        for (AreaTypes.SGType area : AreaTypes.SGType.values()) {
            try {
                tripsByPurposeGeneratorFactory.createTripGeneratorForThisPurpose(dataSet, purposes.get(0), scaleFactorForGeneration, persona, area).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            IndexedDoubleMatrix2D matrix = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
            double tripsbyarea = 0.;
            for (MitoZone origin : dataSet.getZones().values()){
                if (origin.getAreaTypeSG().equals(area)) {
                    for (MitoZone destination : dataSet.getZones().values()) {
                        double tripsZone = matrix.getIndexed(origin.getId(), destination.getId());
                        tripsbyarea = tripsbyarea + tripsZone;
                    }
                }
            }
            dataSet.getTotalTripsGenByPurpose().get(persona).get(purposes.get(0)).put(area, tripsbyarea);
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
