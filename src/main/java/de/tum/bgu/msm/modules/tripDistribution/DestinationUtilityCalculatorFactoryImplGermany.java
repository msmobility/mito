package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImplGermany implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose activityPurpose, double travelDistanceCalibrationK, double impendanceCalibrationK){
        return new DestinationUtilityCalculatorImplGermany(activityPurpose,travelDistanceCalibrationK, impendanceCalibrationK);
    }
}
