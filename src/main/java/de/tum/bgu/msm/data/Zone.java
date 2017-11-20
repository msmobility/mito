package de.tum.bgu.msm.data;

import de.tum.bgu.msm.data.areaTypesForModeChoice.AreaTypeHBWModeChoice;
import de.tum.bgu.msm.data.areaTypesForModeChoice.AreaTypeNHBOModeChoice;
import org.apache.log4j.Logger;

import java.util.EnumMap;

/**
 * Created by Nico on 7/7/2017.
 */
public class Zone {

    private static final Logger logger = Logger.getLogger(Zone.class);

    private final int zoneId;
    private final AreaType areaType;
    private float size;
    private float reductionAtBorderDamper = 0;
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

    private final EnumMap<Purpose, Double> tripAttractionRates = new EnumMap<>(Purpose.class);

    private AreaTypeHBWModeChoice areaTypeHBWModeChoice;
    private AreaTypeNHBOModeChoice areaTypeNHBOmodeChoice;

    private float distanceToNearestTransitStop;

    public Zone(int id, float size, AreaType areaType){
        this.zoneId = id;
        this.size = size;
        this.areaType = areaType;
    }

    //public Map<String, String> getAreaTypesForModeChoice() {return Collections.unmodifiableMap(areaTypesForModeChoice);}

    public AreaTypeHBWModeChoice getAreaTypeHBWModeChoice() {return areaTypeHBWModeChoice;}

    public void setAreaTypeHBWModeChoice(AreaTypeHBWModeChoice areaTypeHBWModeChoice){this.areaTypeHBWModeChoice = areaTypeHBWModeChoice;}

    public AreaTypeNHBOModeChoice getAreaTypeNHBOmodeChoice() {return areaTypeNHBOmodeChoice;}

    public void setAreaTypeNHBOmodeChoice(AreaTypeNHBOModeChoice areaTypeNHBOmodeChoice){this.areaTypeNHBOmodeChoice = areaTypeNHBOmodeChoice;}

    public float getDistanceToNearestTransitStop() {return distanceToNearestTransitStop;}

    public void setDistanceToNearestTransitStop(float distanceToNearestTransitStop) {this.distanceToNearestTransitStop = distanceToNearestTransitStop;}

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

    public AreaType getAreaType() {
        return areaType;
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

    public int getIndustrialEmpl() {
        return industrialEmpl;
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

    public void setTripAttractionRate(Purpose purpose, double tripAttractionRate) {
        this.tripAttractionRates.put(purpose, tripAttractionRate);
    }

    public double getTripAttractionRate(Purpose purpose) {
        Double rate = this.tripAttractionRates.get(purpose);
        if(rate == null)  {
            logger.error("No trip attraction rate set for zone " + zoneId + ". Please make sure to only call " +
                    "this method after trip generation module!");
        }
        return rate;
    }

    @Override
    public String toString() {
        return "[Zone " + zoneId + "]";
    }
}
