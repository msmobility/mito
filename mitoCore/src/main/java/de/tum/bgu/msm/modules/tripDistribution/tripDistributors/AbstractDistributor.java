package de.tum.bgu.msm.modules.tripDistribution.tripDistributors;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.modules.tripDistribution.TripDistribution;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Nico
 */
public abstract class AbstractDistributor extends RandomizableConcurrentFunction<Void> {

    protected final static Logger logger = Logger.getLogger(AbstractDistributor.class);
    protected final List<TripDistribution.tripDistributionData> tripDistributionData;
    private final Map<Integer,Integer> personCategories;

    protected final Purpose purpose;
    protected final TravelDistances travelDistances;
    private final Collection<MitoHousehold> householdCollection;
    protected final Map<Integer, MitoZone> zonesCopy;
    protected boolean randomFlag;

    public AbstractDistributor(Purpose purpose, Collection<MitoHousehold> householdCollection, DataSet dataSet,
                               EnumMap<Purpose, List<TripDistribution.tripDistributionData>> distributionData,
                               EnumMap<Purpose, Map<Integer,Integer>> personCategories) {
        super(MitoUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.tripDistributionData = distributionData.get(purpose);
        this.personCategories = personCategories == null ? null : personCategories.get(purpose);
        this.householdCollection = householdCollection;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
        this.travelDistances = dataSet.getTravelDistancesAuto();
        this.randomFlag = false;
    }

    @Override
    public Void call() {
        for (MitoHousehold household : householdCollection) {
            if (initialiseHousehold(household)) {
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {

                    // Reset random flag (other methods may flip it for this trip)
                    randomFlag = false;

                    // Get data structure associated with this category
                    int index = personCategories == null ? 0 : personCategories.get(trip.getPerson().getId());
                    TripDistribution.tripDistributionData categoryData = tripDistributionData.get(index);

                    // Find and store origin
                    Location origin = findOrigin(household, trip);
                    trip.setTripOrigin(origin);
                    if (origin == null) {
                        logger.debug("No origin found for trip " + trip);
                        categoryData.failedTripsCounter.incrementAndGet();
                        continue;
                    }

                    // Compute and store destination
                    Location destination = findDestination(trip,index);
                    trip.setTripDestination(destination);
                    if (destination == null) {
                        logger.debug("No destination found for trip " + trip);
                        categoryData.failedTripsCounter.incrementAndGet();
                        continue;
                    }

                    // Save key info
                    categoryData.distributedTripCounter.incrementAndGet();
                    categoryData.distributedTripDistance.addAndGet(travelDistances.getTravelDistance(origin.getZoneId(),destination.getZoneId()));
                    if(randomFlag) {
                        categoryData.randomTripCounter.incrementAndGet();
                        categoryData.randomTripDistance.addAndGet(travelDistances.getTravelDistance(origin.getZoneId(),destination.getZoneId()));
                    }

                    // Post-process
                    postProcessTrip(trip);
                }
            }
        }
        return null;
    }

    protected boolean initialiseHousehold(MitoHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    protected void postProcessTrip(MitoTrip trip) {}

    protected Location findOrigin(MitoHousehold household, MitoTrip trip) {
        return household;
    }

    protected Location findDestination(MitoTrip trip, int categoryIndex) {
        IndexedDoubleMatrix2D probabilityMatrix = tripDistributionData.get(categoryIndex).getUtilityMatrix();
        double[] destinationProbabilities = probabilityMatrix.viewRow(trip.getTripOrigin().getZoneId()).toNonIndexedArray();
        final int destinationInternalIndex = MitoUtil.select(destinationProbabilities, random);
        return zonesCopy.get(probabilityMatrix.getIdForInternalColumnIndex(destinationInternalIndex));
    }


    protected Location findPriorDestination(MitoHousehold household, MitoTrip trip, List<Purpose> priorPurposes) {
        final List<Location> possibleOrigins = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MitoTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleOrigins.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleOrigins.isEmpty()) {
            return MitoUtil.select(random, possibleOrigins);
        } else {
            return  null;
        }
    }


}
