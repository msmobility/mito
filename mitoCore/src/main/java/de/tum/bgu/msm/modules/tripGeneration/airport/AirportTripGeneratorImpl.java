package de.tum.bgu.msm.modules.tripGeneration.airport;

public class AirportTripGeneratorImpl implements AirportTripGenerator {

    private static final int CONSTANT_TRIP_NUMBER = 56600;

    @Override
    public int calculateTripRate(int year){
        return CONSTANT_TRIP_NUMBER;
    }
}
