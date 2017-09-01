package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.uec.DMU;

/**
 * Calculates travel time budget
 * Author: Rolf Moeckel, Technical University of Munich
 * Created on 2 April 2017 in train between Stuttgart and Ulm
 **/

public class TravelTimeBudgetDMU extends DMU<MitoHousehold> {

    // uec variables
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
    protected void setup(MitoHousehold hh) {
        this.householdSize = hh.getHhSize();
        this.females = MitoUtil.getFemalesForHousehold(hh);
        this.children = MitoUtil.getChildrenForHousehold(hh);
        this.youngAdults = MitoUtil.getYoungAdultsForHousehold(hh);
        this.retirees = MitoUtil.getRetireesForHousehold(hh);
        this.workers = MitoUtil.getNumberOfWorkersForHousehold(hh);
        this.students = MitoUtil.getStudentsForHousehold(hh);
        this.licenseHolders = MitoUtil.getLicenseHoldersForHousehold(hh);
        this.cars = hh.getAutos();
        this.income = hh.getIncome();
        this.areaType = hh.getHomeZone().getRegion();
        this.hbwTrips = hh.getTripsForPurpose(Purpose.HBW).size();
        this.hbsTrips = hh.getTripsForPurpose(Purpose.HBS).size();
        this.hboTrips = hh.getTripsForPurpose(Purpose.HBO).size();
        this.hbeTrips = hh.getTripsForPurpose(Purpose.HBE).size();
        this.nhbwTrips = hh.getTripsForPurpose(Purpose.NHBW).size();
        this.nhboTrips = hh.getTripsForPurpose(Purpose.NHBO).size();
    }
}