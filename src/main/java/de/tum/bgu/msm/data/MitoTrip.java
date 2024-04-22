package de.tum.bgu.msm.data;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Person;

/**
 * Holds trip objects for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Mar 26, 2017 in Munich, Germany
 */
public class MitoTrip implements Id {

    private final int tripId;
    private final Purpose tripPurpose;

    private Location tripOrigin;
    private Location tripDestination;

    private MitoPerson person;

    private Mode tripMode;

    private int departureInMinutes = -1;
    private int departureInMinutesReturnTrip = -1;
    private int arrivalInMinutes;
    private int actDuration;

    private Person matsimPerson;
    private int coordinatedTripId = 0;
    private Coord destinationCoord;
    private Coord originCoord;

    public MitoTrip(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    @Override
    public int getId() {
        return tripId;
    }

    public Location getTripOrigin() {
        return tripOrigin;
    }

    public void setTripOrigin(Location origin) {
        this.tripOrigin = origin;
    }

    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    public Location getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(Location destination) {
        this.tripDestination = destination;
    }

    public MitoPerson getPerson() {
        return person;
    }

    public void setPerson(MitoPerson person) {
        this.person = person;
//        if (!person.getTrips().contains(this)) {
//            person.addTrip(this);
//        }
    }

    public Mode getTripMode() {
        return tripMode;
    }

    public void setTripMode(Mode tripMode) {
        this.tripMode = tripMode;
    }

    public void setDepartureInMinutes(int departureInMinutes) {
        this.departureInMinutes = departureInMinutes;
    }

    public void setDepartureInMinutesReturnTrip(int departureInMinutesReturnTrip) {
        this.departureInMinutesReturnTrip = departureInMinutesReturnTrip;
    }

    public int getDepartureInMinutes() {
        return departureInMinutes;
    }

    public int getDepartureInMinutesReturnTrip() {
        return departureInMinutesReturnTrip;
    }

    public int getTripId() {
        return tripId;
    }

    public Person getMatsimPerson() {
        return matsimPerson;
    }

    public void setMatsimPerson(Person matsimPerson) {
        this.matsimPerson = matsimPerson;
    }

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
        if (o instanceof MitoTrip) {
            return tripId == ((MitoTrip) o).tripId;
        } else {
            return false;
        }
    }

    public int getCoordinatedTripId() {
        return coordinatedTripId;
    }

    public void setCoordinatedTripId(int coordinatedTripId) {
        this.coordinatedTripId = coordinatedTripId;
    }

    public int getArrivalInMinutes() {
        return arrivalInMinutes;
    }

    public void setArrivalInMinutes(int arrivalInMinutes) {
        this.arrivalInMinutes = arrivalInMinutes;
    }

    public void setDestinationCoord(Coord destinationCoord) {
        this.destinationCoord = destinationCoord;
    }

    public Coord getDestinationCoord() {
        return destinationCoord;
    }

    public Coord getOriginCoord() {
        return originCoord;
    }

    public void setOriginCoord(Coord originCoord) {
        this.originCoord = originCoord;
    }

    public int getActDuration() {
        return actDuration;
    }

    public void setActDuration(int actDuration) {
        this.actDuration = actDuration;
    }
}
