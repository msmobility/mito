package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.io.input.readers.TripAttractionRatesReader;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.modules.tripGeneration.ExplanatoryVariable;
import org.apache.log4j.Logger;

import java.util.List;

public class AttractionCalculatorGermany implements AttractionCalculator {

    private final Purpose purpose;

    private static final Logger logger = Logger.getLogger(AttractionCalculatorGermany.class);

    private final DataSet dataSet;

    public AttractionCalculatorGermany(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        new TripAttractionRatesReader(dataSet, purpose).read();
    }

    @Override
    public void run() {
        logger.info("  Calculating trip attractions");
        for (MitoZone zone : dataSet.getZones().values()) {
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
