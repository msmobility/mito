package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;

public interface TripsByPurposeGeneratorFactory {
    TripsByPurposeGenerator createTripGeneratorForThisPurpose(DataSet dataSet, Purpose activityPurpose, double scaleFactorForGeneration);
}
