package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.modules.personTripAssignment.TripDistribution;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PersonTripAssignment extends Module {

    private static final Logger logger = Logger.getLogger(PersonTripAssignment.class);

    private final TripDistribution distribution;

    public PersonTripAssignment(DataSet dataSet) {
        super(dataSet);
        distribution = Resources.INSTANCE.getTripDistributionFactory().createTripDistribution();
    }

    @Override
    public void run() {
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            Iterator<List<MitoTrip>> iterator = household.getTripsByPurpose().values().iterator();
            while (iterator.hasNext()) {
                for (MitoTrip trip : iterator.next()) {
                    Map<MitoPerson, Double> probabilitiesByPerson = distribution.getProbabilityByPersonForTrip(household, trip);
                    if (probabilitiesByPerson != null) {
                        selectPersonForTrip(trip, probabilitiesByPerson);
                    } else {
                        dataSet.getTrips().remove(trip.getTripId());
                        iterator.remove();
                    }
                }
            }
        }

    }

    private void selectPersonForTrip(MitoTrip trip, Map<MitoPerson, Double> probabilitiesByPerson) {
        MitoPerson selectedPerson = MitoUtil.select(probabilitiesByPerson);
        trip.setPerson(selectedPerson);
    }
}
