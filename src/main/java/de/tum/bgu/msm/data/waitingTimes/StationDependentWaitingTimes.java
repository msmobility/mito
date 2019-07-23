package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;

import java.util.Map;

public class StationDependentWaitingTimes implements WaitingTimes {


    private final AccessAndEgressVariables accessAndEgressVariables;
    private final Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min;
    private final Map<Integer, String> zonesToStationMap;

    public StationDependentWaitingTimes(AccessAndEgressVariables accessAndEgressVariables, Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min, Map<Integer, String> zonesToStationMap) {
        this.accessAndEgressVariables = accessAndEgressVariables;
        this.averageWaitingTimesByUAMStationAndTime_min = averageWaitingTimesByUAMStationAndTime_min;
        this.zonesToStationMap = zonesToStationMap;
    }


    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s) {
        double timeOfDay_s2 = trip.getDepartureInMinutes()*60;
        int zoneId = (int) accessAndEgressVariables.getAccessVariable(origin, destination, mode, AccessAndEgressVariables.AccessVariable.ACCESS_VERTIPORT);
        if (zoneId != -1) {
            String station = zonesToStationMap.get(zoneId);
            for (int interval : averageWaitingTimesByUAMStationAndTime_min.get(station).keySet()) {
                if (timeOfDay_s2 > interval) {
                    return averageWaitingTimesByUAMStationAndTime_min.get(station).get(interval);
                }
            }
            return averageWaitingTimesByUAMStationAndTime_min.get(station).get(averageWaitingTimesByUAMStationAndTime_min.get(station).size());
        } else {
            return 10000.;
        }
    }
}
