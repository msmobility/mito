package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Region;

public interface TravelTimes {

	@Deprecated
    double getTravelTime(int origin, int destination, double timeOfDay_s, String mode);
    
    double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode);
    
    double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode);
}