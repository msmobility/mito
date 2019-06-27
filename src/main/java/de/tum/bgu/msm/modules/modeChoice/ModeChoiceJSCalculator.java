package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<double[]>{

    private final String function;

    protected ModeChoiceJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        function = "calculate"+purpose+"Probabilities";
    }

    public double[] calculateProbabilities(MitoHousehold household, MitoPerson person, MitoZone origin,
                                           MitoZone destination, TravelTimes travelTimes, double travelDistanceAuto,
                                           double travelDistanceNMT, double peakHour){
        return super.calculate(function,
                household,
                person,
                origin,
                destination,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour);
    }

    public double[] calculateProbabilitiesUAM(MitoHousehold household, MitoPerson person, MitoZone origin,
                                              MitoZone destination, TravelTimes travelTimes, AccessAndEgressVariables accessAndEgressVariables, double travelDistanceAuto,
                                              double travelDistanceNMT, double travelCostUAM, double peakHour, double boardingTime, double uamCost){
        return super.calculate(function,
                household,
                person,
                origin,
                destination,
                travelTimes,
                accessAndEgressVariables,
                travelDistanceAuto,
                travelDistanceNMT,
                travelCostUAM,
                peakHour,boardingTime,uamCost);
    }
}
