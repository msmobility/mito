package de.tum.bgu.msm.trafficAssignment;

import com.google.common.collect.Iterables;
import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.*;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarSkimUpdater {

    private final static Logger logger = Logger.getLogger(CarSkimUpdater.class);
    private Network network;
    private final Map<Integer, List<Node>> nodesByZone = new ConcurrentHashMap<>();
    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 8 * 3600;
    private final IndexedDoubleMatrix2D carTravelTimeMatrix;
    private final IndexedDoubleMatrix2D carDistanceMatrix;
    private TravelDisutility travelDisutility;
    private TravelTime travelTime;
    private DataSet dataSet;
    private final String scenarioName;

    public CarSkimUpdater(Controler controler, DataSet dataSet, String scenarioName) {
        this.network = controler.getScenario().getNetwork();
        //this.zoneFeatureMap = zoneFeatureMap;
        this.travelTime = controler.getLinkTravelTimes();
        this.travelDisutility = controler.getTravelDisutilityFactory().createTravelDisutility(travelTime);
        //creates a matrix of (n+1 zones) rows and columns
        this.carTravelTimeMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.carDistanceMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
    }

    public void run() {
        calculateMatrixFromMatsim();
        assignIntrazonals(5, 10, 0.33f);
        updateMatrices();
        printSkim();
    }

    private void printSkim() {
        try {
            String fileName = "./scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/" + Resources.instance.getString(Properties.SKIM_FILE_NAME);
            int dimension = dataSet.getZones().size();
            OmxMatrixWriter.createOmxFile(fileName, dimension);

            SkimTravelTimes tt = (SkimTravelTimes) dataSet.getTravelTimes();
            tt.printOutCarSkim(TransportMode.car, fileName, "timeByTime");

            MatrixTravelDistances td = (MatrixTravelDistances) dataSet.getTravelDistancesAuto();
            td.printOutDistanceSkim(fileName, "distanceByTime");

        } catch (ClassCastException e) {
            logger.info("Currently it is not possible to print out a matrix from an object which is not SkimTravelTime");
        }
    }

    private void calculateMatrixFromMatsim() {
        nodesByZone.clear();

        dataSet.getZones().values().stream().parallel().forEach(mitoZone -> {
            nodesByZone.put(mitoZone.getId(), new LinkedList<>());
            for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several points in a given origin zone
                Coord originCoord = CoordUtils.createCoord(mitoZone.getRandomCoord(MitoUtil.getRandomObject()));
                Node originNode = NetworkUtils.getNearestLink(network, originCoord).getToNode();
                nodesByZone.get(mitoZone.getId()).add(originNode);
            }
        });
        logger.info("Assigned nodes to " + nodesByZone.keySet().size() + " zones");


        long startTime2 = System.currentTimeMillis();

        final int partitionSize = (int) ((double) dataSet.getZones().size() / Runtime.getRuntime().availableProcessors()) + 1;
        logger.info("Intended size of all of partititons = " + partitionSize);
        Iterable<List<MitoZone>> partitions = Iterables.partition(dataSet.getZones().values(), partitionSize);
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        for (final List<MitoZone> partition : partitions) {
            logger.info("Size of partititon = " + partition.size());

            executor.addTaskToQueue(() -> {
                try {
                    MultiNodePathCalculator calculator
                            = (MultiNodePathCalculator) new FastMultiNodeDijkstraFactory(true).createPathCalculator(network, travelDisutility, travelTime);

                    Set<InitialNode> toNodes = new HashSet<>();
                    for (MitoZone zone : dataSet.getZones().values()) {
                        // Several points in a given origin zone
                        for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) {
                            Node originNode = nodesByZone.get(zone.getId()).get(0);
                            toNodes.add(new InitialNode(originNode, 0., 0.));
                        }
                    }

                    ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes);

                    for (MitoZone origin : partition) {
                        Node node = nodesByZone.get(origin.getId()).get(0);
                        calculator.calcLeastCostPath(node, aggregatedToNodes, DEFAULT_PEAK_H_S, null, null);
                        for (MitoZone destination : dataSet.getZones().values()) {
                            LeastCostPathCalculator.Path path = calculator.constructPath(node, nodesByZone.get(destination.getId()).get(0), DEFAULT_PEAK_H_S);

                            //convert to minutes
                            double travelTime = path.travelTime / 60.;
                            double distance = 0.;
                            for (Link link : path.links) {
                                distance += link.getLength();
                            }
                            carTravelTimeMatrix.setIndexed(origin.getId(), destination.getId(), travelTime);
                            carDistanceMatrix.setIndexed(origin.getId(), destination.getId(), distance / 1000.);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                logger.warn("Finished thread.");
                return null;
            });
        }
        executor.execute();

        long runtime2 = (System.currentTimeMillis() - startTime2) / 1000;
        logger.info("Completed car matrix update in " + runtime2 + " seconds(dvrp methods)");
    }


    private void updateMatrices() {
        SkimTravelTimes skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.updateSkimMatrix(carTravelTimeMatrix, TransportMode.car);
        MatrixTravelDistances autoTravelDistances = new MatrixTravelDistances(carDistanceMatrix);
        dataSet.setTravelDistancesAuto(autoTravelDistances);
    }

    private void assignIntrazonals(int numberOfNeighbours, float maximumMinutes, float proportionOfTime) {
        int nonIntrazonalCounter = 0;
        for (int i = 1; i < carTravelTimeMatrix.columns(); i++) {
            int i_id = carTravelTimeMatrix.getIdForInternalColumnIndex(i);
            double[] minTimeValues = new double[numberOfNeighbours];
            double[] minDistValues = new double[numberOfNeighbours];
            for (int k = 0; k < numberOfNeighbours; k++) {
                minTimeValues[k] = maximumMinutes;
                minDistValues[k] = maximumMinutes / 60 * 50; //maximum distance results from maximum time at 50 km/h
            }
            //find the  n closest neighbors - the lower travel time values in the matrix column
            for (int j = 1; j < carTravelTimeMatrix.rows(); j++) {
                int j_id = carTravelTimeMatrix.getIdForInternalRowIndex(j);
                int minimumPosition = 0;
                while (minimumPosition < numberOfNeighbours) {
                    if (minTimeValues[minimumPosition] > carTravelTimeMatrix.getIndexed(i_id, j_id) && carTravelTimeMatrix.getIndexed(i_id, j_id) != 0) {
                        for (int k = numberOfNeighbours - 1; k > minimumPosition; k--) {
                            minTimeValues[k] = minTimeValues[k - 1];
                            minDistValues[k] = minDistValues[k - 1];

                        }
                        minTimeValues[minimumPosition] = carTravelTimeMatrix.getIndexed(i_id, j_id);
                        minDistValues[minimumPosition] = carDistanceMatrix.getIndexed(i_id, j_id);

                        break;
                    }
                    minimumPosition++;
                }
            }
            double globalMinTime = 0;
            double globalMinDist = 0;
            for (int k = 0; k < numberOfNeighbours; k++) {
                globalMinTime += minTimeValues[k];
                globalMinDist += minDistValues[k];
            }
            globalMinTime = globalMinTime / numberOfNeighbours * proportionOfTime;
            globalMinDist = globalMinDist / numberOfNeighbours * proportionOfTime;

            //fill with the calculated value the cells with zero
            for (int j = 1; j < carTravelTimeMatrix.rows(); j++) {
                int j_id = carTravelTimeMatrix.getIdForInternalColumnIndex(j);
                if (carTravelTimeMatrix.getIndexed(i_id, j_id) == 0) {
                    carTravelTimeMatrix.setIndexed(i_id, j_id, globalMinTime);
                    carDistanceMatrix.setIndexed(i, j, globalMinDist);
                    if (i != j) {
                        nonIntrazonalCounter++;
                    }
                }
            }
        }
        logger.info("Calculated intrazonal times and distances using the " + numberOfNeighbours + " nearest neighbours.");
        logger.info("The calculation of intrazonals has also assigned values for cells with travel time equal to 0, that are not intrazonal: (" +
                nonIntrazonalCounter + " cases).");
    }
}
