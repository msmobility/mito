package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;

public interface TripDistribution {
    double getWeight(MitoHousehold household, MitoPerson person, MitoTrip trip);
}
