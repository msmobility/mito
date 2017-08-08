package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Purpose;

/**
 * This distribution returns random weights for persons fitting to the purpose.
 */
class SimpleTripDistribution implements TripDistribution {

    @Override
    public double getWeight(MitoHousehold household, MitoPerson person, MitoTrip trip) {
        if (personFitsToTrip(household, person, trip)) {
            return 1;
        }
        return 0;
    }

    private boolean personFitsToTrip(MitoHousehold household, MitoPerson person, MitoTrip trip) {
        if (trip.getTripPurpose() == 0 && person.getOccupation() != 1 || trip.getTripPurpose() == 1 && person.getOccupation() != 3) {
            return false;
        }
        return true;
    }
}
