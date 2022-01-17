package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

import java.util.Map;

public class DestinationUtilityCalculatorFactoryImplSanFrancisco implements DestinationUtilityCalculatorFactory {

    @Override
    public DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, Map<String, Double> coefficients){
        return new DestinationUtilityCalculatorImplSanFrancisco(purpose,coefficients);
    }
}
