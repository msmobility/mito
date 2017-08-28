package de.tum.bgu.msm.modules.personTripAssignment;

public class DefaultTripDistributionFactory implements TripDistributionFactory {
    @Override
    public TripDistribution createTripDistribution() {
        return new DefaultTripDistribution();
    }
}
