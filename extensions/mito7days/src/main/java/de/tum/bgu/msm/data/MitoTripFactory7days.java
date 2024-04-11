package de.tum.bgu.msm.data;

public class MitoTripFactory7days implements MitoTripFactory {

    @Override
    public MitoTrip createTrip(int tripId, Purpose tripPurpose) {
        return new MitoTrip7days(new MitoTripImpl(tripId, tripPurpose));
    }
}
