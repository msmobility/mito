package de.tum.bgu.msm.modules.externalFlows;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.readers.ExternalFlowMatrixReader;
import de.tum.bgu.msm.io.input.readers.ExternalZonesReader;
import de.tum.bgu.msm.io.input.readers.LongDistanceTimeOfDayDistributionReader;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import java.util.HashMap;
import java.util.Map;

public class LongDistanceTraffic extends Module {
    private static Logger logger = Logger.getLogger(LongDistanceTraffic.class);
    private PopulationFactory matsimPopulationFactory;
    private Map<Integer, ExternalFlowZone> zones;
    private Map<ExternalFlowType, HashBasedTable<Integer, Integer, Float>> externalFlows;
    private Map<Integer,Double> departureTimeProbabilityByHour;

    //for pre-analysis only
    private Map<Integer, Float> totalGeneratedFlows = new HashMap<>();
    private Map<Integer, Float> totalAttractedFlows = new HashMap<>();
    private float internalFlow = 0;
    private  float outboundFlow = 0;
    private float inboundFlow = 0;
    private float thruFlow = 0;

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

    private void readZones(){
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
        for ( ExternalFlowType type : ExternalFlowType.values()) {
            HashBasedTable<Integer, Integer, Float> matrix = reader.read(type);
            externalFlows.put(type, matrix);
        }
    }

    public Population addLongDistancePlans(double scalingFactor, Population matsimPopulation){
        matsimPopulationFactory = matsimPopulation.getFactory();
        long personId = 0;
        for ( ExternalFlowType type : ExternalFlowType.values()) {
            HashBasedTable<Integer, Integer, Float> matrix = externalFlows.get(type);
            for (int originId : matrix.rowKeySet() ){
                for (int destId : matrix.columnKeySet()){
                    if (matrix.contains(originId, destId)){
                        addFlow(originId, destId, matrix.get(originId, destId));
                        countTotals(originId, destId, matrix.get(originId, destId));
                        long trips = Math.round(matrix.get(originId, destId) * scalingFactor);
                        for (long trip = 0; trip < trips; trip++){
                            Plan matsimPlan = matsimPopulationFactory.createPlan();
                            Person matsimPerson = matsimPopulationFactory.createPerson(Id.createPersonId("ld" + personId));
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
        return (MitoUtil.select(departureTimeProbabilityByHour)+ MitoUtil.getRandomObject().nextDouble())*3600;
    }

    private void initialize(int zone){
        //for analysis
        totalGeneratedFlows.put(zone, 0f);
        totalAttractedFlows.put(zone, 0f);
    }

    private void addFlow(int origin, int dest, float flow){
        //for analysis
        totalGeneratedFlows.put(origin, totalGeneratedFlows.get(origin) + flow);
        totalAttractedFlows.put(dest, totalAttractedFlows.get(dest) + flow);
    }

    private void printOutTripGenerationAndAttraction(){
        logger.info("zoneId,generated,attracted");
        for (int zoneId: zones.keySet()){
            logger.info(zoneId + "," + totalGeneratedFlows.get(zoneId) + "," + totalAttractedFlows.get(zoneId) );
        }
    }

    private void countTotals(int origin, int dest, float flow){
        ExternalFlowZoneType origType = zones.get(origin).getZoneType();
        ExternalFlowZoneType destType = zones.get(dest).getZoneType();

        if (origType.equals(ExternalFlowZoneType.BORDER) && destType.equals(ExternalFlowZoneType.BORDER)){
            thruFlow += flow;
        } else if ((origType.equals(ExternalFlowZoneType.BEZIRKE)|| origType.equals(ExternalFlowZoneType.NUTS3))
                && destType.equals(ExternalFlowZoneType.BORDER)){
            outboundFlow += flow;
        } else if ((destType.equals(ExternalFlowZoneType.BEZIRKE)|| destType.equals(ExternalFlowZoneType.NUTS3))
                && origType.equals(ExternalFlowZoneType.BORDER)){
            inboundFlow += flow;
        } else {
            internalFlow += flow;
        }
    }

    private void printOutTotals(){
        logger.info("thru flow: " + thruFlow);
        logger.info("outbound " + outboundFlow);
        logger.info("inbound " + inboundFlow);
        logger.info("internal " + internalFlow);
    }

    @Override
    public void run() {

    }
}
