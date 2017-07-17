package de.tum.bgu.msm.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 7/7/2017.
 */
public class Zone {

    private final int zoneId;
    private float size;
    private float reductionAtBorderDamper = 0;
    private int region = -1;
    private int numberOfHouseholds = 0;
    private int schoolEnrollment = 0;

    private int industrialEmpl = 0;
    private int retailEmpl = 0;
    private int officeEmpl = 0;
    private int otherEmpl = 0;
    private int totalEmpl = 0;

    private float autoAccessibilityHouseholds = 0;
    private float autoAccessibilityRetail = 0;
    private float autoAccessibilityOther = 0;
    private float transitAccessibilityOther = 0;

    private Map<String, Float> attractionByPurpose = new HashMap<>();


    public Zone(int zoneId){
        this.zoneId = zoneId;
    }

    public Zone(int zoneId, float size) {
        this.zoneId = zoneId;
        this.size = size;
    }

    public int getZoneId() {
        return this.zoneId;
    }

    public float getSize() {
        return this.size;
    }


    public float getReductionAtBorderDamper() {
        return reductionAtBorderDamper;
    }

    public void setReductionAtBorderDamper(float damper) {
        this.reductionAtBorderDamper = damper;
    }


    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }


    public int getNumberOfHouseholds() {
        return this.numberOfHouseholds;
    }

    public void setNumberOfHouseholds(int number) {
        this.numberOfHouseholds = number;
    }

    public void addHousehold() {
        this.numberOfHouseholds++;
    }


    public int getSchoolEnrollment() {
        return schoolEnrollment;
    }

    public void setSchoolEnrollment(int schoolEnrollment) {
        this.schoolEnrollment = schoolEnrollment;
    }


    public void setIndEmpl(int indEmpl) {
        this.industrialEmpl = indEmpl;
    }

    public void setRetailEmpl(int retailEmpl) {
        this.retailEmpl = retailEmpl;
    }

    public void setOfficeEmpl(int officerEmpl) {
        this.officeEmpl = officerEmpl;
    }

    public void setOtherEmpl(int otherEmpl) {
        this.otherEmpl = otherEmpl;
    }

    public void setTotalEmpl(int totalEmpl) {
        this.totalEmpl = totalEmpl;
    }


    public int getTotalEmpl() {
        return totalEmpl;
    }

    public int getRetailEmpl() {
        return retailEmpl;
    }

    public int getOfficeEmpl() {
        return officeEmpl;
    }

    public int getOtherEmpl() {
        return otherEmpl;
    }


    public float getAutoAccessibilityHouseholds() {
        return autoAccessibilityHouseholds;
    }

    public void setAutoAccessibilityHouseholds(float autoAccessibilityHouseholds) {
        this.autoAccessibilityHouseholds = autoAccessibilityHouseholds;
    }


    public float getAutoAccessibilityRetail() {
        return autoAccessibilityRetail;
    }

    public void setAutoAccessibilityRetail(float autoAcessibilityRetail) {
        this.autoAccessibilityRetail = autoAcessibilityRetail;
    }


    public float getAutoAccessibilityOther() {
        return autoAccessibilityOther;
    }

    public void setAutoAccessibilityOther(float autoAcessibilityOther) {
        this.autoAccessibilityOther = autoAcessibilityOther;
    }


    public float getTransitAccessibilityOther() {
        return transitAccessibilityOther;
    }

    public void setTransitAccessibilityOther(float transitAcessibilityOther) {
        this.transitAccessibilityOther = transitAcessibilityOther;
    }
}
