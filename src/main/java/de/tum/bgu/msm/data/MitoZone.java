package de.tum.bgu.msm.data;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;

import java.util.EnumMap;

/**
 * Created by Nico on 7/7/2017.
 */
public class MitoZone implements Id{

    private final int zoneId;
    private final float size;
    private float reductionAtBorderDamper = 0;
    private int numberOfHouseholds = 0;
    private int schoolEnrollment = 0;

    private final EnumMap<Purpose, Double> tripAttraction = new EnumMap<>(Purpose.class);
    private final Multiset<JobType> employeesByType = HashMultiset.create();

    private final AreaTypes.SGType areaTypeSG;
    private AreaTypes.RType areaTypeR;

    private float distanceToNearestRailStop;

    public MitoZone(int id, float size, AreaTypes.SGType areaType) {
        this.zoneId = id;
        this.size = size;
        this.areaTypeSG = areaType;
    }

    public AreaTypes.SGType getAreaTypeSG() {
        return areaTypeSG;
    }

    public AreaTypes.RType getAreaTypeR() {
        return areaTypeR;
    }

    public void setAreaTypeR(AreaTypes.RType areaTypeR) {
        this.areaTypeR = areaTypeR;
    }

    public float getDistanceToNearestRailStop() {return distanceToNearestRailStop;}

    /**
     * Sets distance to nearest rail stop
     * @param distanceToNearestRailStop distance in km
     */
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

    public void setTripAttraction(Purpose purpose, double tripAttractionRate) {
        this.tripAttraction.put(purpose, tripAttractionRate);
    }

    public double getTripAttraction(Purpose purpose) {
        Double rate = this.tripAttraction.get(purpose);
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
