package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;

import java.util.Map;

public interface TripDistribution {
    Map<MitoPerson, Double> getProbabilityByPersonForTrip(MitoHousehold household, MitoTrip trip);
}
