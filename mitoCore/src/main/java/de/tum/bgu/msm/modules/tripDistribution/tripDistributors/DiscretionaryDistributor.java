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
public class DiscretionaryDistributor extends AbstractDistributor {

    public DiscretionaryDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                    EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData,
                                    EnumMap<Purpose, Map<Integer,Integer>> personCategories) {
        super(purpose, householdCollection, dataSet, distributionData, personCategories);
    }
}
