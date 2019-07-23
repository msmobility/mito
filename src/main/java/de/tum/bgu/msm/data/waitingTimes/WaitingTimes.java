package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;

public interface WaitingTimes {
    /**
     * returns the waiting time for a certain trip, origin and destination, by certain mode and at a certain time of day. The parameters
     * could be duplicated, to allow flexibility of queries. The implementation of this interface may use none, some or all
     * of the parameters.
     * @param trip
     * @param origin
     * @param destination
     * @param mode
     * @param timeOfDay_s
     * @return the waiting time in minutes
     */
    double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s);
}
