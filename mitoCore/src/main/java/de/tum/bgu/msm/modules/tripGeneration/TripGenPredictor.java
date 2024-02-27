package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;

import java.util.Map;

public interface TripGenPredictor {

    double getPredictor(MitoHousehold household,
                        MitoPerson person,
                        Map<String, Double> coefficients);

}
