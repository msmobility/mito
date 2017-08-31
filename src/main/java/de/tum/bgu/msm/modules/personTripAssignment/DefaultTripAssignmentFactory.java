package de.tum.bgu.msm.modules.personTripAssignment;

public class DefaultTripAssignmentFactory implements TripAssignmentFactory {
    @Override
    public TripAssignment createTripDistribution() {
        return new DefaultTripAssignment();
    }
}
