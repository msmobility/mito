package de.tum.bgu.msm.modules.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 20.07.2017.
 */
public class AttractionCalculator {

    private static final Logger logger = Logger.getLogger(AttractionCalculator.class);

    private final DataSet dataSet;

    public AttractionCalculator(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public Map<Integer, Map<String, Float>> run() {

        logger.info("  Calculating trip attractions");
        TableDataSet attrRates = dataSet.getTripAttractionRates();
        Map<String, Float> attractionRates = getAttractionRates(attrRates);
        String[] independentVariables = attrRates.getColumnAsString("IndependentVariable");

        Collection<Zone> zones = dataSet.getZones().values();
        Map<Integer, Map<String, Float>> tripAttrByZoneAndPurp = new HashMap<>();
        for (Zone zone: zones) {
            Map<String, Float> tripAttrByPurp = new HashMap<>();
            for (int purp = 0; purp < dataSet.getPurposes().length; purp++) {
                float tripAttr = 0;
                for (String variable: independentVariables) {
                    String token = dataSet.getPurposes()[purp] + "_" + variable;
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
                        tripAttr += attribute * attractionRates.get(token);
                    }
                }
                tripAttrByPurp.put(dataSet.getPurposes()[purp], tripAttr);
            }
            tripAttrByZoneAndPurp.put(zone.getZoneId(), tripAttrByPurp);
        }
        return tripAttrByZoneAndPurp;
    }


    private HashMap<String, Float> getAttractionRates (TableDataSet attrRates) {
        // read attraction rate file and create HashMap

        HashMap<String, Float> attractionRates = new HashMap<>();
        for (int row = 1; row <= attrRates.getRowCount(); row++) {
            String generator = attrRates.getStringValueAt(row, "IndependentVariable");
            for (String purp: dataSet.getPurposes()) {
                float rate = attrRates.getValueAt(row, purp);
                String token = purp + "_" + generator;
                attractionRates.put(token, rate);
            }
        }
        return attractionRates;
    }
}
