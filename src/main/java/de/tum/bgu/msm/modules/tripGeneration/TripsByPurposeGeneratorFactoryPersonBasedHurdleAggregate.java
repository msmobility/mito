package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoAggregatePersona;
import de.tum.bgu.msm.data.Purpose;

public class TripsByPurposeGeneratorFactoryPersonBasedHurdleAggregate implements TripsByPurposeGeneratorFactoryAggregate {
    @Override
    public TripsByPurposeGeneratorAggregate createTripGeneratorForThisPurpose(DataSet dataSet, Purpose purpose,
                                                                              double scaleFactorForGeneration, MitoAggregatePersona persona, AreaTypes.SGType areaType) {
        return new TripsByPurposeGeneratorPersonBasedHurdleAggregateModel(dataSet, purpose, scaleFactorForGeneration, persona, areaType);
    }
}
