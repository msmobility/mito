package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.DataSet;

public interface TripDistributionFactory {

    public TripDistribution getTripDistribution(DataSet dataset);

}
