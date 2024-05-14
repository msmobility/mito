package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public interface DestinationUtilityCalculatorFactoryAggregate {
    DestinationUtilityCalculatorAggregate createDestinationUtilityCalculator(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK);
}
