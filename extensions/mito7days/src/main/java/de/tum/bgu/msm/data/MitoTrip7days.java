package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;

/**
 * Holds trip objects for the Microsimulation Transport Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Mar 26, 2017 in Munich, Germany
 */
public class MitoTrip7days implements MitoTrip {

    private final MitoTrip delegate;
    private static final Logger logger = Logger.getLogger(MitoTrip7days.class);
    private Day departureDay;

    public MitoTrip7days(MitoTrip delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public Location getTripOrigin() {
        return delegate.getTripOrigin();
    }

    @Override
    public void setTripOrigin(Location origin) {
        delegate.setTripOrigin(origin);
    }

    @Override
    public Purpose getTripPurpose() {
        return delegate.getTripPurpose();
    }

    @Override
    public Location getTripDestination() {
        return delegate.getTripDestination();
    }

    @Override
    public void setTripDestination(Location destination) {
        delegate.setTripDestination(destination);
    }

    @Override
    public MitoPerson getPerson() {
        return delegate.getPerson();
    }

    @Override
    public void setPerson(MitoPerson person) {
        delegate.setPerson(person);
    }

    @Override
    public Mode getTripMode() {
        return delegate.getTripMode();
    }

    @Override
    public void setTripMode(Mode tripMode) {
        delegate.setTripMode(tripMode);
    }

    @Override
    public void setDepartureInMinutes(int departureInMinutes) {
        delegate.setDepartureInMinutes(departureInMinutes);
    }

    @Override
    public void setDepartureInMinutesReturnTrip(int departureInMinutesReturnTrip) {
        delegate.setDepartureInMinutesReturnTrip(departureInMinutesReturnTrip);
    }

    @Override
    public int getDepartureInMinutes() {
        return delegate.getDepartureInMinutes();
    }

    @Override
    public int getDepartureInMinutesReturnTrip() {
        return delegate.getDepartureInMinutesReturnTrip();
    }

    @Override
    public int getTripId() {
        return delegate.getTripId();
    }

    @Override
    public Person getMatsimPerson() {
        return delegate.getMatsimPerson();
    }

    @Override
    public void setMatsimPerson(Person matsimPerson) {
        delegate.setMatsimPerson(matsimPerson);
    }

    @Override
    public boolean isHomeBased() {
        return delegate.isHomeBased();
    }

    public Day getDepartureDay() {
        return departureDay;
    }

    public void setDepartureDay(Day departureDay) {
        this.departureDay = departureDay;
    }
}
