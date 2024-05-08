package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;

public class TripsByPurposeGeneratorFactoryPersonBasedHurdleAggregate implements TripsByPurposeGeneratorFactory {
    @Override
    public TripsByPurposeGenerator createTripGeneratorForThisPurpose(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration) {
        return new TripsByPurposeGeneratorPersonBasedHurdleAggregateModel(dataSet, purpose, scaleFactorForGeneration);
    }
}
