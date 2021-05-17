package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;

import java.util.EnumMap;

public class CalibratingModeChoiceCalculatorImpl extends ModeChoiceCalculatorImpl {

    private final ModeChoiceCalculator base;
    private final ModeChoiceCalibrationData calibrationData;

    public CalibratingModeChoiceCalculatorImpl(ModeChoiceCalculator base, ModeChoiceCalibrationData calibrationData) {
        super();
        this.base = base;
        this.calibrationData = calibrationData;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose activityPurpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        final double[] calibrationFactors = calibrationData.getCalibrationFactorsAsArray(activityPurpose, originZone);
        final EnumMap<Mode, Double> baseUtilities = base.calculateUtilities(activityPurpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);
        baseUtilities.replaceAll((mode, aDouble) -> aDouble + calibrationFactors[mode.ordinal()]);
        return baseUtilities;
    }
}






