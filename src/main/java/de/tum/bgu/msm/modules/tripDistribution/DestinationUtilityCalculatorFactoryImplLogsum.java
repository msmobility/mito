package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImplLogsum implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, double logsumCalibrationK, double attractionCalibrationK){
        return new DestinationUtilityCalculatorImplLogsum(purpose,logsumCalibrationK, attractionCalibrationK);
    }
}
