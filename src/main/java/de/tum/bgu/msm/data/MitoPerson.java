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

    private Map<String, Double> additionalAttributes = new LinkedHashMap<>();

    private Map<String, String> additionalStringAttributes = new LinkedHashMap<>();

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

    public String hasDriversLicenseString() {
        String license = "p.noDriversLicense";
        if (driversLicense){
            license = "p.driversLicense";
        }
        return license;
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

    public Boolean hasEV(){
        return household.isHasEV();
    }

    public String personAgeGroupTripGen(){
        String ageGroup = "";
        if (age < 19) {
            ageGroup= "p.age_gr_1";
        } else if (age < 30) {
            ageGroup= "p.age_gr_2";
        } else if (age < 50) {
            ageGroup= "p.age_gr_3";
        } else if (age < 60) {
            ageGroup= "p.age_gr_4";
        } else if (age < 70) {
            ageGroup= "p.age_gr_5";
        } else {
            ageGroup= "p.age_gr_6";
        }
        return ageGroup;
    }

    public String personAgeGroupModeChoice(){
        String ageGroup = "";
        if (age < 18) {
            ageGroup= "p.age_0_to_17";
        } else if (age < 30) {
            ageGroup= "p.age_18_to_29";
        } else if (age < 40) {
            ageGroup= "p.age_30_to_39";
        } else if (age < 50) {
            ageGroup= "p.age_40_to_49";
        } else if (age < 60) {
            ageGroup= "p.age_50_to_59";
        } else {
            ageGroup= "p.age_above_60";
        }
        return ageGroup;

    }

    public int getSize5(){
        return household.getSize5();
    }

    public int getChildren(){
        return household.getChildren();
    }

    public int getAutos3(){
        return household.getAutos3();
    }

    public int getBikes1(){
        return household.getBikes1();
    }

    public int getEconomicStatus(){
        return household.getEconomicStatus();
    }

    public String getIncomeClass(){
        return household.getIncomeClass();
    }

    public int getBBSR(){
        return household.getBBSR();
    }

    public Double getCarsPerAdult(){
        return household.getCarsPerAdult();
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

    public String getIsMobile_HBW_car() {
        return additionalStringAttributes.get("p.isMobile_HBW_car");
    }

    public String getIsMobile_HBW_PT() {
        return additionalStringAttributes.get("p.isMobile_HBW_PT");
    }

    public String getIsMobile_HBW_cycle() {
        return additionalStringAttributes.get("p.isMobile_HBW_cycle");
    }

    public String getIsMobile_HBW_walk() {
        return additionalStringAttributes.get("p.isMobile_HBW_walk");
    }

    public Double getTTB_HBW_car() {
        return additionalAttributes.get("p.TTB_HBW_car");
    }

    public Double getTTB_HBW_PT() {
        return additionalAttributes.get("p.TTB_HBW_PT");
    }

    public Double getTTB_HBW_cycle() {
        return additionalAttributes.get("p.TTB_HBW_cycle");
    }

    public Double getTTB_HBW_walk() {
        return additionalAttributes.get("p.TTB_HBW_walk");
    }

    public String getIsMobile_HBE_car() {
        return additionalStringAttributes.get("p.isMobile_HBE_car");
    }

    public String getIsMobile_HBE_PT() {
        return additionalStringAttributes.get("p.isMobile_HBE_PT");
    }

    public String getIsMobile_HBE_cycle() {
        return additionalStringAttributes.get("p.isMobile_HBE_cycle");
    }

    public String getIsMobile_HBE_walk() {
        return additionalStringAttributes.get("p.isMobile_HBE_walk");
    }

    public Double getTTB_HBE_car() {
        return additionalAttributes.get("p.TTB_HBE_car");
    }

    public Double getTTB_HBE_PT() {
        return additionalAttributes.get("p.TTB_HBE_PT");
    }

    public Double getTTB_HBE_cycle() {
        return additionalAttributes.get("p.TTB_HBE_cycle");
    }

    public Double getTTB_HBE_walk() {
        return additionalAttributes.get("p.TTB_HBE_walk");
    }

    public Double getHBW_trips() {
        return additionalAttributes.get("p.HBW_trips");
    }

    public Double getHBE_trips() {
        return additionalAttributes.get("p.HBE_trips");
    }

    public Double getHBS_trips() {
        return additionalAttributes.get("p.HBS_trips");
    }

    public Double getHBO_trips() {
        return additionalAttributes.get("p.HBO_trips");
    }

    public Double getHBR_trips() {
        return additionalAttributes.get("p.HBR_trips");
    }

    public Double getNHBW_trips() {
        return additionalAttributes.get("p.NHBW_trips");
    }

    public Double getNHBO_trips() {
        return additionalAttributes.get("p.NHBO_trips");
    }



    public void setAdditionalAttributes(Map<String, Double> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public Map<String, Double> getAdditionalAttributes(){
        return additionalAttributes;
    }

    public void setAdditionalStringAttributes(Map<String, String> additionalAttributes) {
        this.additionalStringAttributes = additionalAttributes;
    }

    public Map<String, String> getAdditionalStringAttributes(){
        return additionalStringAttributes;
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
