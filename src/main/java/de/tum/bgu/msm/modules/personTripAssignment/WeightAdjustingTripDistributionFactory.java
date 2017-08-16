package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.DataSet;

public class WeightAdjustingTripDistributionFactory implements TripDistributionFactory{
    @Override
    public TripDistribution createTripDistribution() {
        return new WeightAdjustingTripDistribution();
    }
}
