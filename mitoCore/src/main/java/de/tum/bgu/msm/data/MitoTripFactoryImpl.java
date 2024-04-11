package de.tum.bgu.msm.data;

public class MitoTripFactoryImpl implements MitoTripFactory {

    @Override
    public MitoTrip createTrip(int tripId, Purpose tripPurpose) {
        return new MitoTripImpl(tripId, tripPurpose);
    }
}
