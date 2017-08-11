package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.DataSet;

public class SimpleTripDistributionFactory implements TripDistributionFactory{

    @Override
    public TripDistribution createTripDistribution() {
        return new SimpleTripDistribution();
    }
}
