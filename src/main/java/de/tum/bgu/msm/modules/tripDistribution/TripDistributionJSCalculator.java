package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class TripDistributionJSCalculator extends JavaScriptCalculator<Double> {

    private Zone baseZone;

    protected TripDistributionJSCalculator(Reader reader) {
        super(reader);
    }

    public void setBaseZone(Zone zone) {
        this.baseZone = zone;
        this.bindings.put("baseZone", zone.getZoneId());
    }

    public void setPurpose(Purpose purpose) {
        bindings.put("purpose", purpose.name());
    }

    public void setTargetZone(Zone zone, double travelTime) {
        bindings.put("targetZone", zone.getZoneId());
        bindings.put("travelTime", travelTime);
        bindings.put("totalEmployees", zone.getTotalEmpl());
        bindings.put("retailEmployees", zone.getRetailEmpl());
        bindings.put("otherEmployees", zone.getOtherEmpl());
        bindings.put("schoolEnrollment", zone.getSchoolEnrollment());
        bindings.put("households", zone.getNumberOfHouseholds());
    }
}
