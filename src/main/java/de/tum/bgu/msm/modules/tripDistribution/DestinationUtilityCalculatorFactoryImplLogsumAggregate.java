package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

public class DestinationUtilityCalculatorFactoryImplLogsumAggregate implements DestinationUtilityCalculatorFactoryAggregate {

    @Override
    public DestinationUtilityCalculatorAggregate createDestinationUtilityCalculator(Purpose purpose, double logsumCalibrationK, double attractionCalibrationK){
        return new DestinationUtilityCalculatorImplLogsumAggregate(purpose,logsumCalibrationK, attractionCalibrationK);
    }
}
