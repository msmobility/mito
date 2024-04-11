package de.tum.bgu.msm.data;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MitoPerson extends Id {
    MitoOccupation getOccupation();

    MitoOccupationStatus getMitoOccupationStatus();

    @Override
    int getId();

    int getAge();

    MitoGender getMitoGender();

    boolean hasDriversLicense();

    Set<MitoTrip> getTrips();

    void addTrip(MitoTrip trip);

    void removeTripFromPerson(MitoTrip trip);

    @Override
    int hashCode();

    Optional<Boolean> getHasBicycle();

    void setHasBicycle(boolean hasBicycle);

    MitoHousehold getHousehold();

    List<MitoTrip> getTripsForPurpose(Purpose purpose);

    boolean hasTripsForPurpose(Purpose purpose);
}
