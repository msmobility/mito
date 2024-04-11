package de.tum.bgu.msm.data;

public interface MitoTripFactory {
    MitoTrip createTrip(int tripId, Purpose tripPurpose);
}
