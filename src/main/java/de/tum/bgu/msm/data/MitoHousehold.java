package de.tum.bgu.msm.data;

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

    private static final Logger logger = Logger.getLogger(MitoHousehold.class);

    private final int hhId;
    private int income;
    private int autos;
    private Zone homeZone;

    private final EnumMap<Purpose, ArrayList<MitoTrip>> tripsByPurpose = new EnumMap(Purpose.class);;
    private final EnumMap<Purpose, Double> travelTimeBudgetByPurpose= new EnumMap(Purpose.class);

    private final Map<Integer, MitoPerson> persons  = new HashMap<>();


    public MitoHousehold(int id, int income, int autos, Zone homeZone) {
        this.hhId = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
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

    public Map<Integer, MitoPerson> getPersons(){
        return Collections.unmodifiableMap(persons);
    }

    public void addPerson(MitoPerson person) {
        MitoPerson test = this.persons.get(person.getId());
        if(test!= null) {
            if(test.equals(person)) {
                logger.warn("Person " + person.getId() + " was already added to household " + this.getHhId());
            } else {
                throw new IllegalArgumentException("Person id " + person.getId() + " already exists in household " + this.getHhId());
            }
        }
        this.persons.put(person.getId(), person);
    }

    public void removePerson(Integer personId) {
        this.persons.remove(personId);
    }

    public synchronized void addTrip(MitoTrip trip) {
        if(tripsByPurpose.containsKey(trip.getTripPurpose())) {
            tripsByPurpose.get(trip.getTripPurpose()).add(trip);
        } else {
            ArrayList<MitoTrip> trips = new ArrayList<>();
            trips.add(trip);
            tripsByPurpose.put(trip.getTripPurpose(), trips);
        }
    }

    public synchronized void removeTrip(MitoTrip trip) {
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

    public synchronized void setTravelTimeBudgetByPurpose(Purpose purpose, double budget) {
        this.travelTimeBudgetByPurpose.put(purpose, budget);
    }

    public double getTravelTimeBudgetForPurpose(Purpose purpose) {
        return travelTimeBudgetByPurpose.get(purpose) == null ? 0. : travelTimeBudgetByPurpose.get(purpose) ;
    }
}