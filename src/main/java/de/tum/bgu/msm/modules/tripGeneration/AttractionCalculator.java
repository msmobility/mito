package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.jobTypes.Category;
import org.apache.log4j.Logger;

import java.util.List;

public class AttractionCalculator {

    private final List<Purpose> purposes;

    public enum ExplanatoryVariable {
        /**
         * #Households
         */
        HH,
        /**
         * Total employment
         */
        TOT,
        /**
         * Retail employment
         */
        RE,
        /**
         * Office employment
         */
        OFF,
        /**
         * Other employment
         */
        OTH,
        /**
         * School enrollment
         */
        ENR
    }

    private static final Logger logger = Logger.getLogger(AttractionCalculator.class);

    private final DataSet dataSet;

    AttractionCalculator(DataSet dataSet, List<Purpose> purposes) {
        this.dataSet = dataSet;
        this.purposes = purposes;
    }

    public void run() {
        logger.info("  Calculating trip attractions");
        for (MitoZone zone : dataSet.getZones().values()) {
            for (Purpose purpose : purposes) {
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
                            attribute = zone.getEmployeesByCategory(Category.RETAIL);
                            break;
                        case OFF:
                            attribute = zone.getEmployeesByCategory(Category.OFFICE);
                            break;
                        case OTH:
                            attribute = zone.getEmployeesByCategory(Category.OTHER);
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

                zone.setTripAttraction(purpose, tripAttraction);

            }
        }
    }
}
