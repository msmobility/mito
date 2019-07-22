package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;

public interface WaitingTimes {
    double getWaitingTime(Location origin, Location destination, String mode);
}
