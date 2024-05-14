package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


public final class TripDistributionAggregate extends Module {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);
    public final static AtomicInteger completelyRandomNhbTrips = new AtomicInteger(0);

    //todo turn to static to be mantained for both mandatory and discretionary - we expect to remove ttb from the trip distribution
    private static Map<Integer, IndexedDoubleMatrix2D> utilityMatrices = new LinkedHashMap<>();

    private static Map<Integer, IndexedDoubleMatrix2D> tripMatrices = new LinkedHashMap<>();

    private final static Logger logger = Logger.getLogger(TripDistributionAggregate.class);

    private final Map<Purpose, Double> travelDistanceCalibrationParameters;
    private final Map<Purpose, Double> impedanceCalibrationParameters;
    private final boolean useBudgetsInDestinationChoice;

    private final Purpose purpose;

    private MitoAggregatePersona persona;

    private final DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory;

    public TripDistributionAggregate(DataSet dataSet, List<Purpose> purposes, Map<Purpose, Double> travelDistanceCalibrationParameters,
                                     Map<Purpose, Double> impedanceCalibrationParameters, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory,
                                     MitoAggregatePersona persona) {
        super(dataSet, purposes);
        this.travelDistanceCalibrationParameters = travelDistanceCalibrationParameters;
        this.impedanceCalibrationParameters = impedanceCalibrationParameters;
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
        this.purpose = purposes.get(0);
        this.persona = persona;
    }

    public TripDistributionAggregate(DataSet dataSet, List<Purpose> purposes, boolean useBudgetsInDestinationChoice, DestinationUtilityCalculatorFactoryAggregate destinationUtilityCalculatorFactory,
                                     MitoAggregatePersona persona) {
        super(dataSet, purposes);
        this.useBudgetsInDestinationChoice = useBudgetsInDestinationChoice;
        this.destinationUtilityCalculatorFactory = destinationUtilityCalculatorFactory;
        travelDistanceCalibrationParameters = new HashMap<>();
        impedanceCalibrationParameters = new HashMap<>();
        for (Purpose purpose : purposes){
            travelDistanceCalibrationParameters.put(purpose, 1.0);
            impedanceCalibrationParameters.put(purpose, 1.0);
        }
        purpose = purposes.get(0);
        this.persona = persona;
    }

    @Override
    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();
        updateTripMatrices(utilityMatrices);
        printMatrices();
        //logger.info("Distributing trips for households...");
        //distributeTrips();
    }

    private void printMatrices() {

        Path filePersona = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona + "destinationChoice_"+ purposes.get(0) +"_results.csv");
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);

        Path filePersona1 = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona + "destinationChoice_"+ purposes.get(0) +"_trips.csv");
        PrintWriter pwtrips = MitoUtil.openFileForSequentialWriting(filePersona1.toAbsolutePath().toString(), false);

        IndexedDoubleMatrix2D destinationChoice = dataSet.getAggregateTripMatrix().get(Mode.taxi);

        for (MitoZone origin : dataSet.getZones().values()){
            pwh.print(origin.getId());
            pwh.print(",");
            pwtrips.print(origin.getId());
            pwtrips.print(",");
        }
        pwh.println();
        pwtrips.println();
        for (MitoZone origin : dataSet.getZones().values()){
            for(MitoZone destination : dataSet.getZones().values()) {
                pwh.print(destinationChoice.getIndexed(origin.getId(), destination.getId()));
                pwh.print(",");
                pwtrips.print( utilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId()));
                pwtrips.print(",");
            }
            pwh.println();
            pwtrips.println();
        }
        pwh.close();
        pwtrips.close();
    }

    private void buildMatrices() {
        //IndexedDoubleMatrix2D
        List<Callable<Tuple<Integer,IndexedDoubleMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (MitoZone zone : dataSet.getZones().values()) {
            utilityCalcTasks.add(new DestinationUtilityByPurposeGeneratorAggregate(zone, purpose, dataSet,
                    destinationUtilityCalculatorFactory,
                    travelDistanceCalibrationParameters.get(purpose),
                    impedanceCalibrationParameters.get(purpose)));

        }
        ConcurrentExecutor<Tuple<Integer, IndexedDoubleMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(dataSet.getZones().size());
        List<Tuple<Integer,IndexedDoubleMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Tuple<Integer, IndexedDoubleMatrix2D> result: results) {
            utilityMatrices.put(result.getFirst(), result.getSecond());
        }
        logger.info("Destination choice. Finished purpose " + purpose);

    }

    private void updateTripMatrices(Map<Integer, IndexedDoubleMatrix2D> utilityMatrices) {
        final IndexedDoubleMatrix2D utilityMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());

