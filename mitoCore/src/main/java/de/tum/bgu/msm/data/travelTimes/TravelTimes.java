package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;

public interface TravelTimes {

    /**
     * Returns the travel time from an origin to a destination, by time of day and mode
     * @param origin is the origin location of class {@link Location}
     * @param destination is the destination location of class {@link Location}
     * @param timeOfDay_s is the time of day in seconds
     * @param mode is the travel mode as string
     * @return is the travel time in minutes
     */
    double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode);

    /**
     * Returns the travel time from an origin to a destination, by time of day and mode
     * @param origin is the origin Region  {@link Region}
     * @param destination is the destination location of class {@link Zone}
     * @param timeOfDay_s is the time of day in seconds
     * @param mode is the travel mode as string
     * @return is the travel time in minutes
     */
    double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode);

    /**
     * Returns the travel time from an origin to a destination, by time of day and mode
     * @param origin is the origin location of class {@link Zone}
     * @param destination is the destination location of class {@link Region}
     * @param timeOfDay_s is the time of day in seconds
     * @param mode is the travel mode as string
     * @return is the travel time in minutes
     */
    double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode);

    /**
     * Returns the peak hour travel timne matrix
     * @param mode is the travel mode as string
     * @return
     */
    IndexedDoubleMatrix2D getPeakSkim(String mode);

    /**
     * Creates a duplicate of the travel time object
     * @return
     */
    TravelTimes duplicate();
}