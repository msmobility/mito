package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person2TripAssignment extends Module {

    public Person2TripAssignment(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        TripDistribution distribution = dataSet.getTripDistribution();
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for (String purpose : dataSet.getPurposes()) {
                int index = dataSet.getPurposeIndex(purpose);
                List<MitoTrip> trips = household.getTripsByPurpose().get(index);
                Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>();
                List<MitoPerson> persons = household.getPersons();
                if (purpose.equals("HBW")) {
                    for (MitoPerson person : persons) {
                        if (person.getOccupation() == 1) {
                            double weight = distribution.getWeight(person, purpose);
                            probabilitiesByPerson.put(person, weight);
                        }
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                } else if (purpose.equals("HBE")) {
                    for (MitoPerson person : persons) {
                        if (person.getOccupation() == 3) {
                            double weight = distribution.getWeight(person, purpose);
                            probabilitiesByPerson.put(person, weight);
                        }
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                } else if (purpose.equals("HBS")) {
                    for (MitoPerson person : persons) {
                        double weight = distribution.getWeight(person, purpose);
                        probabilitiesByPerson.put(person, weight);
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                } else if (purpose.equals("HBO")) {
                    for (MitoPerson person : persons) {
                        double weight = distribution.getWeight(person, purpose);
                        probabilitiesByPerson.put(person, weight);
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                } else if (purpose.equals("NHBW")) {
                    for (MitoPerson person : persons) {
                        double weight = distribution.getWeight(person, purpose);
                        probabilitiesByPerson.put(person, weight);
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                } else if (purpose.equals("NHBO")) {
                    for (MitoPerson person : persons) {
                        double weight = distribution.getWeight(person, purpose);
                        probabilitiesByPerson.put(person, weight);
                    }
                    for (MitoTrip trip : trips) {
                        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
                        trip.setPerson(selectedPerson);
                        probabilitiesByPerson.replace(selectedPerson, probabilitiesByPerson.get(selectedPerson) / 2);
                    }
                }
            }
        }
    }
}
