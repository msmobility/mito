package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Holds household objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 21, 2016 in Munich, Germany
 *
 */

public class MitoHousehold implements Id {

    private static final Logger logger = Logger.getLogger(MitoHousehold.class);

    private final int hhId;
    private int income;
    private final int autos;
    private final MitoZone homeZone;

    private final EnumMap<Purpose, List<MitoTrip>> tripsByPurpose = new EnumMap<>(Purpose.class);
    private final EnumMap<Purpose, Double> travelTimeBudgetByPurpose= new EnumMap<>(Purpose.class);

    private final Map<Integer, MitoPerson> persons  = new HashMap<>();

    public MitoHousehold(int id, int income, int autos, MitoZone homeZone) {
        this.hhId = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
    }

    @Override
    public int getId() {
        return hhId;
    }

    public int getHhSize() {
        return persons.size();
    }

    public int getIncome() {
        return income;
    }

    public void addIncome(int inc) {
        income += inc;
    }

    public int getAutos() {
        return autos;
    }

    public MitoZone getHomeZone() {
        return homeZone;
    }

    public Map<Integer, MitoPerson> getPersons(){
        return Collections.unmodifiableMap(persons);
    }

    public void addPerson(MitoPerson person) {
        MitoPerson test = this.persons.get(person.getId());
        if(test!= null) {
            if(test.equals(person)) {
                logger.warn("Person " + person.getId() + " was already added to household " + this.getId());
            } else {
                throw new IllegalArgumentException("Person id " + person.getId() + " already exists in household " + this.getId());
            }
        }
        this.persons.put(person.getId(), person);
    }

    public synchronized void setTripsByPurpose(List<MitoTrip> trips, Purpose purpose) {
            tripsByPurpose.put(purpose, trips);
    }

    public List<MitoTrip> getTripsForPurpose(Purpose purpose) {
        if(tripsByPurpose.get(purpose) != null) {
            return tripsByPurpose.get(purpose);
        } else {
            return Collections.emptyList();
        }
    }

    public synchronized void setTravelTimeBudgetByPurpose(Purpose purpose, double budget) {
        this.travelTimeBudgetByPurpose.put(purpose, budget);
    }

    public double getTravelTimeBudgetForPurpose(Purpose purpose) {
        return travelTimeBudgetByPurpose.get(purpose) == null ? 0. : travelTimeBudgetByPurpose.get(purpose) ;
    }
}