package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

public class AttractionCalculator {

    public enum ExplanatoryVariable {HH, TOT, RE, OFF, OTH, ENR};

    private static final Logger logger = Logger.getLogger(AttractionCalculator.class);

    private final DataSet dataSet;

    public AttractionCalculator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {
        logger.info("  Calculating trip attractions");
        for (Zone zone : dataSet.getZones().values()) {
            for (Purpose purpose : Purpose.values()) {
                float tripAttraction = 0;
                for (ExplanatoryVariable variable : ExplanatoryVariable.values()) {
                    float attribute = 0;
                    switch (variable) {
                        case HH:
                            attribute = zone.getNumberOfHouseholds();
                            break;
                        case TOT:
                            attribute = zone.getTotalEmpl();
                            break;
                        case RE:
                            attribute = zone.getRetailEmpl();
                            break;
                        case OFF:
                            attribute = zone.getOfficeEmpl();
                            break;
                        case OTH:
                            attribute = zone.getOtherEmpl();
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
}
