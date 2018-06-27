package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    private final String function;

    DestinationUtilityJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        this.function = "calculate" + purpose;
    }

    Double calculateUtility(double attraction, double travelDistance) {
        return super.calculate(function, travelDistance, attraction);
    }
}
