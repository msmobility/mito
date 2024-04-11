package de.tum.bgu.msm.data;

import org.matsim.api.core.v01.population.Person;

public interface MitoTrip extends Id{
    
    int getId();
    
    Location getTripOrigin();

    
    void setTripOrigin(Location origin);

    
    Purpose getTripPurpose();

    
    Location getTripDestination();

    
    void setTripDestination(Location destination);

    
    MitoPerson getPerson();

    
    void setPerson(MitoPerson person);

    
    Mode getTripMode();

    
    void setTripMode(Mode tripMode);

    
    void setDepartureInMinutes(int departureInMinutes);

    
    void setDepartureInMinutesReturnTrip(int departureInMinutesReturnTrip);

    
    int getDepartureInMinutes();

    
    int getDepartureInMinutesReturnTrip();

    
    int getTripId();

    
    Person getMatsimPerson();

    
    void setMatsimPerson(Person matsimPerson);

    
    boolean isHomeBased();
}
