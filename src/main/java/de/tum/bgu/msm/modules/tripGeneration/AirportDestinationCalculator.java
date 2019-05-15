package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AirportDestinationCalculator extends JavaScriptCalculator<Double> {


    protected AirportDestinationCalculator(Reader reader) {
        super(reader);
    }

    public double calculateUtilityOfThisZone(double popEmp, double logsum, int plz, AreaTypes.SGType areaType){
        return calculate("calculateUtility", popEmp, logsum, plz, areaType);

    }
}
