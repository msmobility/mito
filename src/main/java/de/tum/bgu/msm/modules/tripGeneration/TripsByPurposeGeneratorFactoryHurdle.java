package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;

public class TripsByPurposeGeneratorFactoryHurdle implements TripsByPurposeGeneratorFactory {
    @Override
    public TripsByPurposeGenerator createTripGeneratorForThisPurpose(DataSet dataSet, Purpose activityPurpose, double scaleFactorForGeneration) {
        return new TripsByPurposeGeneratorHurdleModel(dataSet, activityPurpose, scaleFactorForGeneration);
    }
}
