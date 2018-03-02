package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.munich.MunichJobType;
import de.tum.bgu.msm.util.js.JavaScriptCalculator;

import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    DestinationUtilityJSCalculator(Reader reader) {
        super(reader);
    }

    public Double calculateHbwUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBW", travelDistance, targetZone.getTotalEmpl());
    }

    public Double calculateHbeUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBE", travelDistance, targetZone.getSchoolEnrollment());
    }

    public Double calculateHbsUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBS", travelDistance, targetZone.getEmployeesByCategory(Category.RETAIL));
    }

    public Double calculateHboUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateHBO", travelDistance, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV));
    }

    public Double calculateNhbwUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateNHBW", travelDistance, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV),
                targetZone.getNumberOfEmployeesForType(MunichJobType.RETL));
    }

    public Double calculateNhboUtility(MitoZone targetZone, double travelDistance) {
        return super.calculate("calculateNHBO", travelDistance, targetZone.getNumberOfHouseholds(),
                targetZone.getNumberOfEmployeesForType(MunichJobType.ADMN), targetZone.getNumberOfEmployeesForType(MunichJobType.SERV),
                targetZone.getNumberOfEmployeesForType(MunichJobType.RETL));
    }
}
