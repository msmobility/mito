package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.*;

public class MitoTripFactoryTengos implements MitoTripFactory {
    @Override
    public MitoTrip createTrip(int tripId, Purpose tripPurpose) {
        return new MitoTripTengos(new MitoTripImpl(tripId, tripPurpose));
    }
}
