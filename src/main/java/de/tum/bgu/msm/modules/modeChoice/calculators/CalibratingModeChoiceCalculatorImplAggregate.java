package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculatorAggregate;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationDataAggregate;

import java.util.EnumMap;

public class CalibratingModeChoiceCalculatorImplAggregate extends ModeChoiceCalculatorImplAggregate {

    private final ModeChoiceCalculatorAggregate base;
    private final ModeChoiceCalibrationDataAggregate calibrationData;

    public CalibratingModeChoiceCalculatorImplAggregate(ModeChoiceCalculatorAggregate base, ModeChoiceCalibrationDataAggregate calibrationData) {
        super();
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






