package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;

import java.util.Map;

/**
 * Waiting times that depend on the station (UAM vertiport) and time interval of the day.
 */
public class StationDependentTotalHandlingTimes implements TotalHandlingTimes {


    private final AccessAndEgressVariables accessAndEgressVariables;
    private final Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min;
    private final Map<Integer, String> zonesToStationMap;

    public StationDependentTotalHandlingTimes(AccessAndEgressVariables accessAndEgressVariables, Map<String, Map<Integer, Double>> averageWaitingTimesByUAMStationAndTime_min, Map<Integer, String> zonesToStationMap) {
        this.accessAndEgressVariables = accessAndEgressVariables;
        this.averageWaitingTimesByUAMStationAndTime_min = averageWaitingTimesByUAMStationAndTime_min;
        this.zonesToStationMap = zonesToStationMap;
    }


    @Override
    public double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s) {
        if (mode.equalsIgnoreCase(Mode.uam.toString())) {
            double timeOfDay_s2 = trip.getDepartureInMinutes() * 60;
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
                //did not have an actual UAM-plausible-route but choose still UAM - this is not consistent between mito and uam-matsim.
                //in MITO, the trip that is faster by car is assigned with travel time = 10000 min, so it should not be done (although
                //error terms let the choices a bit random, thus it can be chosen?. In MATSim,
                //the agents can choose an origin destination pair of UAM even if the assigned trip would be faster by car.
                //all these are assumptions now and need further refinement (carlos)
                return 10000.;
            }
        } else {
            //other modes are not considered for waiting times
            return 0;
        }
    }
}
