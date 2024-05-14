package de.tum.bgu.msm.modules.tripDistribution;

public interface DestinationUtilityCalculatorAggregate {
    double calculateExpUtility(double attraction, double travelDistance);

    double calculateExpUtility2(double tripAttraction, double travelDistance, double travelDistance1);
}
