package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AirportTripRateCalculator extends JavaScriptCalculator<Double> {
    protected AirportTripRateCalculator(Reader reader) {
        super(reader);
    }

    public double calculateTripRate(MitoHousehold hh, int airportZone, TravelDistances travelDistances){
        return calculate("calculateTripRateToAirport",hh, airportZone, travelDistances);
    }
}
