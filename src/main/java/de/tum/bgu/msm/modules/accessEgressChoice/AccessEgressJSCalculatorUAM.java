package de.tum.bgu.msm.modules.accessEgressChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AccessEgressJSCalculatorUAM extends JavaScriptCalculator<double[]> {
    private final String function;
    private final Purpose purpose;

    protected AccessEgressJSCalculatorUAM(Reader reader, Purpose purpose) {
        super(reader);
        function = "calculate"+purpose+"Probabilities";
        this.purpose = purpose;
    }

    public double[] calculateProbabilities(MitoHousehold household, MitoPerson person, MitoZone origin,
                                           MitoZone destination, TravelTimes travelTimes, double travelDistanceAuto,
                                           double travelDistanceNMT, double peakHour){

        if(Purpose.AIRPORT.equals(this.purpose)) {
            if (household.getHomeZone().equals(origin)) {
                return super.calculate("calculateHBOProbabilities",
                        household,
                        person,
                        origin,
                        destination,
                        travelTimes,
                        travelDistanceAuto,
                        travelDistanceNMT,
                        peakHour);
            } else {
                return super.calculate("calculateNHBOProbabilities",
                        household,
                        person,
                        origin,
                        destination,
                        travelTimes,
                        travelDistanceAuto,
                        travelDistanceNMT,
                        peakHour);
            }
        }

        else {
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
    }
}
