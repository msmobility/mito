package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Zone;

public interface TravelTimes {

    public double getTravelTimeFromTo(Zone origin, Zone destination);
}
