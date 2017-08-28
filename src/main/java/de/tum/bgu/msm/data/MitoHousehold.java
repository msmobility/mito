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
    private int homeZone;

    private final EnumMap<Purpose, ArrayList<MitoTrip>> tripsByPurpose;
    private final EnumMap<Purpose, Double> travelTimeBudgetByPurpose;

    private final List<MitoPerson> persons;


    public MitoHousehold(int id, int income, int autos, int homeZone) {
        this.hhId = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
        this.tripsByPurpose = new EnumMap(Purpose.class);
        this.persons = new ArrayList<>();
        this.travelTimeBudgetByPurpose = new EnumMap(Purpose.class);
    }

    public List<MitoPerson> getPersons(){
        return persons;
    }

    public int getHhId() {
        return hhId;
    }

    public int getHhSize() {
        return persons.size();
    }

    public int getFemales() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getGender().equals(Gender.FEMALE)).count();
    }

    public int getChildren() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getAge() < 18).count();
    }

    public int getYoungAdults() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getAge() >=18 && mitoPerson.getAge() <= 25).count();
    }

    public int getRetirees() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getAge() >= 65).count();
    }

    public int getNumberOfWorkers() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getOccupation().equals(Occupation.WORKER)).count();
    }

    public int getStudents() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.getOccupation().equals(Occupation.STUDENT)).count();
    }

    public int getLicenseHolders() {
        return (int) persons.stream().filter(mitoPerson -> mitoPerson.hasDriversLicense()).count();
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

    public int getHomeZone() {
        return homeZone;
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

    public Map<Purpose, ArrayList<MitoTrip>> getTripsByPurpose() {
        return tripsByPurpose;
    }

    public void setTravelTimeBudgetByPurpose(Purpose purpose, double budget) {
        this.travelTimeBudgetByPurpose.put(purpose, budget);
    }

    public double getTravelTimeBudgetForPurpose(Purpose purpose) {
        return travelTimeBudgetByPurpose.get(purpose) == null ? 0. : travelTimeBudgetByPurpose.get(purpose) ;
    }
}
