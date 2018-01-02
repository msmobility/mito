package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;
import java.util.Map;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<double[]>{

    protected ModeChoiceJSCalculator(Reader reader) {
        super(reader);
    }

    public double[] calculateProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistanceAuto, double travelDistanceNMT){
        return super.calculate("calculate"+trip.getTripPurpose()+"Probabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistanceAuto,
                travelDistanceNMT);
    }
}
