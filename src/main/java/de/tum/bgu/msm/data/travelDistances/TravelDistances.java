package de.tum.bgu.msm.data.travelDistances;

import de.tum.bgu.msm.data.Location;

public interface TravelDistances {

    /**
     * Returns the travel distance from an origin zone to a destination zone
     * @param origin is the origin zone id, as integer
     * @param destination is the destination zone id, as integer
     * @return is the travel distance in km
     */
    double getTravelDistance(int origin, int destination);
}
