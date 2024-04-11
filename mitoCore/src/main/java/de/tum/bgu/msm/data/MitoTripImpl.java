package de.tum.bgu.msm.data;

import org.matsim.api.core.v01.population.Person;

/**
 * Holds trip objects for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Mar 26, 2017 in Munich, Germany
 */
public class MitoTripImpl implements MitoTrip {

    private final int tripId;
    private final Purpose tripPurpose;

    private Location tripOrigin;
    private Location tripDestination;

    private MitoPerson person;

    private Mode tripMode;

    private int departureInMinutes;
    private int departureInMinutesReturnTrip = -1;

    private Person matsimPerson;

    public MitoTripImpl(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    @Override
    public int getId() {
        return tripId;
    }

    @Override
    public Location getTripOrigin() {
        return tripOrigin;
    }

    @Override
    public void setTripOrigin(Location origin) {
        this.tripOrigin = origin;
    }

    @Override
    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    @Override
    public Location getTripDestination() {
        return this.tripDestination;
    }

    @Override
    public void setTripDestination(Location destination) {
        this.tripDestination = destination;
    }

    @Override
    public MitoPerson getPerson() {
        return person;
    }

    @Override
    public void setPerson(MitoPerson person) {
        this.person = person;
//        if (!person.getTrips().contains(this)) {
//            person.addTrip(this);
//        }
    }

    @Override
    public Mode getTripMode() {
        return tripMode;
    }

    @Override
    public void setTripMode(Mode tripMode) {
        this.tripMode = tripMode;
    }

    @Override
    public void setDepartureInMinutes(int departureInMinutes) {
        this.departureInMinutes = departureInMinutes;
    }

    @Override
    public void setDepartureInMinutesReturnTrip(int departureInMinutesReturnTrip) {
        this.departureInMinutesReturnTrip = departureInMinutesReturnTrip;
    }

    @Override
    public int getDepartureInMinutes() {
        return departureInMinutes;
    }

    @Override
    public int getDepartureInMinutesReturnTrip() {
        return departureInMinutesReturnTrip;
    }

    @Override
    public int getTripId() {
        return tripId;
    }

    @Override
    public Person getMatsimPerson() {
        return matsimPerson;
    }

    @Override
    public void setMatsimPerson(Person matsimPerson) {
        this.matsimPerson = matsimPerson;
    }

    @Override
    public boolean isHomeBased() {
        return !this.getTripPurpose().equals(Purpose.NHBW) &&
                !this.getTripPurpose().equals(Purpose.NHBO) &&
                !this.getTripPurpose().equals(Purpose.AIRPORT);
    }

    @Override
    public String toString() {
        return "Trip [id: " + this.tripId + " purpose: " + this.tripPurpose + "]";
    }

    @Override
    public int hashCode() {
        return tripId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MitoTripImpl) {
            return tripId == ((MitoTripImpl) o).tripId;
        } else {
            return false;
        }
    }
}
