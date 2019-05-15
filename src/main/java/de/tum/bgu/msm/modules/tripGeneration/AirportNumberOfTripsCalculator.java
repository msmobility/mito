package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class AirportNumberOfTripsCalculator extends JavaScriptCalculator<Integer> {
    protected AirportNumberOfTripsCalculator(Reader reader) {
        super(reader);
    }

    public int calculateTripRate(int year){
        return calculate("getNumberOfTripsToAirport", year);
    }
}
