package de.tum.bgu.msm.modules.trafficAssignment;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.TripRouter;
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
    private final Map<Integer, List<Node>> zoneCalculationNodesMap = new ConcurrentHashMap<>();
    Map<Integer, ArrayList<Map<Id<Node>, LeastCostPathTree.NodeData>>> treeMap = new ConcurrentHashMap<>();

    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 28800;
    private DoubleMatrix2D carTravelTimeMatrix;
    private TravelDisutility travelDisutility;
    private TravelTime travelTime;
    private Map<Integer, SimpleFeature> zoneFeatureMap;
    private final Map<Id<Node>, Map<Integer, Map<Id<Node>, LeastCostPathTree.NodeData>>> treesForNodesByTimes = new HashMap<>();
    private DataSet dataSet;

    public CarSkimUpdater(TravelTime travelTime, TravelDisutility travelDisutility,
                          Map<Integer, SimpleFeature> zoneFeatureMap,
                          Network network, DataSet dataSet) {

        this.network = network;
        this.treesForNodesByTimes.clear();
        this.zoneFeatureMap = zoneFeatureMap;
        this.travelTime = travelTime;
        this.travelDisutility = travelDisutility;
        int maxZone = 4954; // todo
        this.carTravelTimeMatrix = new DenseDoubleMatrix2D(maxZone, maxZone);
        this.dataSet = dataSet;

    }

    public void run(){
        updateZoneConnections();
        updateMatrix();
    }

    private void updateZoneConnections() {
        zoneCalculationNodesMap.clear();
        zoneFeatureMap.keySet().stream().parallel().forEach(zoneId -> {
            SimpleFeature originFeature = zoneFeatureMap.get(zoneId);

            for (int i = 0; i < NUMBER_OF_CALC_POINTS; i++) { // Several points in a given origin zone
                Coord originCoord = MatsimPopulationGenerator.getRandomCoordinateInZone(originFeature);
                Node originNode = NetworkUtils.getNearestLink(network, originCoord).getToNode();
                zoneCalculationNodesMap.put(zoneId, new LinkedList());
                zoneCalculationNodesMap.get(zoneId).add(originNode);

                ArrayList<Map<Id<Node>, LeastCostPathTree.NodeData>> treesByZone = new ArrayList<>();
                LeastCostPathTree leastCoastPathTree = new LeastCostPathTree(travelTime, travelDisutility);
                Map<Id<Node>, LeastCostPathTree.NodeData> tree;
                leastCoastPathTree.calculate(network, originNode, DEFAULT_PEAK_H_S);
                tree = leastCoastPathTree.getTree();
                treesByZone.add(tree);
                treeMap.put(zoneId, treesByZone);
            }
            //LOGGER.info("Completed zone " + zoneId);
        });

        LOGGER.info("There are " + zoneCalculationNodesMap.keySet().size() + " origin zones.");
    }


    private void updateMatrix() {
        AtomicInteger count = new AtomicInteger(0);
        zoneCalculationNodesMap.keySet().stream().parallel().forEach(origin -> {
            for (int destination : zoneCalculationNodesMap.keySet()) {
                double sumTravelTime_min = 0.;
                if (origin <= destination) {
                    for (Map<Id<Node>, LeastCostPathTree.NodeData> tree: treeMap.get(origin)) { // loop different origin nodes
                        for (Node destinationNode : zoneCalculationNodesMap.get(destination)) {// several points in a given destination zone
                            double arrivalTime_s = tree.get(destinationNode.getId()).getTime();
                            sumTravelTime_min += ((arrivalTime_s - DEFAULT_PEAK_H_S) / 60.);
                        }
                    }
                    carTravelTimeMatrix.setQuick(origin, destination, sumTravelTime_min / NUMBER_OF_CALC_POINTS / NUMBER_OF_CALC_POINTS);
                    carTravelTimeMatrix.setQuick(destination, origin, sumTravelTime_min / NUMBER_OF_CALC_POINTS / NUMBER_OF_CALC_POINTS);
                }
            }
        });
        SkimTravelTimes skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.updateSkimMatrix(carTravelTimeMatrix, TransportMode.car);
        LOGGER.info("Completed car matrix update");
    }


}