/*        for (MitoZone origin : dataSet.getZones().values()){
            IndexedDoubleMatrix1D tripGeneration = dataSet.getAggregateTripMatrix().get(Mode.taxi).viewRow(origin.getId());
            for(MitoZone destination : dataSet.getZones().values()) {
                utilityMatrix.setIndexed(origin.getId(), destination.getId(),
                        tripGeneration.getIndexed(destination.getId()) * utilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId()));
            }
        }*/

  /*      int nthreads = ConcurrencyUtils.getNumberOfThreads();

        if (nthreads > 1 && dataSet.getZones().size() * dataSet.getZones().size() >= ConcurrencyUtils.getThreadsBeginN_2D()) {
            nthreads = Math.min(nthreads, dataSet.getZones().size());
            int r;
            int c;
            if (nthreads > 1 && dataSet.getZones().size() * dataSet.getZones().size() >= ConcurrencyUtils.getThreadsBeginN_2D()) {
                nthreads = Math.min(nthreads, dataSet.getZones().size());
                Future<?>[] futures = new Future[nthreads];
                r = dataSet.getZones().size() / nthreads;

                for(c = 0; c < nthreads; ++c) {
                    final int firstRow = c * r;
                    final int lastRow = c == nthreads - 1 ? dataSet.getZones().size() : firstRow + r;
                    futures[c] = ConcurrencyUtils.submit(new Callable() {
                        public Object call()  {
                            for(int r = firstRow; r < lastRow; ++r) {
                                IndexedDoubleMatrix1D tripGeneration = utilityMatrices.get(r).viewRow(r);
                                for(int c = 0; c < utilityMatrix.columns(); ++c) {
                                    double value = tripGeneration.getIndexed(r) * utilityMatrices.get(r).getIndexed(r, c);
                                    utilityMatrix.setIndexed(r, c, value);
                                }
                            }

                            return null;
                        }
                    });
                    *//*futures[c] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {

                            for(int r = firstRow; r < lastRow; ++r) {
                                IndexedDoubleMatrix1D tripGeneration = utilityMatrices.get(r).viewRow(1);
                                for(int c = 0; c < utilityMatrix.columns(); ++c) {
                                    double value = tripGeneration.getIndexed(c) * utilityMatrices.get(r).getIndexed(r, c);
                                    utilityMatrix.setIndexed(r, c, value);
                                }
                            }

                        }
                    });*//*
                }
                ConcurrencyUtils.waitForCompletion(futures);
                //} else {

                    for(r = 0; r < utilityMatrix.rows(); ++r) {
                        IndexedDoubleMatrix1D tripGeneration = dataSet.getAggregateTripMatrix().get(Mode.taxi).viewRow(r);
                        for(c = 0; c < utilityMatrix.columns(); ++c) {
                            double value = tripGeneration.getIndexed(c) * utilityMatrices.get(r).getIndexed(r, c);
                            utilityMatrix.setIndexed(r, c, value);
                        }
                    }
                //}*/

            for (MitoZone origin : dataSet.getZones().values()){
                IndexedDoubleMatrix1D tripGeneration = dataSet.getAggregateTripMatrix().get(Mode.taxi).viewRow(origin.getId());
                double originalTrips = tripGeneration.getIndexed(origin.getId());
                for(MitoZone destination : dataSet.getZones().values()) {
                    utilityMatrix.setIndexed(origin.getId(), destination.getId(),
                             originalTrips * utilityMatrices.get(origin.getId()).getIndexed(origin.getId(), destination.getId()));
                }
            }
        dataSet.getAggregateTripMatrix().put(Mode.taxi, utilityMatrix);
        }

    }
