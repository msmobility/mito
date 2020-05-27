package de.tum.bgu.msm.modules.modeChoice.calculators;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalculator;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;

public class CalibratingModeChoiceCalculatorImpl extends ModeChoiceCalculatorImpl {

    private final ModeChoiceCalculator base;
    private final ModeChoiceCalibrationData calibrationData;

    public CalibratingModeChoiceCalculatorImpl(ModeChoiceCalculator base, ModeChoiceCalibrationData calibrationData) {
        super();
        this.base = base;
        this.calibrationData = calibrationData;
    }


    @Override
    public double[] calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        final double[] baseUtilities = base.calculateUtilities(purpose, household, person, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);
        final double[] calibrationFactors = this.calibrationData.getCalibrationFactorsAsArray(purpose, originZone);
        for (int i = 0; i < baseUtilities.length; i++) {
            baseUtilities[i] += calibrationFactors[i];
        }
        return baseUtilities;
    }
}






