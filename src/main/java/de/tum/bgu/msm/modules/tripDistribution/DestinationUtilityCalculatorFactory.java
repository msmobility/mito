package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public interface DestinationUtilityCalculatorFactory {
    DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose activityPurpose, double travelDistanceCalibrationK, double impendanceCalibrationK);
}
