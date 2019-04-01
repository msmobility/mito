package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;

public interface TravelTimes {

    double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode);
    
    double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode);

    IndexedDoubleMatrix2D getPeakSkim(String mode);
}