package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.tripGeneration.TripsByPurposeGenerator;

public interface TripsByPurposeGeneratorFactory {
    TripsByPurposeGenerator createTripGeneratorForThisPurpose(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration);
}
