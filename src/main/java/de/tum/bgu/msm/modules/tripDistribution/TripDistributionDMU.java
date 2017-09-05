package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.TravelTimes;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.util.uec.DMU;

public class TripDistributionDMU extends DMU<Zone> {

    private TravelTimes travelTimes;
    private double budget;

    private Zone originZone;
    private Zone zone;

    public int getTotalEmployees() {
        return zone.getTotalEmpl();
    }

    public int getRetailEmployees() {
        return zone.getRetailEmpl();
    }

    public int getOtherEmployees() {
        return zone.getOtherEmpl();
    }

    public int getSchoolEnrollment() {
        return zone.getSchoolEnrollment();
    }

    public double getTravelTime() {
        return travelTimes.getTravelTimeFromTo(originZone, zone);
    }

    public double getBudgetOffset() {
        return Math.abs(travelTimes.getTravelTimeFromTo(originZone, zone) - budget);
    }

    public void setOriginZone(Zone zone) {
        this.originZone = zone;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public TripDistributionDMU(TravelTimes travelTimes) {
        this.travelTimes = travelTimes;
    }

    @Override
    public void updateDMU(Zone object) {
        this.zone = zone;
    }
}
