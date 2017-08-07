package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.MitoPerson;

public interface TripDistribution {
    double getWeight(MitoPerson person, String purpose);
}
