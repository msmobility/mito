package de.tum.bgu.msm.modules.tripGeneration;

import com.google.common.collect.Multiset;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.data.jobTypes.JobType;
import org.apache.log4j.Logger;

public class AttractionCalculator {

    public enum ExplanatoryVariable {HH, TOT, RE, OFF, OTH, ENR}

    private static final Logger logger = Logger.getLogger(AttractionCalculator.class);

    private final DataSet dataSet;

    public AttractionCalculator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {
        logger.info("  Calculating trip attractions");
        for (Zone zone : dataSet.getZones().values()) {
            int retail = getEmployeesByCategory(zone, Category.RETAIL);
            int office = getEmployeesByCategory(zone, Category.OFFICE);
            int other = getEmployeesByCategory(zone, Category.OTHER);
            for (Purpose purpose : Purpose.values()) {
                float tripAttraction = 0;
                for (ExplanatoryVariable variable : ExplanatoryVariable.values()) {
                    float attribute;
                    switch (variable) {
                        case HH:
                            attribute = zone.getNumberOfHouseholds();
                            break;
                        case TOT:
                            attribute = zone.getTotalEmpl();
                            break;
                        case RE:
                            attribute = retail;
                            break;
                        case OFF:
                            attribute = office;
                            break;
                        case OTH:
                            attribute = other;
                            break;
                        case ENR:
                            attribute = zone.getSchoolEnrollment();
                            break;
                        default:
                            throw new RuntimeException("Unknown trip attraction Variable.");
                    }
                    Double rate = purpose.getTripAttractionForVariable(variable);
                    if(rate == null) {
                        throw new RuntimeException("Purpose " + purpose + " does not have an attraction" +
                                " rate for variable " + variable + " registered.");
                    }
                    tripAttraction += attribute * rate;
                }
                zone.setTripAttractionRate(purpose, tripAttraction);
            }
        }
    }

    private int getEmployeesByCategory(Zone zone, Category category) {
        int sum = 0;
        Multiset<JobType> jobTypes = zone.getEmployeesByType();
        for(JobType distinctType: jobTypes.elementSet()) {
            if(category == distinctType.getCategory()) {
                sum += jobTypes.count(distinctType);
            }
        }
        return sum;
    }
}
