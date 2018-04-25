package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<double[]>{

    protected ModeChoiceJSCalculator(Reader reader) {
        super(reader);
    }

    public double[] calculateProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip,
                                           TravelTimes travelTimes, double travelDistanceAuto,
                                           double travelDistanceNMT, double peakHour){
        return super.calculate("calculate"+trip.getTripPurpose()+"Probabilities",
                household,
                person,
                trip,
                travelTimes,
                travelDistanceAuto,
                travelDistanceNMT,
                peakHour);
    }
}
