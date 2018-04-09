package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    DestinationUtilityJSCalculator(Reader reader) {
        super(reader);
    }

    public Double calculateHbwUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBW", travelDistance, targetZone.getTripAttraction(Purpose.HBW));
    }

    public Double calculateHbeUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBE", travelDistance, targetZone.getTripAttraction(Purpose.HBE));
    }

    public Double calculateHbsUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBS", travelDistance, targetZone.getTripAttraction(Purpose.HBS));
    }

    public Double calculateHboUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBO", travelDistance, targetZone.getTripAttraction(Purpose.HBO));
    }

    public Double calculateNhbwUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateNHBW", travelDistance, targetZone.getTripAttraction(Purpose.NHBW));
    }

    public Double calculateNhboUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateNHBO", travelDistance, targetZone.getTripAttraction(Purpose.NHBO));
    }
}
