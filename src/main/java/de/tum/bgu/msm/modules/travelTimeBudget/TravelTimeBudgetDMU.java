package de.tum.bgu.msm.modules.travelTimeBudget;

import com.pb.common.calculator2.IndexValues;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates travel time budget
 * Author: Rolf Moeckel, Technical University of Munich
 * Created on 2 April 2017 in train between Stuttgart and Ulm
 **/


public class TravelTimeBudgetDMU implements com.pb.common.calculator2.VariableTable {

    protected transient Logger logger = Logger.getLogger(TravelTimeBudgetDMU.class);

    // uec variables
    private final IndexValues dmuIndex;
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

    private Map<Integer, String> fieldByIndex;



    public TravelTimeBudgetDMU() {
        dmuIndex = new IndexValues();
        fieldByIndex = new HashMap<>();
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

    @Override
    public int getIndexValue(String variableName) {
        fieldByIndex.put(fieldByIndex.size(), variableName);
        return fieldByIndex.size()-1;
    }

    @Override
    public int getAssignmentIndexValue(String variableName) {
        return 0;
    }

    @Override
    public double getValueForIndex(int variableIndex) {
        return getValueForIndex(0, 0);
    }

    @Override
    public double getValueForIndex(int variableIndex, int arrayIndex) {
        String field = fieldByIndex.get(variableIndex);
        try {
            Method method = this.getClass().getMethod(field);
            return Double.parseDouble(String.valueOf(method.invoke(this, null)));
        } catch (NoSuchMethodException e) {
            logger.error("Could not find defined field in DMU class");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("Error in method loading");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("Error in method loading");
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void setValue(String variableName, double variableValue) {

    }

    @Override
    public void setValue(int variableIndex, double variableValue) {

    }
}