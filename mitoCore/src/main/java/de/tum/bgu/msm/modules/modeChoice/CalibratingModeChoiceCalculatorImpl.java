package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static de.tum.bgu.msm.data.Mode.*;
import static de.tum.bgu.msm.data.Mode.bicycle;

public class CalibratingModeChoiceCalculatorImpl extends AbstractModeChoiceCalculator {

    private final ModeChoiceCalculator base;
    private final ModeChoiceCalibrationData calibrationData;

    public CalibratingModeChoiceCalculatorImpl(ModeChoiceCalculator base, ModeChoiceCalibrationData calibrationData) {
        super();
        this.base = base;
        this.calibrationData = calibrationData;
        super.setNests(((AbstractModeChoiceCalculator)base).getNests());
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        final double[] calibrationFactors = calibrationData.getCalibrationFactorsAsArray(purpose, originZone);
        final EnumMap<Mode, Double> baseUtilities = base.calculateUtilities(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);
        baseUtilities.replaceAll((mode, aDouble) -> aDouble + calibrationFactors[mode.ordinal()]);
        return baseUtilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        return null;
    }
}






