package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AirportLogsumCalculator extends JavaScriptCalculator<Double> {
    protected AirportLogsumCalculator(Reader reader) {
        super(reader);
    }

    public double calculateLogsumForThisZone(MitoZone origin, MitoZone destination, TravelTimes travelTimes, double travelDistanceAuto, double peak_hour_s){
        return calculate("returnLogsumAIRPORT", null, null, origin, destination, travelTimes, travelDistanceAuto, null, peak_hour_s);
    }

    public double calculateLogsumForThisZoneUAM(MitoZone origin, MitoZone destination, TravelTimes travelTimes, double travelDistanceAuto, double peak_hour_s, double boardingTime, double uamCost){
        return calculate("returnLogsumAIRPORT", null, null, origin, destination, travelTimes, travelDistanceAuto, null, peak_hour_s, boardingTime, uamCost);
    }
}
