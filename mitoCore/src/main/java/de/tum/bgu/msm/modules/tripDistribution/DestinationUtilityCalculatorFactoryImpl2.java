package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImpl2 implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK){
        return new DestinationUtilityCalculatorImpl2(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
    }
}
