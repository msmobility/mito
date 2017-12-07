package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobType;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    protected DestinationUtilityJSCalculator(Reader reader) {
        super(reader);
    }

    public Double calculateHbwUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateHBW", travelTime, targetZone.getTotalEmpl());
    }

    public Double calculateHbeUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateHBE", travelTime, targetZone.getSchoolEnrollment());
    }

    public Double calculateHbsUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateHBS", travelTime, targetZone.getEmployeesByCategory(Category.RETAIL));
    }

    public Double calculateHboUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateHBO", travelTime, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV));
    }

    public Double calculateNhbwUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateNHBW", travelTime, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV),
                targetZone.getNumberOfEmployeesForType(MunichJobType.RETL));
    }

    public Double calculateNhboUtility(Zone targetZone, double travelTime) {
        return super.calculate("calculateNHBW", travelTime, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV),
                targetZone.getNumberOfEmployeesForType(MunichJobType.RETL));
    }
}
