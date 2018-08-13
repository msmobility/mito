package de.tum.bgu.msm.modules.externalFlows;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.readers.ExternalFlowMatrixReader;
import de.tum.bgu.msm.io.input.readers.ExternalZonesReader;
import de.tum.bgu.msm.io.input.readers.LongDistanceTimeOfDayDistributionReader;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LongDistanceTraffic extends Module {
    private static Logger logger = Logger.getLogger(LongDistanceTraffic.class);
    private PopulationFactory matsimPopulationFactory;
    private Map<Integer, ExternalFlowZone> zones;
    private Map<ExternalFlowType, HashBasedTable<Integer, Integer, Float>> externalFlows;
    private Map<Integer, Double> departureTimeProbabilityByHour;

    //for pre-analysis only
    private Map<Integer, Float> totalGeneratedFlows = new HashMap<>();
    private Map<Integer, Float> totalAttractedFlows = new HashMap<>();
    private Map<ExternalFlowType, Float> internalFlow = new HashMap<>();
    private Map<ExternalFlowType, Float> outboundFlow = new HashMap<>();
    private Map<ExternalFlowType, Float> inboundFlow = new HashMap<>();
    private Map<ExternalFlowType, Float> thruFlow = new HashMap<>();

    public LongDistanceTraffic(DataSet dataSet) {
        super(dataSet);
        readDepartureTimeDistribution();
        readZones();
        readMatrices();
    }

    private void readDepartureTimeDistribution() {
        LongDistanceTimeOfDayDistributionReader reader = new LongDistanceTimeOfDayDistributionReader(dataSet);
        reader.read();
        departureTimeProbabilityByHour = reader.getDepartureTimeDistribution();
    }

    private void readZones() {
        ExternalZonesReader reader = new ExternalZonesReader(dataSet);
        reader.read();
        zones = reader.getZones();
        for (int id : zones.keySet()) {
            initialize(id);
        }
    }

    private void readMatrices() {
        ExternalFlowMatrixReader reader = new ExternalFlowMatrixReader(dataSet, zones);
        externalFlows = new HashMap<>();
        for (ExternalFlowType type : ExternalFlowType.values()) {
            HashBasedTable<Integer, Integer, Float> matrix = reader.read(type);
            externalFlows.put(type, matrix);
        }
    }

    public Population addLongDistancePlans(double scalingFactor, Population matsimPopulation) {
        matsimPopulationFactory = matsimPopulation.getFactory();
        long personId = 0;
        for (ExternalFlowType type : ExternalFlowType.values()) {
            HashBasedTable<Integer, Integer, Float> matrix = externalFlows.get(type);
            for (int originId : matrix.rowKeySet()) {
                for (int destId : matrix.columnKeySet()) {
                    if (matrix.contains(originId, destId)) {
                        addFlow(originId, destId, matrix.get(originId, destId));
                        countTotals(originId, destId, matrix.get(originId, destId), type);
                        long trips = getNumberOfTripsFromDecimal(matrix.get(originId, destId),scalingFactor);
                        for (long trip = 0; trip < trips; trip++) {
                            Plan matsimPlan = matsimPopulationFactory.createPlan();
                            Person matsimPerson = matsimPopulationFactory.createPerson(Id.createPersonId(ExternalFlowType.getPrefixForType(type) + personId));
                            matsimPerson.addPlan(matsimPlan);
                            Activity homeActivity =
                                    matsimPopulationFactory.createActivityFromCoord("home", zones.get(originId).getCoordinatesForTripGeneration());
                            homeActivity.setEndTime(selectDepartureTimeInSeconds());
                            matsimPlan.addActivity(homeActivity);
                            Activity destinationActivity =
                                    matsimPopulationFactory.createActivityFromCoord("other", zones.get(destId).getCoordinatesForTripGeneration());
                            matsimPlan.addLeg(matsimPopulationFactory.createLeg(ExternalFlowType.getMatsimMode(type)));
                            matsimPlan.addActivity(destinationActivity);
                            matsimPopulation.addPerson(matsimPerson);
                            personId++;
                        }
                    }
                }
            }
        }
        printOutTotals();
        return matsimPopulation;
    }

    private double selectDepartureTimeInSeconds() {
        return (MitoUtil.select(departureTimeProbabilityByHour) + MitoUtil.getRandomObject().nextDouble()) * 3600;
    }

    private void initialize(int zone) {
        //for analysis
        totalGeneratedFlows.put(zone, 0f);
        totalAttractedFlows.put(zone, 0f);

        for (ExternalFlowType type : ExternalFlowType.values()) {
            thruFlow.put(type, 0.f);
            outboundFlow.put(type, 0.f);
            inboundFlow.put(type, 0.f);
            internalFlow.put(type, 0.f);
        }
    }

    private void addFlow(int origin, int dest, float flow) {
        //for analysis
        totalGeneratedFlows.put(origin, totalGeneratedFlows.get(origin) + flow);
        totalAttractedFlows.put(dest, totalAttractedFlows.get(dest) + flow);
    }

    private void printOutTripGenerationAndAttraction() {
        logger.info("zoneId,generated,attracted");
        for (int zoneId : zones.keySet()) {
            logger.info(zoneId + "," + totalGeneratedFlows.get(zoneId) + "," + totalAttractedFlows.get(zoneId));
        }
    }

    private void countTotals(int origin, int dest, float flow, ExternalFlowType type) {
        ExternalFlowZoneType origType = zones.get(origin).getZoneType();
        ExternalFlowZoneType destType = zones.get(dest).getZoneType();

        if (origType.equals(ExternalFlowZoneType.BORDER) && destType.equals(ExternalFlowZoneType.BORDER)) {
            thruFlow.put(type, thruFlow.get(type) + flow);
        } else if ((origType.equals(ExternalFlowZoneType.BEZIRKE) || origType.equals(ExternalFlowZoneType.NUTS3))
                && destType.equals(ExternalFlowZoneType.BORDER)) {
            outboundFlow.put(type, outboundFlow.get(type) + flow);
        } else if ((destType.equals(ExternalFlowZoneType.BEZIRKE) || destType.equals(ExternalFlowZoneType.NUTS3))
                && origType.equals(ExternalFlowZoneType.BORDER)) {
            inboundFlow.put(type, inboundFlow.get(type) + flow);
        } else {
            internalFlow.put(type, internalFlow.get(type) + flow);
        }
    }

    private void printOutTotals() {
        for (ExternalFlowType type : ExternalFlowType.values()) {
            logger.info("type of traffic," + type.toString());
            logger.info("thru \t outbound \t inbound \t internal");
            logger.info(thruFlow.get(type) + "\t" +
                    outboundFlow.get(type) + "\t" +
                    inboundFlow.get(type) + "\t" +
                    internalFlow.get(type));
        }
    }

    @Override
    public void run() {

    }

    private long getNumberOfTripsFromDecimal(double realValue, double scalingFactor){
        long trips = Math.round(realValue * scalingFactor);
        double decimalPart = realValue * scalingFactor - trips;
        if (decimalPart > 0 && MitoUtil.getRandomObject().nextDouble() < decimalPart) {
            trips++;
            //avoids discarding trips when scaling down
        } else if (decimalPart < 0 && MitoUtil.getRandomObject().nextDouble() < -decimalPart) {
            trips--;
            //avoids considering too many trips when scaling down
        }
        return trips;
    }


}
