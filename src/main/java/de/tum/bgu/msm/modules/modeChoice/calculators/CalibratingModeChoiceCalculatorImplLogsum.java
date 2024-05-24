package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculatorLogsum;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;

import java.util.EnumMap;
import java.util.Map;

public class CalibratingModeChoiceCalculatorImplLogsum extends ModeChoiceCalculatorImplLogsum {

    private final ModeChoiceCalculatorLogsum base;
    private final ModeChoiceCalibrationData calibrationData;

    public CalibratingModeChoiceCalculatorImplLogsum(ModeChoiceCalculatorLogsum base, ModeChoiceCalibrationData calibrationData) {
        super();
        this.base = base;
        this.calibrationData = calibrationData;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s,
                                                    Map<Mode, Map<String, Double>> coef) {
        final double[] calibrationFactors = calibrationData.getCalibrationFactorsAsArray(purpose, originZone);
        final EnumMap<Mode, Double> baseUtilities = base.calculateUtilities(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);
        baseUtilities.replaceAll((mode, aDouble) -> aDouble + calibrationFactors[mode.ordinal()]);
        return baseUtilities;
    }
}






