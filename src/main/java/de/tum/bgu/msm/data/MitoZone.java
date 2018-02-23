package de.tum.bgu.msm.data;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;
import org.apache.log4j.Logger;

import java.util.EnumMap;

/**
 * Created by Nico on 7/7/2017.
 */
public class MitoZone implements Id{

    private final int zoneId;
    private final AreaType areaType;
    private final float size;
    private float reductionAtBorderDamper = 0;
    private int numberOfHouseholds = 0;
    private int schoolEnrollment = 0;

    private final EnumMap<Purpose, Double> tripAttractionRates = new EnumMap<>(Purpose.class);
    private final Multiset<JobType> employeesByType = HashMultiset.create();

    private AreaTypeForModeChoice areaTypeHBWModeChoice;
    private AreaTypeForModeChoice areaTypeNHBOmodeChoice;

    private float distanceToNearestRailStop;

    public MitoZone(int id, float size, AreaType areaType) {
        this.zoneId = id;
        this.size = size;
        this.areaType = areaType;
    }

    public AreaTypeForModeChoice getAreaTypeHBWModeChoice() {return areaTypeHBWModeChoice;}

    public void setAreaTypeHBWModeChoice(AreaTypeForModeChoice areaTypeHBWModeChoice){this.areaTypeHBWModeChoice = areaTypeHBWModeChoice;}

    public AreaTypeForModeChoice getAreaTypeNHBOModeChoice() {return areaTypeNHBOmodeChoice;}

    public void setAreaTypeNHBOModeChoice(AreaTypeForModeChoice areaTypeNHBOModeChoice){this.areaTypeNHBOmodeChoice = areaTypeNHBOModeChoice;}

    public float getDistanceToNearestRailStop() {return distanceToNearestRailStop;}

    public void setDistanceToNearestRailStop(float distanceToNearestRailStop) {this.distanceToNearestRailStop = distanceToNearestRailStop;}

    @Override
    public int getId() {
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

    public void addHousehold() {
        this.numberOfHouseholds++;
    }

    public int getSchoolEnrollment() {
        return schoolEnrollment;
    }

    public void setSchoolEnrollment(int schoolEnrollment) {
        this.schoolEnrollment = schoolEnrollment;
    }

    public void addEmployeeForType(JobType type) {
        this.employeesByType.add(type);
    }

    public int getNumberOfEmployeesForType(JobType type) {
        return this.employeesByType.count(type);
    }

    public int getTotalEmpl() {
        return this.employeesByType.size();
    }

    public void setTripAttractionRate(Purpose purpose, double tripAttractionRate) {
        this.tripAttractionRates.put(purpose, tripAttractionRate);
    }

    public double getTripAttractionRate(Purpose purpose) {
        Double rate = this.tripAttractionRates.get(purpose);
        if (rate == null) {
            throw new RuntimeException("No trip attraction rate set for zone " + zoneId + ". Please make sure to only call " +
                    "this method after trip generation module!");
        }
        return rate;
    }

    public int getEmployeesByCategory(Category category) {
        int sum = 0;
        Multiset<JobType> jobTypes = employeesByType;
        for (JobType distinctType : jobTypes.elementSet()) {
            if (category == distinctType.getCategory()) {
                sum += jobTypes.count(distinctType);
            }
        }
        return sum;
    }

    @Override
    public String toString() {
        return "[MitoZone " + zoneId + "]";
    }
}
