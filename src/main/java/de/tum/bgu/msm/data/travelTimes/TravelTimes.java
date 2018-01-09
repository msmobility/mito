package de.tum.bgu.msm.data.travelTimes;

public interface TravelTimes {

    double getTravelTime(int origin, int destination, double timeOfDay_s);
}
