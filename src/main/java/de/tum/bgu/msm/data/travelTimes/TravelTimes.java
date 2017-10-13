package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Zone;

public interface TravelTimes {

    double getTravelTimeFromTo(int origin, int destination);
}
