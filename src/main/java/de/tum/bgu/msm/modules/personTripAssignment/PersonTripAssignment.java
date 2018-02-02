package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

public class PersonTripAssignment extends Module {

    private static final Logger logger = Logger.getLogger(PersonTripAssignment.class);

    private final TripAssignment distribution;

    public PersonTripAssignment(DataSet dataSet) {
        super(dataSet);
        distribution = Resources.INSTANCE.getTripAssignmentFactory().createTripDistribution();
    }

    @Override
    public void run() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for(Purpose purpose: Purpose.values()) {
                for(Iterator<MitoTrip> iterator = household.getTripsForPurpose(purpose).listIterator(); iterator.hasNext(); ) {
                    MitoTrip trip = iterator.next();
                    Map<MitoPerson, Double> probabilitiesByPerson = distribution.getProbabilityByPersonForTrip(household, trip);
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

    private void selectPersonForTrip(MitoTrip trip, Map<MitoPerson, Double> probabilitiesByPerson) {
        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
        trip.setPerson(selectedPerson);
        selectedPerson.addTrip(trip);
    }
}
