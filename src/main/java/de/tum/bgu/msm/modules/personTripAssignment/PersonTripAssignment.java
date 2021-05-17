package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.data.MitoOccupationStatus.STUDENT;
import static de.tum.bgu.msm.data.MitoOccupationStatus.WORKER;
import static de.tum.bgu.msm.data.Purpose.*;

public class PersonTripAssignment extends Module {

    private static final Logger logger = Logger.getLogger(PersonTripAssignment.class);

    public PersonTripAssignment(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
    }

    @Override
    public void run() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for (Purpose activityPurpose : purposes) {
                //todo check that the order of purposes affects the assignment. It crashes if a non-home based is done before a home-based
                for (Iterator<MitoTrip> iterator = household.getTripsForPurpose(activityPurpose).listIterator(); iterator.hasNext(); ) {
                    MitoTrip trip = iterator.next();
                    Map<MitoPerson, Double> probabilitiesByPerson = getProbabilityByPersonForTrip(household, trip);
                    if (probabilitiesByPerson != null && !probabilitiesByPerson.isEmpty()) {
                        selectPersonForTrip(trip, probabilitiesByPerson);
                    } else {
                        logger.warn("Removing " + trip + " since no person could be assigned.");
                        iterator.remove();
                        dataSet.removeTrip(trip.getId());
                    }
                }
            }
        }
    }

    private Map<MitoPerson, Double> getProbabilityByPersonForTrip(MitoHousehold household, MitoTrip trip) {
        Purpose activityPurpose = trip.getTripPurpose();
        Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>(household.getHhSize());
        if (activityPurpose == HBW) {
            assignHBW(household, probabilitiesByPerson);
        } else if (activityPurpose == HBE) {
            assignHBE(household, probabilitiesByPerson);
        } else if (activityPurpose == HBS || activityPurpose == HBO) {
            assignHBSHBO(household, probabilitiesByPerson);
        } else if (activityPurpose == NHBW) {
            assignNHBW(household, probabilitiesByPerson);
        } else if (activityPurpose == NHBO) {
            assignNHBO(household, probabilitiesByPerson);
        } else if (activityPurpose == AIRPORT) {
            assignAIRPORT(household, probabilitiesByPerson);
        }
        return probabilitiesByPerson;
    }



    private void selectPersonForTrip(MitoTrip trip, Map<MitoPerson, Double> probabilitiesByPerson) {
        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
        trip.setPerson(selectedPerson);
        selectedPerson.addTrip(trip);
    }

    private void assignHBW(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getMitoOccupationStatus() == WORKER) {
                final long previousTrips = person.getTrips().stream().filter(trip -> trip.getTripPurpose() == HBW).count();
                probabilitiesByPerson.put(person, Math.pow(10, -previousTrips));
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
            if (person.getMitoOccupationStatus() == STUDENT) {
                long previousTrips = person.getTrips().stream().filter(trip -> trip.getTripPurpose() == HBE).count();
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
            if (person.getMitoOccupationStatus() == WORKER) {
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

    private void assignAIRPORT(MitoHousehold household, Map<MitoPerson,Double> probabilitiesByPerson) {
        //by now we assign all the trips to the airport to residents, with the uniform probability for all hh
        //members. Consider improvement: assign them long-distance travelers' based on socio-demographic attributes.
        fillEquallyDistributedAmongAdults(household, probabilitiesByPerson);
    }

    private void fillEquallyDistributed(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            probabilitiesByPerson.put(person, 1.);
        }
    }

    private void fillEquallyDistributedAmongAdults(MitoHousehold household, Map<MitoPerson, Double> probabilitiesByPerson) {
        for (MitoPerson person : household.getPersons().values()) {
            if (person.getAge() > 17) {
                probabilitiesByPerson.put(person, 1.);
            }
        }
    }
}
