package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nico
 */
public final class MandatoryDistributor extends AbstractDistributor {

    public MandatoryDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData,
                                EnumMap<Purpose, Map<Integer,Integer>> personCategories) {
        super(purpose, householdCollection, dataSet, distributionData, personCategories);
    }

    @Override
    protected Location findDestination(MitoTrip trip, int categoryIndex) {
        if (isFixedByOccupation(trip)) {
                return trip.getPerson().getOccupation();
        } else {
            randomFlag = true;
            return super.findDestination(trip, categoryIndex);
        }
    }

    private boolean isFixedByOccupation(MitoTrip trip) {
        MitoOccupationStatus personOccupation = trip.getPerson().getMitoOccupationStatus();
        if((purpose.equals(Purpose.HBW) && personOccupation.equals(MitoOccupationStatus.WORKER)) ||
                purpose.equals(Purpose.HBE) && personOccupation.equals(MitoOccupationStatus.STUDENT)) {
            return trip.getPerson().getOccupation() != null;
        }
        return false;
    }
}
