package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoAggregatePersona;
import de.tum.bgu.msm.data.Purpose;

public interface TripsByPurposeGeneratorFactoryAggregate {
    TripsByPurposeGeneratorAggregate createTripGeneratorForThisPurpose(DataSet dataSet, Purpose purpose,
                                                                       double scaleFactorForGeneration, MitoAggregatePersona persona,
                                                                       AreaTypes.SGType areaType);
}
