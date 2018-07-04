package de.tum.bgu.msm.modules.trafficAssignment;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.router.DijkstraTree;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.utils.leastcostpathtree.LeastCostPathTree;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CarSkimUpdater {
    private final static Logger LOGGER = Logger.getLogger(CarSkimUpdater.class);
    private Network network;
    private final Map<Integer, List<Node>> nodesByZone = new ConcurrentHashMap<>();
    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 8 * 3600;
    private final DoubleMatrix2D carTravelTimeMatrix;
    private final DoubleMatrix2D carDistanceMatrix;
    private TravelDisutility travelDisutility;
    private TravelTime travelTime;
    //private Map<Integer, SimpleFeature> zoneFeatureMap;
    private DataSet dataSet;
    private final Vehicle VEHICLE = VehicleUtils.getFactory().createVehicle(Id.create("theVehicle", Vehicle.class), VehicleUtils.getDefaultVehicleType());
    private final Person PERSON = PopulationUtils.getFactory().createPerson(Id.create("thePerson", Person.class));


    public CarSkimUpdater(TravelTime travelTime, TravelDisutility travelDisutility,
                          //Map<Integer, SimpleFeature> zoneFeatureMap,
                          Network network, DataSet dataSet) {

        this.network = network;
        //this.zoneFeatureMap = zoneFeatureMap;
        this.travelTime = travelTime;
        this.travelDisutility = travelDisutility;
        //creates a matrix of (n+1 zones) rows and columns
        int maxZone = dataSet.getZones().keySet().stream().max(Integer::compareTo).get() + 1;
        this.carTravelTimeMatrix = new DenseDoubleMatrix2D(maxZone, maxZone);
        this.carDistanceMatrix = new DenseDoubleMatrix2D(maxZone, maxZone);
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
                Coord originCoord = mitoZone.getRandomCoord();
                Node originNode = NetworkUtils.getNearestLink(network, originCoord).getToNode();
                nodesByZone.get(mitoZone.getId()).add(originNode);
            }
        });
        LOGGER.info("Assigned nodes to " + nodesByZone.keySet().size() + " zones");

        Map<Id<Link>, Double> linkPeakHourTimes = new ConcurrentHashMap<>();
        Map<Id<Link>, Double> linkOffPeakHourTimes = new ConcurrentHashMap<>();
        for (Link link : network.getLinks().values()){
            linkPeakHourTimes.put(link.getId(), travelTime.getLinkTravelTime(link, DEFAULT_PEAK_H_S, PERSON, VEHICLE));
            linkOffPeakHourTimes.put(link.getId(), link.getLength() / link.getFreespeed());
        }
        LOGGER.info("Assigned travel times");

        DijkstraTree dijkstraTree = new DijkstraTree(network, new MyTravelDisutility(linkPeakHourTimes, linkOffPeakHourTimes),
                new MyTravelTime(linkPeakHourTimes));

        AtomicInteger count = new AtomicInteger(0);
//        for (int zoneId : zoneFeatureMap.keySet()) {
        for (int origin : nodesByZone.keySet()) {
            //nodesByZone.keySet().parallelStream().forEach(destination -> {
            Node originNode = nodesByZone.get(origin).get(0);
            //for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several nodes in a given origin zone
            dijkstraTree.calcLeastCostPathTree(originNode, DEFAULT_PEAK_H_S);
                //for(int destination : nodesByZone.keySet()) {
                    nodesByZone.keySet().stream().filter(x -> x > origin).parallel().forEach(destination -> {
                        //if (origin < destination) {
                            //for (Node destinationNode : nodesByZone.get(destination)) {// several nodes in a given destination zone
                            Node destinationNode = nodesByZone.get(destination).get(0);
                            LeastCostPathCalculator.Path path = dijkstraTree.getLeastCostPath(destinationNode);
                            double time = 0;
                            double distance = 0;
                            for (Link link : path.links) {
                                distance += link.getLength();
                                //time += travelTime.getLinkTravelTime(link, DEFAULT_PEAK_H_S, PERSON, VEHICLE);
                                time += linkPeakHourTimes.get(link.getId());
                            }

                            //}
                            //}
                            carTravelTimeMatrix.setQuick(origin, destination, time/60);
                            carTravelTimeMatrix.setQuick(destination, origin, time/60);
                            carDistanceMatrix.setQuick(origin, destination, distance/1000);
                            carDistanceMatrix.setQuick(destination, origin, distance/1000);
                        //}
                        //}
                    });
            //});
            if (LongMath.isPowerOfTwo(count.incrementAndGet())) {
                LOGGER.info("Completed " + count + " zones");
            }
        }
        LOGGER.info("Completed car matrix update");
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
                minDistValues[k] = maximumMinutes / 60 * 50 * 1000; //maximum distance results from maximum time at 50 km/h
            }
            //find the  n closest neighbors - the lower travel time values in the matrix column
            for (int j = 1; j < carTravelTimeMatrix.rows(); j++) {
                int minimumPosition = 0;
                while (minimumPosition < numberOfNeighbours) {
                    if (minTimeValues[minimumPosition] > carTravelTimeMatrix.getQuick(i, j) && carTravelTimeMatrix.getQuick(i, j) != 0) {
                        for (int k = numberOfNeighbours - 1; k > minimumPosition; k--) {
                            minTimeValues[k] = minTimeValues[k - 1];
                            minDistValues[k] = minDistValues[k - 1];

                        }
                        minTimeValues[minimumPosition] = carTravelTimeMatrix.getQuick(i, j);
                        minDistValues[minimumPosition] = carDistanceMatrix.getQuick(i, j);
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
                if (carTravelTimeMatrix.getQuick(i, j) == 0) {
                    carTravelTimeMatrix.setQuick(i, j, globalMinTime);
                    carDistanceMatrix.setQuick(i, j, globalMinDist);
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

    class MyTravelTime implements TravelTime{

        Map<Id<Link>, Double > peakHourTimes;

        public MyTravelTime(Map<Id<Link>, Double> peakHourTimes) {
            this.peakHourTimes = peakHourTimes;
        }

        @Override
        public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
            return peakHourTimes.get(link.getId());
        }
    }

    class MyTravelDisutility implements TravelDisutility {

        Map<Id<Link>, Double > peakHourTimes;
        Map<Id<Link>, Double > offPeakHourTimes;

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
