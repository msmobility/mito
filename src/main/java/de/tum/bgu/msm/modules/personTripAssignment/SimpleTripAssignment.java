package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This distribution returns random weights for persons fitting to the purpose.
 */
class SimpleTripAssignment implements TripAssignment {

    private static final Logger logger = Logger.getLogger(SimpleTripAssignment.class);

    SimpleTripAssignment(){

    }

    @Override
    public Map<MitoPerson, Double> getProbabilityByPersonForTrip(MitoHousehold household, MitoTrip trip) {
        double weightSum = 0;
        Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>();
        for (MitoPerson person : household.getPersons().values()) {
            double weight = getWeight(household, person, trip);
            weightSum += weight;
            probabilitiesByPerson.put(person, weight);
        }
        if (probabilitiesByPerson.isEmpty() || weightSum == 0) {
            logger.error("Household has " + trip.getTripPurpose() + " trip but no suitable persons. Deleting the trip.");
            return null;
        }
        return probabilitiesByPerson;
    }

    public double getWeight(MitoHousehold household, MitoPerson person, MitoTrip trip) {
        if (personFitsToTrip(person, trip)) {
            return 1;
        }
        return 0;
    }

    private boolean personFitsToTrip(MitoPerson person, MitoTrip trip) {
        if (personNotSuitableForPurpose(person, trip, Purpose.HBW, Occupation.WORKER) ||
                personNotSuitableForPurpose(person, trip, Purpose.HBE, Occupation.STUDENT)) {
            return false;
        }
        return true;
    }

    private boolean personNotSuitableForPurpose(MitoPerson person, MitoTrip trip, Purpose purpose, Occupation occupation) {
        return trip.getTripPurpose().equals(purpose) && !person.getOccupation().equals(occupation);
    }
}
