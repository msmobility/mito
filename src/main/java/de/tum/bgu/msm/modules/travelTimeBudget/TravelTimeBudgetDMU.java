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

public final class TravelTimeBudgetDMU extends DMU<MitoHousehold> {

    private MitoHousehold household;

    // DMU methods - define one of these for every @var in the control file.
    public int getHouseholdSize() {
        return household.getHhSize();
    }

    public int getFemales() {
        return MitoUtil.getFemalesForHousehold(household);
    }

    public int getChildren() {
        return MitoUtil.getChildrenForHousehold(household);
    }

    public int getYoungAdults() {
        return MitoUtil.getYoungAdultsForHousehold(household);
    }

    public int getRetirees() {
        return MitoUtil.getRetireesForHousehold(household);
    }

    public int getStudents() {
        return MitoUtil.getStudentsForHousehold(household);
    }

    public int getWorkers() {
        return MitoUtil.getNumberOfWorkersForHousehold(household);
    }

    public int getIncome() {
        return household.getIncome();
    }

    public int getCars() {
        return household.getAutos();
    }

    public int getLicenseHolders() {
        return MitoUtil.getLicenseHoldersForHousehold(household);
    }

    public int getAreaType() {
        return household.getHomeZone().getRegion();
    }

    public int getHbwTrips() {
        return household.getTripsForPurpose(Purpose.HBW).size();
    }

    public int getHbsTrips() {
        return household.getTripsForPurpose(Purpose.HBS).size();
    }

    public int getHboTrips() {
        return household.getTripsForPurpose(Purpose.HBO).size();
    }

    public int getHbeTrips() {
        return household.getTripsForPurpose(Purpose.HBE).size();
    }

    public int getNhbwTrips() {
        return household.getTripsForPurpose(Purpose.NHBW).size();
    }

    public int getNhboTrips() {
        return household.getTripsForPurpose(Purpose.NHBO).size();
    }

    TravelTimeBudgetDMU() {

    }

    @Override
    public void updateDMU(MitoHousehold hh) {
        this.household = hh;
    }

    @Override
    public String toString() {
        return "Household " + household.getHhId() + " with " + household.getHhSize() + " persons living in area type " +
                household.getHomeZone().getRegion();
    }
}