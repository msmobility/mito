package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import com.google.common.collect.ImmutableList;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;

import java.util.*;

import static de.tum.bgu.msm.data.Purpose.*;

/**
 * @author Nico
 */
public class NonHomeBasedDistributor extends AbstractDistributor {

    protected final List<Purpose> priorPurposes;
    protected final MitoOccupationStatus relatedMitoOccupationStatus;
    private final EnumMap<Purpose, List<TripDistribution.tripDistributionData>> allTripDistributionData;
    private final EnumMap<Purpose,Map<Integer,Integer>> allPersonCategories;

    public NonHomeBasedDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                                   EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData,
                                   EnumMap<Purpose, Map<Integer,Integer>> personCategories) {
        super(purpose, householdCollection, dataSet, distributionData, personCategories);
        if(this.purpose.equals(NHBW)) {
            this.priorPurposes = Collections.singletonList(HBW);
            this.relatedMitoOccupationStatus = MitoOccupationStatus.WORKER;
        } else if(this.purpose.equals(NHBO)){
            this.priorPurposes = ImmutableList.of(HBO, HBE, HBR, HBS);
            this.relatedMitoOccupationStatus = null;
        } else {
            throw new RuntimeException("Unknown non-home-based purpose: \"" + purpose + "\"");
        }
        this.allTripDistributionData = distributionData;
        this.allPersonCategories = personCategories;
    }

    @Override
    protected Location findOrigin(MitoHousehold household, MitoTrip trip) {
        MitoPerson person = trip.getPerson();
        Location priorOrigin = findPriorDestination(household, trip, priorPurposes);

        if(priorOrigin != null) {
            return priorOrigin;
        } else if (person.getMitoOccupationStatus().equals(relatedMitoOccupationStatus) && person.getOccupation() != null) {
            return person.getOccupation();
        } else {
            final Purpose randomlySelectedPurpose = MitoUtil.select(random, priorPurposes);
            return findRandomOrigin(household, trip, randomlySelectedPurpose);
        }
    }

    private MitoZone findRandomOrigin(MitoHousehold household, MitoTrip trip, Purpose priorPurpose) {
        randomFlag = true;

        int priorPurposeCategoryIndex = allPersonCategories == null ? 0 : allPersonCategories.get(priorPurpose).get(trip.getPerson().getId());
        final IndexedDoubleMatrix1D originProbabilities = allTripDistributionData.get(priorPurpose).
                get(priorPurposeCategoryIndex).getUtilityMatrix().viewRow(household.getHomeZone().getId());
        final int destinationInternalId = MitoUtil.select(originProbabilities.toNonIndexedArray(), random);

        return zonesCopy.get(originProbabilities.getIdForInternalIndex(destinationInternalId));
    }
}
