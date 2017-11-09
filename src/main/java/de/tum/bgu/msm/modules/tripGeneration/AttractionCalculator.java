package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.data.Purpose;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AttractionCalculator {

    private static final Logger logger = Logger.getLogger(AttractionCalculator.class);

    private final DataSet dataSet;

    public AttractionCalculator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {

        logger.info("  Calculating trip attractions");
        TableDataSet attrRates = dataSet.getTripAttractionRates();
        Map<String, Float> attractionRates = getAttractionRates(attrRates);
        String[] independentVariables = attrRates.getColumnAsString("IndependentVariable");

        Collection<Zone> zones = dataSet.getZones().values();
        for (Zone zone: zones) {
            for (Purpose purpose: Purpose.values()) {
                float tripAttraction = 0;
                for (String variable: independentVariables) {
                    String token = purpose + "_" + variable;
                    if (attractionRates.containsKey(token)) {
                        float attribute = 0;
                        switch (variable) {
                            case "HH":
                                attribute = zone.getNumberOfHouseholds();
                                break;
                            case "TOT":
                                attribute = zone.getTotalEmpl();
                                break;
                            case "RE":
                                attribute = zone.getRetailEmpl();
                                break;
                            case "OFF":
                                attribute = zone.getOfficeEmpl();
                                break;
                            case "OTH":
                                attribute = zone.getOtherEmpl();
                                break;
                            case "ENR":
                                attribute = zone.getSchoolEnrollment();
                                break;
                        }
                        tripAttraction += attribute * attractionRates.get(token);
                    } else {
                        logger.warn("No attraction rate found for token " + token);
                    }
                }
                zone.setTripAttractionRate(purpose, tripAttraction);
            }
        }
    }


    private HashMap<String, Float> getAttractionRates (TableDataSet attrRates) {
        // read attraction rate file and create HashMap

        HashMap<String, Float> attractionRates = new HashMap<>();
        for (int row = 1; row <= attrRates.getRowCount(); row++) {
            String generator = attrRates.getStringValueAt(row, "IndependentVariable");
            for (Purpose purpose: Purpose.values()) {
                float rate = attrRates.getValueAt(row, purpose.toString());
                String token = purpose.toString() + "_" + generator;
                attractionRates.put(token, rate);
            }
        }
        return attractionRates;
    }
}
