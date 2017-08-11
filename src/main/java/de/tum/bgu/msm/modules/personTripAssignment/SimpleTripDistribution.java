package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;

/**
 * This distribution returns random weights for persons fitting to the purpose.
 */
class SimpleTripDistribution implements TripDistribution {

    SimpleTripDistribution(){

    }

    @Override
    public double getWeight(MitoHousehold household, MitoPerson person, MitoTrip trip) {
        if (personFitsToTrip(household, person, trip)) {
            return 1;
        }
        return 0;
    }

    private boolean personFitsToTrip(MitoHousehold household, MitoPerson person, MitoTrip trip) {
        if (trip.getTripPurpose().equals(Purpose.HBW) && !person.getOccupation().equals(Occupation.WORKER) ||
                trip.getTripPurpose().equals(Purpose.HBE) && !person.getOccupation().equals(Occupation.STUDENT)) {
            return false;
        }
        return true;
    }
}
