package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */
public class MitoPersonImpl implements MitoPerson {

    private static final Logger logger = Logger.getLogger(MitoPersonImpl.class);

    private final int id;
    private final MitoGender mitoGender;
    private final MitoOccupationStatus mitoOccupationStatus;
    private final MitoOccupation occupation;
    private final int age;
    private final boolean driversLicense;
    private final MitoHousehold household;
    private Optional<Boolean> hasBicycle = Optional.empty();
    private Set<MitoTrip> trips = new LinkedHashSet<>();

    public MitoPersonImpl(int id, MitoHousehold household, MitoOccupationStatus mitoOccupationStatus, MitoOccupation occupation, int age, MitoGender mitoGender, boolean driversLicense) {
        this.id = id;
        this.mitoOccupationStatus = mitoOccupationStatus;
        this.occupation = occupation;
        this.age = age;
        this.mitoGender = mitoGender;
        this.driversLicense = driversLicense;
        this.household = household;
    }

    @Override
    public MitoOccupation getOccupation() {
        return occupation;
    }

    @Override
    public MitoOccupationStatus getMitoOccupationStatus() {
        return mitoOccupationStatus;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public MitoGender getMitoGender() {
        return mitoGender;
    }

    @Override
    public boolean hasDriversLicense() {
        return driversLicense;
    }

    @Override
    public Set<MitoTrip> getTrips() {
        return Collections.unmodifiableSet(this.trips);
    }

    @Override
    public void addTrip(MitoTrip trip) {
        this.trips.add(trip);
        if(trip.getPerson() != this) {
            trip.setPerson(this);
        }
    }

    @Override
    public void removeTripFromPerson(MitoTrip trip){
        trips.remove(trip);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Optional<Boolean> getHasBicycle() {
        if (!hasBicycle.isPresent()){
            throw new RuntimeException("The number of bicycles is needed but has not been set");
        }
        return hasBicycle;
    }

    @Override
    public void setHasBicycle(boolean hasBicycle) {
        this.hasBicycle = Optional.of(hasBicycle);
    }


    @Override
    public MitoHousehold getHousehold() {
        return household;
    }

    @Override
    public List<MitoTrip> getTripsForPurpose(Purpose purpose) {
        List<MitoTrip> tripsByPurpose = trips.stream().filter(mitoTrip -> purpose.equals(mitoTrip.getTripPurpose())).collect(Collectors.toList());
        if(tripsByPurpose != null) {
            return tripsByPurpose;
        } else {
            return Collections.emptyList();
        }
    }
    @Override
    public boolean hasTripsForPurpose(Purpose purpose) {
        return getTripsForPurpose(purpose).size() > 0;
    }

}
