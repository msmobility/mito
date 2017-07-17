package de.tum.bgu.msm.modules;

import com.pb.common.calculator.IndexValues;
import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

/**
 * Calculates travel time budget
 * Author: Rolf Moeckel, Technical University of Munich
 * Created on 2 April 2017 in train between Stuttgart and Ulm
 **/


public class TravelTimeBudgetDMU {

    protected transient Logger logger = Logger.getLogger(TravelTimeBudgetDMU.class);

    // uec variables
    private IndexValues dmuIndex;
    private int householdSize;
    private int females;
    private int children;
    private int youngAdults;
    private int retirees;
    private int workers;
    private int students;
    private int cars;
    private int licenseHolders;
    private int income;
    private int areaType;
    private int hbwTrips;
    private int hbsTrips;
    private int hboTrips;
    private int hbeTrips;
    private int nhbwTrips;
    private int nhboTrips;



    public TravelTimeBudgetDMU() {
        dmuIndex = new IndexValues();
    }

    public IndexValues getDmuIndexValues() {
        return dmuIndex;
    }

    public void setHouseholdSize (int householdSize) {
        this.householdSize = householdSize;
    }

    public void setWorkers (int workers) {
        this.workers = workers;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public void setFemales(int females) {
        this.females = females;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public void setYoungAdults(int youngAdults) {
        this.youngAdults = youngAdults;
    }

    public void setRetirees(int retirees) {
        this.retirees = retirees;
    }

    public void setStudents(int students) {
        this.students = students;
    }

    public void setCars(int cars) {
        this.cars = cars;
    }

    public void setLicenseHolders(int licenseHolders) {
        this.licenseHolders = licenseHolders;
    }

    public void setAreaType(int areaType) {
        this.areaType = areaType;
    }

    public void setTrips(int[] tripCounter, DataSet dataSet) {
        this.hbwTrips = tripCounter[dataSet.getPurposeIndex("HBW")];
        this.hbsTrips = tripCounter[dataSet.getPurposeIndex("HBS")];
        this.hboTrips = tripCounter[dataSet.getPurposeIndex("HBO")];
        this.hbeTrips = tripCounter[dataSet.getPurposeIndex("HBE")];
        this.nhbwTrips = tripCounter[dataSet.getPurposeIndex("NHBW")];
        this.nhboTrips = tripCounter[dataSet.getPurposeIndex("NHBO")];
    }

    public void setHbsTrips(int hbsTrips) {
        this.hbsTrips = hbsTrips;
    }

    public void setHboTrips(int hboTrips) {
        this.hboTrips = hboTrips;
    }

    public void setHbeTrips(int hbeTrips) {
        this.hbeTrips = hbeTrips;
    }

    public void setNhbwTrips(int nhbwTrips) {
        this.nhbwTrips = nhbwTrips;
    }

    public void setNhboTrips(int nhboTrips) {
        this.nhboTrips = nhboTrips;
    }


    // DMU methods - define one of these for every @var in the control file.
    public int getHouseholdSize() {
        return householdSize;
    }

    public int getFemales() {
        return females;
    }

    public int getChildren() {
        return children;
    }

    public int getYoungAdults() {
        return youngAdults;
    }

    public int getRetirees() {
        return retirees;
    }

    public int getStudents() {
        return students;
    }

    public int getWorkers() {
        return workers;
    }

    public int getIncome() {
        return income;
    }

    public int getCars() {
        return cars;
    }

    public int getLicenseHolders() {
        return licenseHolders;
    }

    public int getAreaType() {
        return areaType;
    }

    public int getHbwTrips() {
        return hbwTrips;
    }

    public int getHbsTrips() {
        return hbsTrips;
    }

    public int getHboTrips() {
        return hboTrips;
    }

    public int getHbeTrips() {
        return hbeTrips;
    }

    public int getNhbwTrips() {
        return nhbwTrips;
    }

    public int getNhboTrips() {
        return nhboTrips;
    }

}