package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobType;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class TripDistributionJSCalculator extends JavaScriptCalculator<Double> {

    protected TripDistributionJSCalculator(Reader reader) {
        super(reader);
    }

    public Double calculateUtility(Zone targetZone, double travelTime, Purpose purpose) {
        return super.calculate("calculate",
                purpose.name(),
                travelTime,
                targetZone.getZoneId(),
                targetZone.getTotalEmpl(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.RETL),
                targetZone.getNumberOfEmployeesForType(MunichJobType.AGRI),
                targetZone.getSchoolEnrollment(),
                targetZone.getNumberOfHouseholds());
    }
}
