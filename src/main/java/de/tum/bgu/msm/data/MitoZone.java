package de.tum.bgu.msm.data;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;
import de.tum.bgu.msm.util.SeededRandomPointsBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.core.utils.geometry.geotools.MGC;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Nico on 7/7/2017.
 */
public class MitoZone implements Id, Location {

    private final int zoneId;

    /**
     * Gemeindeschluessel?
     */
    private int ags = -1;

    private float reductionAtBorderDamper = 0;
    private int numberOfHouseholds = 0;
    private int schoolEnrollment = 0;

    private final EnumMap<Purpose, Double> tripAttraction = new EnumMap<>(Purpose.class);
    private final Multiset<JobType> employeesByType = HashMultiset.create();

    private final AreaTypes.SGType areaTypeSG;
    private AreaTypes.RType areaTypeR;

    private float distanceToNearestRailStop;
    private Geometry geometry;

    public void setOpenDataExplanatoryVariables(Map<String, Double> openDataExplanatoryVariables) {
        this.openDataExplanatoryVariables = openDataExplanatoryVariables;
    }

    private Map<String, Double> openDataExplanatoryVariables = new HashMap<>();

    public Map<String, Double> getOpenDataExplanatoryVariables() {
        return openDataExplanatoryVariables;
    }

    public MitoZone(int id, AreaTypes.SGType areaType) {
        this.zoneId = id;
        this.areaTypeSG = areaType;
    }

    //TODO: is this supposed to stay in master?
    //TODO: this decision should be handled by some other class that takes zone candidates as an argument
    public boolean isMunichZone() {
         return 9162000 == ags;
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

    public float getDistanceToNearestRailStop() {
        return distanceToNearestRailStop;
    }

    /**
     * Sets distance to nearest rail stop
     *
     * @param distanceToNearestRailStop distance in km
     */
    public void setDistanceToNearestRailStop(float distanceToNearestRailStop) {
        this.distanceToNearestRailStop = distanceToNearestRailStop;
    }

    @Override
    public int getId() {
        return this.zoneId;
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

    public void addSchoolEnrollment(int schoolEnrollment) {
        this.schoolEnrollment += schoolEnrollment;
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

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }

    public void setAGS(int ags) {
        this.ags = ags;
    }

    public Coordinate getRandomCoord(Random random) {
        SeededRandomPointsBuilder randomPointsBuilder = new SeededRandomPointsBuilder(new GeometryFactory(), random);
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent(geometry);
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        Point p = MGC.coordinate2Point(coordinate);
        return new Coordinate(p.getX(), p.getY());
    }

    @Override
    public int hashCode() {
        return zoneId;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MitoZone) {
            return zoneId == ((MitoZone) o).getId();
        } else {
            return false;
        }
    }

    @Override
    public int getZoneId() {
        return zoneId;
    }
}
