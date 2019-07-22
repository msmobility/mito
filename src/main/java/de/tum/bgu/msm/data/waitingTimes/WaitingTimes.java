package de.tum.bgu.msm.data.waitingTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MitoTrip;

public interface WaitingTimes {
    double getWaitingTime(MitoTrip trip, Location origin, Location destination, String mode, double timeOfDay_s);
}
