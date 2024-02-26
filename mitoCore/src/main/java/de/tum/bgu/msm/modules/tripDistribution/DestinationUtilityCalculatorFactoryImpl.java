package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImpl implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK){
        return new DestinationUtilityCalculatorImpl(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
    }
}
