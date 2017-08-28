package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Purpose;

import java.util.HashMap;
import java.util.Map;

import static de.tum.bgu.msm.resources.Occupation.STUDENT;
import static de.tum.bgu.msm.resources.Occupation.WORKER;
import static de.tum.bgu.msm.resources.Purpose.*;


public class DefaultTripDistribution implements TripDistribution {
    @Override
    public Map<MitoPerson, Double> getProbabilityByPersonForTrip(MitoHousehold household, MitoTrip trip) {
        Purpose purpose = trip.getTripPurpose();
        Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>();
        if(purpose == HBW) {
            distributeHBW(household, probabilitiesByPerson);
        } else if(purpose == HBE){
            distributeHBE(household, probabilitiesByPerson);
        } else if(purpose == HBS || purpose == HBO) {
            distributeHBSHBO(household, probabilitiesByPerson);
        } else if(purpose == NHBW) {
            distributeNHBW(household, probabilitiesByPerson);
        } else if(purpose == NHBO) {
            distributeNHBO(household, probabilitiesByPerson);
        }
        return probabilitiesByPerson;
    }

    private void distributeHBW(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for(MitoPerson person: household.getPersons()) {
            if(person.getOccupation() == WORKER) {
                probabilitiesByPerson.put(person, 1.);
            }
        }
        if(probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void distributeHBE(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons()) {
            if (person.getOccupation() == STUDENT) {
                probabilitiesByPerson.put(person, 1.);
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void distributeHBSHBO(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        fillEquallyDistributed(household, probabilitiesByPerson);
    }

    private void distributeNHBW(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        if(household.getTripsByPurpose().containsKey(HBW)) {
            for (MitoTrip workTrip : household.getTripsByPurpose().get(HBW)) {
                probabilitiesByPerson.put(workTrip.getPerson(), 1.);
            }
        }
        if (probabilitiesByPerson.isEmpty()) {
            fillEquallyDistributed(household, probabilitiesByPerson);
        }
    }

    private void distributeNHBO(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        if(household.getTripsByPurpose().containsKey(HBO)) {
            for (MitoTrip otherTrip : household.getTripsByPurpose().get(HBO)) {
                probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
            }
        }
        if(household.getTripsByPurpose().containsKey(HBS)) {
            for (MitoTrip otherTrip : household.getTripsByPurpose().get(HBS)) {
                probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
            }
        }
        if(household.getTripsByPurpose().containsKey(HBE)) {
            for (MitoTrip otherTrip : household.getTripsByPurpose().get(HBE)) {
                probabilitiesByPerson.put(otherTrip.getPerson(), 1.);
            }
        }
        if(household.getTripsByPurpose().containsKey(HBO)) {
            if (probabilitiesByPerson.isEmpty()) {
                fillEquallyDistributed(household, probabilitiesByPerson);
            }
        }
    }

    private void fillEquallyDistributed(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for(MitoPerson person: household.getPersons()) {
            probabilitiesByPerson.put(person, 1.);
        }
    }
}
