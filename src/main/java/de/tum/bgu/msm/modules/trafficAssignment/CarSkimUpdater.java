package de.tum.bgu.msm.modules.trafficAssignment;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.router.DijkstraTree;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CarSkimUpdater {
    private final static Logger LOGGER = Logger.getLogger(CarSkimUpdater.class);
    private Network network;
    private final Map<Integer, List<Node>> nodesByZone = new ConcurrentHashMap<>();
    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 8 * 3600;
    private final IndexedDoubleMatrix2D carTravelTimeMatrix;
    private final IndexedDoubleMatrix2D carDistanceMatrix;
    private TravelDisutility travelDisutility;
    private Controler controler;
    private TravelTime travelTime;
    //private Map<Integer, SimpleFeature> zoneFeatureMap;
    private DataSet dataSet;
    private final Vehicle VEHICLE = VehicleUtils.getFactory().createVehicle(Id.create("theVehicle", Vehicle.class), VehicleUtils.getDefaultVehicleType());
    private final Person PERSON = PopulationUtils.getFactory().createPerson(Id.create("thePerson", Person.class));


    public CarSkimUpdater(Controler controler,
                          //Map<Integer, SimpleFeature> zoneFeatureMap,
                          Network network, DataSet dataSet) {

        this.network = network;
        //this.zoneFeatureMap = zoneFeatureMap;
        this.controler = controler;
        this.travelTime = controler.getLinkTravelTimes();
        this.travelDisutility = controler.getTravelDisutilityFactory().createTravelDisutility(travelTime);
        //creates a matrix of (n+1 zones) rows and columns
        this.carTravelTimeMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.carDistanceMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.dataSet = dataSet;
    }

    public void run() {
        calculateMatrixFromMatsim();
        assignIntrazonals(5, 10, 0.33f);
        updateMatrices();
    }

    private void calculateMatrixFromMatsim() {
        nodesByZone.clear();

        dataSet.getZones().values().stream().parallel().forEach(mitoZone -> {
            nodesByZone.put(mitoZone.getId(), new LinkedList());
            for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several points in a given origin zone
                Coord originCoord = CoordUtils.createCoord(mitoZone.getRandomCoord());
                Node originNode = NetworkUtils.getNearestLink(network, originCoord).getToNode();
                nodesByZone.get(mitoZone.getId()).add(originNode);
            }
        });
        LOGGER.info("Assigned nodes to " + nodesByZone.keySet().size() + " zones");

        Map<Id<Link>, Double> linkPeakHourTimes = new ConcurrentHashMap<>();
        Map<Id<Link>, Double> linkOffPeakHourTimes = new ConcurrentHashMap<>();
        for (Link link : network.getLinks().values()) {
            linkPeakHourTimes.put(link.getId(), travelTime.getLinkTravelTime(link, DEFAULT_PEAK_H_S, PERSON, VEHICLE));
            linkOffPeakHourTimes.put(link.getId(), link.getLength() / link.getFreespeed());
        }
        LOGGER.info("Assigned travel times");

//        //Alt1: LeastCostPathTreeExtended @ accessibility
//        LeastCostPathTreeExtended leastCostPathTreeExtended =
//                new LeastCostPathTreeExtended(travelTime, travelDisutility,null);
//
//        long startTime = System.currentTimeMillis();
//        AtomicInteger count = new AtomicInteger(0);
//
//        // for (int zoneId : zoneFeatureMap.keySet()) {
//        for (int origin : nodesByZone.keySet()) {
//            Node originNode = nodesByZone.get(origin).get(0);
//            leastCostPathTreeExtended.calculateExtended(network, originNode, DEFAULT_PEAK_H_S);
//            nodesByZone.keySet().stream().filter(x -> x > origin).parallel().forEach(destination -> {
//                Node destinationNode = nodesByZone.get(destination).get(0);
//                double time = leastCostPathTreeExtended.getTree().get(destinationNode.getId()).getTime();
//                double distance = leastCostPathTreeExtended.getTreeExtended().get(destinationNode.getId()).getDistance();
//                carTravelTimeMatrix.setQuick(origin, destination, (time - DEFAULT_PEAK_H_S) / 60);
//                carTravelTimeMatrix.setQuick(destination, origin, (time - DEFAULT_PEAK_H_S) / 60);
//                carDistanceMatrix.setQuick(origin, destination, distance / 1000);
//                carDistanceMatrix.setQuick(destination, origin, distance / 1000);
//            });
//            if (LongMath.isPowerOfTwo(count.incrementAndGet())) {
//                LOGGER.info("Completed " + count + " zones");
//            }
//        }
//        long runtime = (System.currentTimeMillis() - startTime) / 1000;
//        LOGGER.info("Completed car matrix update in " + runtime + " seconds (access. methods)");

        //Alt 2: DijkstraTree @ dvpr
        DijkstraTree dijkstraTree = new DijkstraTree(network, new MyTravelDisutility(linkPeakHourTimes, linkOffPeakHourTimes),
                new MyTravelTime(linkPeakHourTimes));

        long startTime2 = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        for (int origin : nodesByZone.keySet()) {
            Node originNode = nodesByZone.get(origin).get(0);
            dijkstraTree.calcLeastCostPathTree(originNode, DEFAULT_PEAK_H_S);
            nodesByZone.keySet().stream().filter(x -> x > origin).parallel().forEach(destination -> {
                Node destinationNode = nodesByZone.get(destination).get(0);
                LeastCostPathCalculator.Path path = dijkstraTree.getLeastCostPath(destinationNode);
                double time = 0;
                double distance = 0;
                for (Link link : path.links) {
                    distance += link.getLength();
                    time += linkPeakHourTimes.get(link.getId());
                }
                carTravelTimeMatrix.setIndexed(origin, destination, time / 60);
                carTravelTimeMatrix.setIndexed(destination, origin, time / 60);
                carDistanceMatrix.setIndexed(origin, destination, distance / 1000);
                carDistanceMatrix.setIndexed(destination, origin, distance / 1000);
            });
            if (LongMath.isPowerOfTwo(count.incrementAndGet())) {
                LOGGER.info("Completed " + count + " zones");
            }
        }
        long runtime2 = (System.currentTimeMillis() - startTime2) / 1000;
        LOGGER.info("Completed car matrix update in " + runtime2 + " seconds(dvrp methods)");
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
            double[] minTimeValues = new double[numberOfNeighbours];
            double[] minDistValues = new double[numberOfNeighbours];
            for (int k = 0; k < numberOfNeighbours; k++) {
                minTimeValues[k] = maximumMinutes;
                minDistValues[k] = maximumMinutes / 60 * 50; //maximum distance results from maximum time at 50 km/h
            }
            //find the  n closest neighbors - the lower travel time values in the matrix column
            for (int j = 1; j < carTravelTimeMatrix.rows(); j++) {
                int minimumPosition = 0;
                while (minimumPosition < numberOfNeighbours) {
                    if (minTimeValues[minimumPosition] > carTravelTimeMatrix.getIndexed(i, j) && carTravelTimeMatrix.getIndexed(i, j) != 0) {
                        for (int k = numberOfNeighbours - 1; k > minimumPosition; k--) {
                            minTimeValues[k] = minTimeValues[k - 1];
                            minDistValues[k] = minDistValues[k - 1];

                        }
                        minTimeValues[minimumPosition] = carTravelTimeMatrix.getIndexed(i, j);
                        minDistValues[minimumPosition] = carDistanceMatrix.getIndexed(i, j);
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
                if (carTravelTimeMatrix.getIndexed(i, j) == 0) {
                    carTravelTimeMatrix.setIndexed(i, j, globalMinTime);
                    carDistanceMatrix.setIndexed(i, j, globalMinDist);
                    if (i != j) {
                        nonIntrazonalCounter++;
                    }
                }
            }
        }
        LOGGER.info("Calculated intrazonal times and distances using the " + numberOfNeighbours + " nearest neighbours.");
        LOGGER.info("The calculation of intrazonals has also assigned values for cells with travel time equal to 0, that are not intrazonal: (" +
                nonIntrazonalCounter + " cases).");
    }

    class MyTravelTime implements TravelTime {

        Map<Id<Link>, Double> peakHourTimes;

        public MyTravelTime(Map<Id<Link>, Double> peakHourTimes) {
            this.peakHourTimes = peakHourTimes;
        }

        @Override
        public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
            return peakHourTimes.get(link.getId());
        }
    }

    class MyTravelDisutility implements TravelDisutility {

        Map<Id<Link>, Double> peakHourTimes;
        Map<Id<Link>, Double> offPeakHourTimes;

        public MyTravelDisutility(Map<Id<Link>, Double> peakHourTimes, Map<Id<Link>, Double> offPeakHourTimes) {
            this.peakHourTimes = peakHourTimes;
            this.offPeakHourTimes = offPeakHourTimes;
        }

        @Override
        public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
            return peakHourTimes.get(link.getId());
        }

        @Override
        public double getLinkMinimumTravelDisutility(Link link) {
            return offPeakHourTimes.get(link.getId());
        }
    }


}
