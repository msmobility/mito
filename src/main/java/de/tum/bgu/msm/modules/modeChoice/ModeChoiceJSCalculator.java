package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;
import java.util.Map;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<double[]>{

    protected ModeChoiceJSCalculator(Reader reader) {
        super(reader);
    }

    public double[] calculateHBWProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateHBWProbabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }

    public double[] calculateHBEProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateHBEProbabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }

    public double[] calculateHBSProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateHBSProbabilities",
                household,
                MitoUtil.getChildrenForHousehold(household),
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }

    public double[] calculateHBOProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateHBOProbabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }

    public double[] calculateNHBWProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateNHBWProbabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }

    public double[] calculateNHBOProbabilities(MitoHousehold household, MitoPerson person, MitoTrip trip, Map<String,Double> travelTimeByMode, double travelDistance){
        return super.calculate("calculateNHBOProbabilities",
                household,
                person,
                trip,
                travelTimeByMode,
                travelDistance);
    }
}
