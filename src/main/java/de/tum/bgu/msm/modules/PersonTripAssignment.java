package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.modules.personTripAssignment.TripDistribution;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.*;

public class PersonTripAssignment extends Module {

    private static final Logger logger = Logger.getLogger(PersonTripAssignment.class);

    private final TripDistribution distribution;

    public PersonTripAssignment(DataSet dataSet) {
        super(dataSet);
        distribution = Resources.INSTANCE.getTripDistributionFactory().getTripDistribution(dataSet);
    }

    @Override
    public void run() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for (Map.Entry<Integer, List<MitoTrip>> entry : household.getTripsByPurpose().entrySet()) {
                List<MitoPerson> persons = household.getPersons();
                String purpose = dataSet.getPurposes()[entry.getKey()];
                double weightSum = 0;
                List<MitoTrip> toDelete = new ArrayList<>();
                for (MitoTrip trip : entry.getValue()) {
                    Map<MitoPerson, Double> probabilitiesByPerson = new HashMap<>();
                    for (MitoPerson person : persons) {
                        double weight = distribution.getWeight(household, person, trip);
                        weightSum += weight;
                        probabilitiesByPerson.put(person, weight);
                    }
                    if (probabilitiesByPerson.isEmpty() || weightSum == 0) {
                        logger.error("Household has " + purpose + " trip but no suitable persons. Deleting the trip.");
                        dataSet.getTrips().remove(trip.getTripId());
                        toDelete.add(trip);
                        continue;
                    }
                    selectPersonForTrip(trip, probabilitiesByPerson);
                }
                entry.getValue().removeAll(toDelete);
            }
        }
    }

    private void selectPersonForTrip(MitoTrip trip, Map<MitoPerson, Double> probabilitiesByPerson) {
        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
        trip.setPerson(selectedPerson);
    }
}
