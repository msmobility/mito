package de.tum.bgu.msm.modules.personTripAssignment;

public class SimpleTripAssignmentFactory implements TripAssignmentFactory {

    @Override
    public TripAssignment createTripDistribution() {
        return new SimpleTripAssignment();
    }
}
