package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;

import java.util.Map;

public class StationDependentWaitingTimes implements WaitingTimes {


    private final AccessAndEgressVariables accessAndEgressVariables;
    private final Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime;
    private final Map<Integer, String> zonesToStationMap;

    public StationDependentWaitingTimes(AccessAndEgressVariables accessAndEgressVariables, Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime, Map<Integer, String> zonesToStationMap){
        this.accessAndEgressVariables = accessAndEgressVariables;
        this.averageWaitingTimesByUAMStationAndTime = averageWaitingTimesByUAMStationAndTime;
        this.zonesToStationMap = zonesToStationMap;
    }


    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s) {
        int zoneId = (int) accessAndEgressVariables.getAccessVariable(origin, destination, mode, AccessAndEgressVariables.AccessVariable.ACCESS_VERTIPORT);
        String station = zonesToStationMap.get(zoneId);
        for (int interval : averageWaitingTimesByUAMStationAndTime.get(station).keySet()){
            if (timeOfDay_s > interval ){
                return averageWaitingTimesByUAMStationAndTime.get(station).get(interval);
            }
        }
        return averageWaitingTimesByUAMStationAndTime.get(station).get(averageWaitingTimesByUAMStationAndTime.get(station).size());
    }
}
