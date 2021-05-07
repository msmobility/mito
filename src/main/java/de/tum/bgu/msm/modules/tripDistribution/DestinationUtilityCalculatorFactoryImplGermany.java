package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImplGermany implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, double travelDistanceCalibrationK, double impendanceCalibrationK){
        return new DestinationUtilityCalculatorImplGermany(purpose,travelDistanceCalibrationK, impendanceCalibrationK);
    }
}
