package de.tum.bgu.msm.modules.trafficAssignment;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.utils.leastcostpathtree.LeastCostPathTree;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CarSkimUpdater {
    private final static Logger LOGGER = Logger.getLogger(CarSkimUpdater.class);
    private Network network;
    private final Map<Integer, List<Node>> nodesByZone = new ConcurrentHashMap<>();
    Map<Integer, ArrayList<Map<Id<Node>, LeastCostPathTree.NodeData>>> treesByZone = new ConcurrentHashMap<>();

    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 28800;
    private DoubleMatrix2D carTravelTimeMatrix;
    private TravelDisutility travelDisutility;
    private TravelTime travelTime;
    private Map<Integer, SimpleFeature> zoneFeatureMap;
    private DataSet dataSet;

    public CarSkimUpdater(TravelTime travelTime, TravelDisutility travelDisutility,
                          Map<Integer, SimpleFeature> zoneFeatureMap,
                          Network network, DataSet dataSet) {

        this.network = network;
        this.zoneFeatureMap = zoneFeatureMap;
        this.travelTime = travelTime;
        this.travelDisutility = travelDisutility;
        //creates a matrix of (n+1 zones) rows and columns
        int maxZone = dataSet.getZones().keySet().stream().max(Integer::compareTo).get() + 1;
        this.carTravelTimeMatrix = new DenseDoubleMatrix2D(maxZone, maxZone);
        this.dataSet = dataSet;

    }

    public void run() {
        calculateMatrixFromMatsim();
        updateMatrix();

    }

    private void calculateMatrixFromMatsim() {
        nodesByZone.clear();
        zoneFeatureMap.keySet().stream().parallel().forEach(zoneId -> {
            SimpleFeature originFeature = zoneFeatureMap.get(zoneId);
            nodesByZone.put(zoneId, new LinkedList());
            for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several points in a given origin zone
                Coord originCoord = MatsimPopulationGenerator.getRandomCoordinateInZone(originFeature);
                Node originNode = NetworkUtils.getNearestLink(network, originCoord).getToNode();
                nodesByZone.get(zoneId).add(originNode);
            }
        });
        LOGGER.info("Assigned nodes to  " + nodesByZone.keySet().size());

        zoneFeatureMap.keySet().stream().parallel().forEach(zoneId -> {
            //ArrayList<Map<Id<Node>, LeastCostPathTree.NodeData>> treesAtThisZone = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several points in a given origin zone
                Node originNode = nodesByZone.get(zoneId).get(i);
                LeastCostPathTree leastCoastPathTree = new LeastCostPathTree(travelTime, travelDisutility);
                Map<Id<Node>, LeastCostPathTree.NodeData> tree;
                leastCoastPathTree.calculate(network, originNode, DEFAULT_PEAK_H_S);
                tree = leastCoastPathTree.getTree();
                //treesAtThisZone.add(tree);
                //this.treesByZone.put(zoneId, treesAtThisZone);

                for (int destination : nodesByZone.keySet()) {
                    double sumTravelTime_min = 0.;
                    if (zoneId <= destination) {
                        for (Node destinationNode : nodesByZone.get(destination)) {// several points in a given destination zone
                            double arrivalTime_s = tree.get(destinationNode.getId()).getTime();
                            sumTravelTime_min += ((arrivalTime_s - DEFAULT_PEAK_H_S) / 60.);
                        }

                    carTravelTimeMatrix.setQuick(zoneId, destination, sumTravelTime_min / NUMBER_OF_CALC_POINTS / NUMBER_OF_CALC_POINTS);
                    carTravelTimeMatrix.setQuick(destination, zoneId, sumTravelTime_min / NUMBER_OF_CALC_POINTS / NUMBER_OF_CALC_POINTS);
                    }
                }
            }
        });
        LOGGER.info("Calculated trees for " + nodesByZone.keySet().size());
        LOGGER.info("Completed car matrix update");
    }


    private void updateMatrix() {
        SkimTravelTimes skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.updateSkimMatrix(carTravelTimeMatrix, TransportMode.car);

    }


}
