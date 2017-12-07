package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;

import java.util.HashMap;
import java.util.Map;

import static de.tum.bgu.msm.data.Occupation.*;
import static de.tum.bgu.msm.data.Purpose.*;


public class DefaultTripAssignment implements TripAssignment {

    @Override
    public Map<MitoPerson, Double> getProbabilityByPersonForTrip(MitoHousehold household, MitoTrip trip) {
        Purpose purpose = trip.getTripPurpose();
        Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>(household.getHhSize());
        if (purpose == HBW) {
            assignHBW(household, probabilitiesByPerson);
        } else if (purpose == HBE) {
            assignHBE(household, probabilitiesByPerson);
        } else if (purpose == HBS || purpose == HBO) {
            assignHBSHBO(household, probabilitiesByPerson);
        } else if (purpose == NHBW) {
            assignNHBW(household, probabilitiesByPerson);
        } else if (purpose == NHBO) {
            assignNHBO(household, probabilitiesByPerson);
        }
        return probabilitiesByPerson;
    }

    private void assignHBW(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation() == WORKER) {
                long previousTrips = person.getTrips().values().stream().filter(trip -> trip.getTripPurpose() == HBW).count();
                double probability = Math.pow(10, -previousTrips);
                probabilitiesByPerson.put(person, probability);
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            for (MitoPerson person : household.getPersons().values()) {
                if (person.getAge() > 16) {
                    probabilitiesByPerson.put(person, 1.);
                }
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void assignHBE(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation() == STUDENT) {
                long previousTrips = person.getTrips().values().stream().filter(trip -> trip.getTripPurpose() == HBE).count();
                double probability = Math.pow(10, -previousTrips);
                probabilitiesByPerson.put(person, probability);
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void assignHBSHBO(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getOccupation() == WORKER) {
                probabilitiesByPerson.put(person, 1. / 3.);
            }
            probabilitiesByPerson.put(person, 1.);
        }
    }

    private void assignNHBW(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoTrip workTrip : household.getTripsForPurpose(HBW)) {
            probabilitiesByPerson.put(workTrip.getPerson(), 1.);
        }
        if (probabilitiesByPerson.isEmpty()) {
            for (MitoPerson person : household.getPersons().values()) {
                if (person.getAge() > 16) {
                    probabilitiesByPerson.put(person, 1.);
                }
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void assignNHBO(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoTrip otherTrip : household.getTripsForPurpose(HBO)) {
            probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
        }
        for (MitoTrip otherTrip : household.getTripsForPurpose(HBS)) {
            probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
        }
        for (MitoTrip otherTrip : household.getTripsForPurpose(HBE)) {
            probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void fillEquallyDistributed(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            probabilitiesByPerson.put(person, 1.);
        }
    }
}
