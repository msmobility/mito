package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */
public class MitoPerson implements Id {

    private static final Logger logger = Logger.getLogger(MitoPerson.class);

    private final int id;
    private final MitoGender mitoGender;
    private final MitoOccupationStatus mitoOccupationStatus;
    private final MitoOccupation occupation;
    private final int age;
    private final boolean driversLicense;
    private Optional<Boolean> hasBicycle = Optional.empty();

    private Set<MitoTrip> trips = new LinkedHashSet<>();
    private LinkedList<Activity> actChain = new LinkedList<>();

    private MitoHousehold household;
    private Map<SocialNetworkType, ArrayList<Integer> > alterLists = new HashMap<>();

    public MitoPerson(int id, MitoOccupationStatus mitoOccupationStatus, MitoOccupation occupation, int age, MitoGender mitoGender, boolean driversLicense) {
        this.id = id;
        this.mitoOccupationStatus = mitoOccupationStatus;
        this.occupation = occupation;
        this.age = age;
        this.mitoGender = mitoGender;
        this.driversLicense = driversLicense;
    }

    public MitoOccupation getOccupation() {
        return occupation;
    }

    public MitoOccupationStatus getMitoOccupationStatus() {
        return mitoOccupationStatus;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public int getAge() {
        return age;
    }

    public MitoGender getMitoGender() {
        return mitoGender;
    }

    public boolean hasDriversLicense() {
        return driversLicense;
    }

    public Set<MitoTrip> getTrips() {
        return Collections.unmodifiableSet(this.trips);
    }

    public void addTrip(MitoTrip trip) {
        this.trips.add(trip);
        if(trip.getPerson() != this) {
            trip.setPerson(this);
        }
    }

    public void removeTripFromPerson(MitoTrip trip){
        trips.remove(trip);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Optional<Boolean> getHasBicycle() {
        if (!hasBicycle.isPresent()){
            throw new RuntimeException("The number of bicycles is needed but has not been set");
        }
        return hasBicycle;
    }

    public void setHasBicycle(boolean hasBicycle) {
        this.hasBicycle = Optional.of(hasBicycle);
    }

    public MitoHousehold getHousehold() {
        return household;
    }

    public void setHousehold(MitoHousehold household) {
        this.household = household;
    }

    public Map<SocialNetworkType, ArrayList<Integer>> getAlterLists() {
        return alterLists;
    }

    public void setAlterLists(Map<SocialNetworkType, ArrayList<Integer>> alterLists) {
        this.alterLists = alterLists;
    }


    public LinkedList<Activity> getActChain() {
        return actChain;
    }

    public void setActChain(LinkedList<Activity> actChain) {
        this.actChain = actChain;
    }

    public static class Activity implements Comparable<Activity>{

        Activity priorAct;
        Location location;
        int endTime;
        Mode mode;
        Activity afterAct;

        public Activity(Location location, int endTime, Mode mode){
            this.location=location;
            this.endTime=endTime;
        }

        public Activity getPriorAct() {
            return priorAct;
        }

        public void setPriorAct(Activity priorAct) {
            this.priorAct = priorAct;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public int getEndTime() {
            return endTime;
        }

        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public Activity getAfterAct() {
            return afterAct;
        }

        public void setAfterAct(Activity afterAct) {
            this.afterAct = afterAct;
        }


        public void setPriorAndAfter(Activity priorAct, Activity afterAct) {
            this.priorAct = priorAct;
            this.afterAct = afterAct;
        }


        @Override
        public int compareTo(Activity o) {
            if (this.endTime > o.endTime) {

                // if current object is greater,then return 1
                return 1;
            }
            else if (this.endTime < o.endTime) {

                // if current object is greater,then return -1
                return -1;
            }
            else {

                // if current object is equal to o,then return 0
                return 0;
            }
        }
    }
}
