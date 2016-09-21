package main.java.de.tum.bgu.msm.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.util.ResourceUtil;
import main.java.de.tum.bgu.msm.TimoData;
import main.java.de.tum.bgu.msm.TimoTravelDemand;
import main.java.de.tum.bgu.msm.TimoUtil;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration {

    private static Logger logger = Logger.getLogger(TimoTravelDemand.class);
    private ResourceBundle rb;
    private TimoData td;

    public TripGeneration(ResourceBundle rb, TimoData td) {
        this.rb = rb;
        this.td = td;
    }

    public void generateTrips () {
        // Run trip generation model

        logger.info("  Started microscopic trip generation model.");
        microgenerateTrips();
        removeNonMotorizedTrips();
        dampenTripGenAtStudyAreaBorder();
        calculateTripAttractions();
        balanceTripGeneration();
        scaleTripGeneration();
        writeTrips();
        logger.info("  Completed microscopic trip generation model.");

    }


    private String selectAutoMode (String purpose) {
        // return autos or autoSufficiency depending on mode chosen

        String autoMode = "autos";
        if (purpose.equalsIgnoreCase("HBW") || purpose.equalsIgnoreCase("NHBW")) autoMode = "autoSufficiency";
        return autoMode;
    }


    private void microgenerateTrips () {

        TableDataSet regionDefinition = TimoUtil.readCSVfile(rb.getString("household.travel.survey.reg"));
        regionDefinition.buildIndex(regionDefinition.getColumnPosition("SMZRMZ"));

        // Generate trips for each purpose
        float[][][] tripProd = new float[TimoUtil.getHighestVal(td.getZones()) + 1][td.getPurposes().length][6];
        for (int purp = 0; purp < td.getPurposes().length; purp++) {
            String strPurp = td.getPurposes()[purp];
            logger.info("  Generating trips with purpose " + strPurp);
            TableDataSet hhTypeDef = createHHTypeDefinition(strPurp);
            int[] hhTypeArray = td.defineHouseholdTypeOfEachSurveyRecords(selectAutoMode(strPurp), hhTypeDef);
            HashMap<String, Integer[]> tripsByHhTypeAndPurpose = td.collectTripFrequencyDistribution(hhTypeArray);
            // Generate trips for each household
            for (Household hh: Household.getHouseholdArray()) {
                int region = (int) regionDefinition.getIndexedValueAt(hh.getHomeZone(), "Regions");
                int incCategory = translateIncomeIntoCategory (hh.getHhIncome());
                int hhType = td.getHhType(selectAutoMode(strPurp), hhTypeDef, hh.getHhSize(), hh.getNumberOfWorkers(),
                        incCategory, hh.getAutos(), region);
                String token = hhType + "_" + strPurp;
                Integer[] tripFrequencies = tripsByHhTypeAndPurpose.get(token);
                if (tripFrequencies == null) {
                    logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
                }
                if (TimoUtil.getSum(tripFrequencies) == 0) continue;
                int numTrips = selectNumberOfTrips(tripFrequencies);
                int mstmIncCat = defineMstmIncomeCategory(hh.getHhIncome());
                tripProd[hh.getHomeZone()][purp][mstmIncCat] += numTrips;
            }
        }
        logger.info("  Generated " + TimoUtil.customFormat("###,###", TimoUtil.getSum(tripProd)) + " raw trips.");
    }


    private TableDataSet createHHTypeDefinition (String purpose) {
        // create household type definition file
        String[] hhDefToken = ResourceUtil.getArray(rb, ("hh.type." + purpose));
        //        int categoryID = Integer.parseInt(hhDefToken[0]);
        int numCategories = Integer.parseInt(hhDefToken[1]);
        String sizeToken = hhDefToken[2];
        String[] sizePortions = sizeToken.split("\\.");
        String workerToken = hhDefToken[3];
        String[] workerPortions = workerToken.split("\\.");
        String incomeToken = hhDefToken[4];
        String[] incomePortions = incomeToken.split("\\.");
        String autoToken = hhDefToken[5];
        String[] autoPortions = autoToken.split("\\.");
        String regionToken = hhDefToken[6];
        String[] regionPortions = regionToken.split("\\.");
        TableDataSet hhTypeDef = createHouseholdTypeTableDataSet(numCategories, sizePortions, workerPortions,
                incomePortions, autoPortions, regionPortions);
        int[] hhCounter = td.defineHouseholdTypeOfEachSurveyRecords(selectAutoMode(purpose), hhTypeDef);
        HashMap<Integer, Integer> numHhByType = new HashMap<>();
        for (int hhType: hhCounter) {
            if (numHhByType.containsKey(hhType)) {
                int oldNum = numHhByType.get(hhType);
                numHhByType.put(hhType, (oldNum + 1));
            } else {
                numHhByType.put(hhType, 1);
            }
        }
        hhTypeDef.appendColumn(new float[hhTypeDef.getRowCount()], "counter");
        hhTypeDef.buildIndex(hhTypeDef.getColumnPosition("hhType"));
        for (int type: numHhByType.keySet()) {
            if (type == 0) continue;
            hhTypeDef.setIndexedValueAt(type, "counter", numHhByType.get(type));
        }
//        mstmUtilities.writeTable(hhTypeDef, "temp_" + purpose + ".csv");
        return hhTypeDef;
    }


    public TableDataSet createHouseholdTypeTableDataSet (int numCategories, String[] sizePortions, String[] workerPortions,
                                                         String[] incomePortions, String[] autoPortions, String[] regionPortions) {
        // create household type TableDataSet

        int[] hhType = new int[numCategories];
        int[] size_l = new int[numCategories];
        int[] size_h = new int[numCategories];
        int[] workers_l = new int[numCategories];
        int[] workers_h = new int[numCategories];
        int[] income_l = new int[numCategories];
        int[] income_h = new int[numCategories];
        int[] autos_l = new int[numCategories];
        int[] autos_h = new int[numCategories];
        int[] region_l = new int[numCategories];
        int[] region_h = new int[numCategories];

        int typeCounter = 0;
        for (String sizeToken: sizePortions) {
            String[] sizeParts = sizeToken.split("-");
            for (String workerToken : workerPortions) {
                String[] workerParts = workerToken.split("-");
                for (String incomeToken : incomePortions) {
                    String[] incomeParts = incomeToken.split("-");
                    for (String autoToken : autoPortions) {
                        String[] autoParts = autoToken.split("-");
                        for (String regionToken : regionPortions) {
                            String[] regionParts = regionToken.split("-");
                            hhType[typeCounter] = typeCounter + 1;
                            size_l[typeCounter] = Integer.parseInt(sizeParts[0]);
                            size_h[typeCounter] = Integer.parseInt(sizeParts[1]);
                            workers_l[typeCounter] = Integer.parseInt(workerParts[0]) - 1;
                            workers_h[typeCounter] = Integer.parseInt(workerParts[1]) - 1;
                            income_l[typeCounter] = Integer.parseInt(incomeParts[0]);
                            income_h[typeCounter] = Integer.parseInt(incomeParts[1]);
                            autos_l[typeCounter] = Integer.parseInt(autoParts[0]) - 1;
                            autos_h[typeCounter] = Integer.parseInt(autoParts[1]) - 1;
                            region_l[typeCounter] = Integer.parseInt(regionParts[0]);
                            region_h[typeCounter] = Integer.parseInt(regionParts[1]);
                            typeCounter++;
                        }
                    }
                }
            }
        }

        TableDataSet hhTypeDef = new TableDataSet();
        hhTypeDef.appendColumn(hhType, "hhType");
        hhTypeDef.appendColumn(size_l, "size_l");
        hhTypeDef.appendColumn(size_h, "size_h");
        hhTypeDef.appendColumn(workers_l, "workers_l");
        hhTypeDef.appendColumn(workers_h, "workers_h");
        hhTypeDef.appendColumn(income_l, "income_l");
        hhTypeDef.appendColumn(income_h, "income_h");
        hhTypeDef.appendColumn(autos_l, "autos_l");
        hhTypeDef.appendColumn(autos_h, "autos_h");
        hhTypeDef.appendColumn(region_l, "region_l");
        hhTypeDef.appendColumn(region_h, "region_h");
        hhTypeDef.buildIndex(hhTypeDef.getColumnPosition("hhType"));
        return hhTypeDef;
    }


    private int translateIncomeIntoCategory (int hhIncome) {
        // translate income in absolute dollars into household travel survey income categories

        if (hhIncome < 10000) return 1;
        else if (hhIncome >= 10000 && hhIncome < 15000) return 2;
        else if (hhIncome >= 15000 && hhIncome < 30000) return 3;
        else if (hhIncome >= 30000 && hhIncome < 40000) return 4;
        else if (hhIncome >= 40000 && hhIncome < 50000) return 5;
        else if (hhIncome >= 50000 && hhIncome < 60000) return 6;
        else if (hhIncome >= 60000 && hhIncome < 75000) return 7;
        else if (hhIncome >= 75000 && hhIncome < 100000) return 8;
        else if (hhIncome >= 100000 && hhIncome < 125000) return 9;
        else if (hhIncome >= 125000 && hhIncome < 150000) return 10;
        else if (hhIncome >= 150000 && hhIncome < 200000) return 11;
        else if (hhIncome >= 200000) return 12;
        logger.error("Unknown HTS income: " + hhIncome);
        return -1;
    }


    private int defineMstmIncomeCategory (int hhIncome) {
        // translate income in absolute dollars into MSTM income categories

        if (hhIncome < 20000) return 1;
        else if (hhIncome >= 20000 && hhIncome < 40000) return 2;
        else if (hhIncome >= 40000 && hhIncome < 60000) return 3;
        else if (hhIncome >= 60000 && hhIncome < 100000) return 4;
        else if (hhIncome >= 100000) return 5;
        logger.error("Unknown MSTM income: " + hhIncome);
        return -1;
    }


    private int selectNumberOfTrips (Integer[] tripFrequencies) {
        // select number of trips
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) probabilities[i] = (double) tripFrequencies[i];
        return TimoUtil.select(td.getRand(), probabilities);
    }


    private void removeNonMotorizedTrips () {
        // subtract fixed share of trips by purpose and zone that is assumed to be non-motorized

        int[] householdsByZone = summarizeData.getHouseholdsByZone();
        int[] retailEmplByZone = summarizeData.getRetailEmploymentByZone();
        int[] otherEmplByZone = summarizeData.getOtherEmploymentByZone();
        int[] totalEmplByZone = summarizeData.getTotalEmploymentByZone();
        TripGenAccessibility acc = new TripGenAccessibility(rb, year, householdsByZone, retailEmplByZone, otherEmplByZone);
        int[] zones = geoData.getZones();
        float[] hhDensity = new float[zones.length];
        float[] actDensity = new float[zones.length];
        for (int zone: zones) {
            hhDensity[geoData.getZoneIndex(zone)] = householdsByZone[geoData.getZoneIndex(zone)] /
                    geoData.getSizeOfZoneInAcres(zone);
            actDensity[geoData.getZoneIndex(zone)] = (householdsByZone[geoData.getZoneIndex(zone)] +
                    retailEmplByZone[geoData.getZoneIndex(zone)]) +
                    totalEmplByZone[geoData.getZoneIndex(zone)] /
                            geoData.getSizeOfZoneInAcres(zone);
        }
        logger.info("  Removing non-motorized trips");
        TableDataSet nmFunctions = SiloUtil.readCSVfile(rb.getString("non.motorized.share.functions"));
        nmFunctions.buildStringIndex(nmFunctions.getColumnPosition("Purpose"));

        float[][][] nonMotTrips = new float[6][5][zones.length];  // non-motorized trips by purpose, income and zone
        for (int zone: zones) {
//            if (zone > mstmData.highestSmz) continue;
            for (int purp = 0; purp < tripPurposes.values().length; purp++) {
                String purpTxt = tripPurposes.values()[purp].toString();
                for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                    String purpose;
                    if (purp < 3) {
                        purpose = purpTxt + mstmInc;
                    } else {
                        purpose = purpTxt;
                    }
                    float nonMotShare =
                            hhDensity[geoData.getZoneIndex(zone)] * nmFunctions.getStringIndexedValueAt(purpose, "hhDensity") +
                                    actDensity[geoData.getZoneIndex(zone)] * nmFunctions.getStringIndexedValueAt(purpose, "actDensity") +
                                    acc.getAutoAccessibilityHouseholds(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccHH") +
                                    acc.getAutoAccessibilityRetail(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccRetailEmp") +
                                    acc.getAutoAccessibilityOther(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccOtherEmp") +
                                    acc.getTransitAccessibilityOther(zone) * nmFunctions.getStringIndexedValueAt(purpose, "trnAccOtherEmp");
                    nonMotTrips[purp][mstmInc-1][geoData.getZoneIndex(zone)] = tripProd[zone][purp][mstmInc] * nonMotShare;
                    tripProd[zone][purp][mstmInc] -= nonMotTrips[purp][mstmInc-1][geoData.getZoneIndex(zone)];
                }
            }
        }
        PrintWriter pw = SiloUtil.openFileForSequentialWriting(rb.getString("non.motorized.trips"), false);
        pw.println("SMZ,HBW1,HBW2,HBW3,HBW4,HBW5,HBS1,HBS2,HBS3,HBS4,HBS5,HBO1,HBO2,HBO3,HBO4,HBO5,HBE,NHBW,NHBO");
        float nonMotSum = 0;
        for (int zone: zones) {
            pw.print(zone);
            for (int purp = 0; purp < tripPurposes.values().length; purp++) {
                float purpSum = 0;
                for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                    if (purp < 3) {
                        pw.print("," + nonMotTrips[purp][mstmInc-1][geoData.getZoneIndex(zone)]);
                    } else {
                        purpSum += nonMotTrips[purp][mstmInc-1][geoData.getZoneIndex(zone)];
                    }
                    nonMotSum += nonMotTrips[purp][mstmInc-1][geoData.getZoneIndex(zone)];
                }
                if (purp >= 3) pw.print("," + purpSum);
            }
            pw.println();
        }
        pw.close();
        logger.info("  Removed " + SiloUtil.customFormat("###,###", nonMotSum) + " non-motorized trips");
    }


    private void dampenTripGenAtStudyAreaBorder () {
        // as trips near border of study area that travel to destinations outside of study area are not represented,
        // trip generation near border of study area is artificially reduced

        logger.info("  Removing short-distance trips that would cross border of MSTM study area");
        TableDataSet damperNearMstmBorder = SiloUtil.readCSVfile(rb.getString("damper.near.mstm.border"));
        damperNearMstmBorder.buildIndex(damperNearMstmBorder.getColumnPosition("SMZ"));

        PrintWriter pw = SiloUtil.openFileForSequentialWriting(rb.getString("removd.trips.near.mstm.border"), false);
        pw.println("SMZ,removedTrips");
        int[] zones = geoData.getZones();
        float removedTripsSum = 0;
        for (int zone: zones) {
//            if (zone > mstmData.highestSmz) continue;
            float damper = damperNearMstmBorder.getIndexedValueAt(zone, "damper");
            if (damper == 0) continue;
            float removedTripsThisZone = 0;
            for (int purp = 0; purp < tripPurposes.values().length; purp++) {
                for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                    float removedTrips = tripProd[zone][purp][mstmInc] * damper;
                    tripProd[zone][purp][mstmInc] -= removedTrips;
                    removedTripsThisZone += removedTrips;
                }
            }
            pw.println(zone + "," + removedTripsThisZone);
            removedTripsSum += removedTripsThisZone;
        }
        pw.close();
        logger.info("  Removed " + SiloUtil.customFormat("###,###", removedTripsSum) +
                " short-distance trips near border of MSTM study area");
    }


    private void calculateTripAttractions () {
        // calculate zonal trip attractions

        logger.info("  Calculating trip attractions");
        TableDataSet attrRates = SiloUtil.readCSVfile(rb.getString("trip.attraction.rates"));
        HashMap<String, Float> attractionRates = getAttractionRates(attrRates);
        String[] independentVariables = attrRates.getColumnAsString("IndependentVariable");

        TableDataSet attrData = SiloUtil.readCSVfile(rb.getString("smz.demographics") + year + ".csv");
        attrData.buildIndex(attrData.getColumnPosition(";SMZ_N"));

        int[] zones = geoData.getZones();
        tripAttr = new float[SiloUtil.getHighestVal(geoData.getZones()) + 1][tripPurposes.values().length][6];
        for (int zone: zones) {
//            if (zone > mstmData.highestSmz) continue;
            for (int purp = 0; purp < tripPurposes.values().length; purp++) {
                for (String variable: independentVariables) {
                    String token = tripPurposes.values()[purp].toString() + "_" + variable;
                    if (attractionRates.containsKey(token)) {
                        tripAttr[zone][purp][0] += attrData.getIndexedValueAt(zone, variable + year) *
                                attractionRates.get(token);
                    }
                }
            }
        }
    }


    private HashMap<String, Float> getAttractionRates (TableDataSet attrRates) {
        // read attraction rate file and create HashMap

        HashMap<String, Float> attractionRates = new HashMap<>();
        for (int row = 1; row <= attrRates.getRowCount(); row++) {
            String generator = attrRates.getStringValueAt(row, "IndependentVariable");
            for (tripPurposes purp: tripPurposes.values()) {
                float rate = attrRates.getValueAt(row, purp.toString());
                String token = purp.toString() + "_" + generator;
                attractionRates.put(token, rate);
            }
        }
        return attractionRates;
    }


    private void balanceTripGeneration() {
        // Balance trip production and trip attraction

        logger.info("  Balancing trip production and attractions");
        int[] zones = geoData.getZones();
        for (int purp = 0; purp < tripPurposes.values().length; purp++) {
            for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                float prodSum = 0;
                float attrSum = 0;
                for (int zone: zones) {
//                    if (zone > mstmData.highestSmz) continue;
                    attrSum += tripAttr[zone][purp][0];
                    prodSum += tripProd[zone][purp][mstmInc];
                }
                // adjust attractions (or productions for NHBW and NHBO)
                for (int zone: zones) {
//                    if (zone > mstmData.highestSmz) continue;
                    tripAttr[zone][purp][mstmInc] = tripAttr[zone][purp][0] * prodSum / attrSum;

                    // for NHBW and NHBO, we have more confidence in total production, as it is based on the household
                    // travel survey. The distribution, however, is better represented by attraction rates. Therefore,
                    // attractions are first scaled to productions (step above) and then productions are replaced with
                    // zonal level attractions (step below).
                    if (tripPurposes.values()[purp] == tripPurposes.NHBW || tripPurposes.values()[purp] == tripPurposes.NHBO) {
                        tripProd[zone][purp][mstmInc] = tripAttr[zone][purp][mstmInc];
                    }
                }
            }
        }
    }


    private void scaleTripGeneration() {
        // scale trip generation to account for underreporting in survey

        logger.info("  Scaling trip production and attraction to account for underreporting in survey");
        String[] token = ResourceUtil.getArray(rb, "trip.gen.scaler.purpose");
        double[] scaler = ResourceUtil.getDoubleArray(rb, "trip.gen.scaler.factor");
        HashMap<String, Double[]> scale = new HashMap<>();
        for (tripPurposes purp: tripPurposes.values()) scale.put(purp.toString(), new Double[]{0d,0d,0d,0d,0d});
        for (int i = 0; i < token.length; i++) {
            String[] tokenParts = token[i].split(Pattern.quote("."));
            if (tokenParts.length == 2) {
                // purpose is split by income categories
                Double[] values = scale.get(tokenParts[0]);
                values[Integer.parseInt(tokenParts[1]) - 1] = scaler[i];
            } else {
                // purpose is not split by income categories
                Double[] values = scale.get(token[i]);
                for (int inc = 0; inc < values.length; inc++) values[inc] = scaler[i];
            }
        }
        for (int purp = 0; purp < tripPurposes.values().length; purp++) {
            Double[] scalingFactors = scale.get(tripPurposes.values()[purp].toString());
            for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                if (scalingFactors[mstmInc-1] == 1) continue;
                for (int zone: geoData.getZones()) {
                    tripProd[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
                    tripAttr[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
                }
            }
        }
    }


    private void writeTrips() {
        // write trips by five income groups to output file

        PrintWriter pwProd = SiloUtil.openFileForSequentialWriting(rb.getString("trip.production.output"), false);
        PrintWriter pwAttr = SiloUtil.openFileForSequentialWriting(rb.getString("trip.attraction.output"), false);
        pwProd.print(";SMZ");
        pwAttr.print(";SMZ");
        for (tripPurposes tripPurpose: tripPurposes.values()) {
            if (tripPurpose.toString().equalsIgnoreCase("HBW") || tripPurpose.toString().equalsIgnoreCase("HBS") ||
                    tripPurpose.toString().equalsIgnoreCase("HBO")) {
                // split purpose by 5 MSTM income categories
                for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                    pwProd.print("," + tripPurpose.toString() + "P" + mstmInc);
                    pwAttr.print("," + tripPurpose.toString() + "A" + mstmInc);
                }
            } else {
                // purpose are not split by income
                pwProd.print("," + tripPurpose.toString() + "P");
                pwAttr.print("," + tripPurpose.toString() + "A");
            }
        }
        pwProd.println();
        pwAttr.println();
        for (int zone: geoData.getZones()) {
//            if (zone > mstmData.highestSmz) break;

            pwProd.print(zone);
            pwAttr.print(zone);
            for (int purp = 0; purp < tripProd[0].length; purp++) {
                if (purp < 3) {
                    // split tripProd by 5 income categories
                    for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
                        pwProd.print("," + tripProd[zone][purp][mstmInc]);
                        pwAttr.print("," + tripAttr[zone][purp][mstmInc]);
                    }
                } else {
                    float prod = tripProd[zone][purp][1] + tripProd[zone][purp][2] + tripProd[zone][purp][3] +
                            tripProd[zone][purp][4] + tripProd[zone][purp][5];
                    pwProd.print("," + prod);
                    float attr = tripAttr[zone][purp][1] + tripAttr[zone][purp][2] + tripAttr[zone][purp][3] +
                            tripAttr[zone][purp][4] + tripAttr[zone][purp][5];
                    pwAttr.print("," + attr);
                }
            }
            pwProd.println();
            pwAttr.println();

        }
        pwProd.close();
        pwAttr.close();
        logger.info("  Wrote out a total of " + SiloUtil.customFormat("###,###.#", SiloUtil.getSum(tripProd)) + " trips.");
    }
}

}
