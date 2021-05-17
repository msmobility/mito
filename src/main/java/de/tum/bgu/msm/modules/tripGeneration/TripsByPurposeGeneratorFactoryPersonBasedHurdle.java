package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;

public class TripsByPurposeGeneratorFactoryPersonBasedHurdle implements TripsByPurposeGeneratorFactory {
    @Override
    public TripsByPurposeGenerator createTripGeneratorForThisPurpose(DataSet dataSet, Purpose activityPurpose, double scaleFactorForGeneration) {
        return new TripsByPurposeGeneratorPersonBasedHurdleModel(dataSet, activityPurpose, scaleFactorForGeneration);
    }
}
