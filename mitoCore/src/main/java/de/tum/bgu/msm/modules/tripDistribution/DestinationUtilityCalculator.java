package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public interface DestinationUtilityCalculator {
    double calculateUtility(double attraction, double travelDistance);

    void prepare(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK);
}
