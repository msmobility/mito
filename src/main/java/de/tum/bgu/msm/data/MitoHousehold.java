package de.tum.bgu.msm.data;

import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * Holds household objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 21, 2016 in Munich, Germany
 *
 */

public class MitoHousehold implements Serializable {

    private final int hhId;
    private int income;
    private int autos;
    private Zone homeZone;

    private final EnumMap<Purpose, ArrayList<MitoTrip>> tripsByPurpose;
    private final EnumMap<Purpose, Double> travelTimeBudgetByPurpose;

    private final List<MitoPerson> persons;


    public MitoHousehold(int id, int income, int autos, Zone homeZone) {
        this.hhId = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
        this.tripsByPurpose = new EnumMap(Purpose.class);
        this.persons = new ArrayList<>();
        this.travelTimeBudgetByPurpose = new EnumMap(Purpose.class);
    }

    public int getHhId() {
        return hhId;
    }

    public int getHhSize() {
        return persons.size();
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int inc) {
        income = inc;
    }

    public int getAutos() {
        return autos;
    }

    public Zone getHomeZone() {
        return homeZone;
    }

    public List<MitoPerson> getPersons(){
        return Collections.unmodifiableList(persons);
    }

    public void addPerson(MitoPerson person) {
        this.persons.add(person);
    }

    public void removePerson(MitoPerson person) {
        this.persons.remove(person);
    }


    public void addTrip(MitoTrip trip) {
        if(tripsByPurpose.containsKey(trip.getTripPurpose())) {
            tripsByPurpose.get(trip.getTripPurpose()).add(trip);
        } else {
            ArrayList<MitoTrip> trips = new ArrayList<>();
            trips.add(trip);
            tripsByPurpose.put(trip.getTripPurpose(), trips);
        }
    }

    public void removeTrip(MitoTrip trip) {
        if(trip != null) {
            if(tripsByPurpose.containsKey(trip.getTripPurpose())) {
                tripsByPurpose.get(trip.getTripPurpose()).remove(trip);
            }
        }
    }

    public List<MitoTrip> getTripsForPurpose(Purpose purpose) {
        if(tripsByPurpose.get(purpose) != null) {
            return Collections.unmodifiableList(tripsByPurpose.get(purpose));
        } else {
            return Collections.unmodifiableList(Collections.emptyList());
        }
    }

    public void setTravelTimeBudgetByPurpose(Purpose purpose, double budget) {
        this.travelTimeBudgetByPurpose.put(purpose, budget);
    }

    public double getTravelTimeBudgetForPurpose(Purpose purpose) {
        return travelTimeBudgetByPurpose.get(purpose) == null ? 0. : travelTimeBudgetByPurpose.get(purpose) ;
    }
}