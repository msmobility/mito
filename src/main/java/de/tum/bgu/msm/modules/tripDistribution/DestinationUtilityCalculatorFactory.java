package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;

import java.util.Map;

public interface DestinationUtilityCalculatorFactory {
    DestinationUtilityCalculator createDestinationUtilityCalculator(Purpose purpose, Map<String, Double> coefficients);
}
