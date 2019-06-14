package de.tum.bgu.msm.modules.accessibility;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AccessibilityJSCalculator extends JavaScriptCalculator<Double> {

    private final String function;

    protected AccessibilityJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        function = "calculate"+purpose+"Probabilities";
    }

    public double calculateProbabilities(MitoZone origin,
                                           MitoZone destination, TravelTimes travelTimes, double travelDistanceAuto,
                                           double travelDistanceNMT, double peakHour){
        return super.calculate(function,
                origin,
                destination,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour);
    }
}
