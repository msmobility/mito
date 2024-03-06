package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;

import java.util.*;

/**
 * @author Nico
 */
public class RecreationalRoundTripDistributor extends AbstractDistributor {

    protected final List<Purpose> priorPurposes = Purpose.getHomeBasedPurposes();

    public RecreationalRoundTripDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                            EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData,
                                            EnumMap<Purpose, Map<Integer,Integer>> personCategories) {
        super(purpose, householdCollection, dataSet, distributionData, personCategories);
    }

    protected Location findOrigin(MitoHousehold household, MitoTrip trip) {
        if(super.random.nextDouble() < 0.85) {
            return household;
        } else {
            Location priorDestination = findPriorDestination(household, trip, priorPurposes);
            if(priorDestination != null) {
                return priorDestination;
            } else {
                return household;
            }
        }
    }
}
