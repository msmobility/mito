package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

/**
 * Holds household objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 21, 2016 in Munich, Germany
 *
 */

public class MitoHousehold implements Id, MicroLocation {

    private static final Logger logger = Logger.getLogger(MitoHousehold.class);

    private final int hhId;
    private int monthlyIncome_EUR;
    private int economicStatus;
    private final int autos;
    private MitoZone homeZone;
    private Coordinate homeLocation;

    private final EnumMap<Purpose, List<MitoTrip>> tripsByPurpose = new EnumMap<>(Purpose.class);
    private final EnumMap<Purpose, Double> travelTimeBudgetByPurpose= new EnumMap<>(Purpose.class);

    private final Map<Integer, MitoPerson> persons  = new HashMap<>();

    public MitoHousehold(int id, int monthlyIncome_EUR, int autos) {
        this.hhId = id;
        this.monthlyIncome_EUR = monthlyIncome_EUR;
        this.autos = autos;
    }

    @Override
    public int getId() {
        return hhId;
    }

    public int getHhSize() {
        return persons.size();
    }

    public int getMonthlyIncome_EUR() {
        return monthlyIncome_EUR;
    }

    public void addIncome(double inc) {
        monthlyIncome_EUR += inc;
    }

    public int getAutos() {
        return autos;
    }

    public void setHomeZone(MitoZone homeZone) {
        this.homeZone = homeZone;
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

    public int getEconomicStatus() {
        return economicStatus;
    }

    public void setEconomicStatus(int economicStatus) {
        this.economicStatus = economicStatus;
    }

    public Coordinate getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Coordinate homeLocation) {
        this.homeLocation = homeLocation;
    }

    @Override
    public Coordinate getCoordinate() {
        return homeLocation;
    }

    @Override
    public int getZoneId() {
        return homeZone.getId();
    }

    @Override
    public int hashCode() {
        return hhId;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MitoHousehold) {
            return hhId == ((MitoHousehold) o).getId();
        } else {
            return false;
        }
    }
}