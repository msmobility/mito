package de.tum.bgu.msm.modules.tripDistribution;

import java.util.Map;

public interface DestinationUtilityCalculator {
    double calculateExpUtility(Map<String, Double> variables);
}
