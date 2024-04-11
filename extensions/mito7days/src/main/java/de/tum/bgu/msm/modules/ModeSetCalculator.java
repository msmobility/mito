package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;

import java.util.EnumMap;
import java.util.Map;

public interface ModeSetCalculator {
    EnumMap<ModeSet, Double> calculateUtilities(MitoPerson person, Map<String, Map<String, Double>> coefficients, EnumMap<ModeSet, Double> constants);

    double getPredictor(MitoPerson pp, Map<String, Double> coefficients);
}
