package de.tum.bgu.msm.modules.tripDistribution;

import com.google.common.collect.Table;
import de.tum.bgu.msm.data.*;

public class MandatoryTripDistributor extends TripDistributor {

    private final Occupation occupation;

    public MandatoryTripDistributor(Purpose purpose, DataSet dataSet, Table<Integer, Integer, Double> utilityMatrix, Occupation occupation) {
        super(purpose, dataSet, utilityMatrix);
        this.occupation = occupation;
    }

    @Override
    protected void distributeHouseholdTrips(MitoHousehold household) {
        probabilities = utilityMatrix.row(household.getHomeZone().getZoneId());
        for(MitoTrip trip: household.getTripsForPurpose(purpose)) {
            trip.setTripOrigin(household.getHomeZone());
            if(trip.getPerson().getOccupation() == occupation && trip.getPerson().getWorkzone() != null) {
                trip.setTripDestination(household.getHomeZone());
                TripDistribution.DISTRIBUTED_TRIPS_COUNTER.incrementAndGet();
            } else {
//                selectDestination(trip);
            }
        }
    }
}
