package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;

import java.util.EnumMap;

public class CalibratingModeChoiceCalculator2017Impl extends ModeChoiceCalculator2017Impl {

    private final ModeChoiceCalculator base;
    private final ModeChoiceCalibrationData calibrationData;

    public CalibratingModeChoiceCalculator2017Impl(ModeChoiceCalculator base, ModeChoiceCalibrationData calibrationData, Purpose purpose, DataSet dataSet) {
        super(purpose, dataSet);
        this.base = base;
        this.calibrationData = calibrationData;
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        final double[] calibrationFactors = calibrationData.getCalibrationFactorsAsArray(purpose, originZone);
        final EnumMap<Mode, Double> baseUtilities = base.calculateUtilities(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);
        baseUtilities.replaceAll((mode, aDouble) -> aDouble + calibrationFactors[mode.ordinal()]);
        return baseUtilities;
    }
}






