package de.tum.bgu.msm.modules.accessEgressChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AccessEgressJSCalculatorTransit extends JavaScriptCalculator<double[]>{

    private Purpose purpose;

    protected AccessEgressJSCalculatorTransit(Reader reader, Purpose purpose) {
        super(reader);
        this.purpose = purpose;
    }

    public double[] calculateSecondaryModeProbabilities(MitoHousehold household, MitoPerson person, Mode mainMode, MitoZone origin) {
        if (this.purpose.equals(Purpose.HBW) || this.purpose.equals(Purpose.HBE) || this.purpose.equals(Purpose.HBS) ||
                this.purpose.equals(Purpose.HBO) || (this.purpose.equals(Purpose.AIRPORT) && household.getHomeZone().equals(origin))) {
            return super.calculate("calculateHB",household,person,mainMode,purpose,origin);
        }
        else {
            return super.calculate("calculateNHB",household,person,mainMode,purpose,origin);
        }
    }
}