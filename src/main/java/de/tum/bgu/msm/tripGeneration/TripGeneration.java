package de.tum.bgu.msm.tripGeneration;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.*;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration {

    private static Logger logger = Logger.getLogger(MitoTravelDemand.class);
    private ResourceBundle rb;
    private MitoData td;

    public TripGeneration(ResourceBundle rb, MitoData td) {
        this.rb = rb;
        this.td = td;
    }

    public void generateTrips () {
        // Run trip generation model

        logger.info("  Started microscopic trip generation model.");
        microgenerateTrips();
        if (ResourceUtil.getBooleanProperty(rb, "remove.non.motorized.trips", false)) removeNonMotorizedTrips();
        if (ResourceUtil.getBooleanProperty(rb, "reduce.trips.at.outer.border", false)) reduceTripGenAtStudyAreaBorder();
        float[][] rawTripAttr = calculateTripAttractions();
        float[][] balancedAttr = balanceTripGeneration(rawTripAttr);
        writeTripSummary(balancedAttr);

        logger.info("  Completed microscopic trip generation model.");

    }


    private String selectAutoMode (String purpose) {
        // return autos or autoSufficiency depending on mode chosen

        String autoMode = "autos";
        if (purpose.equalsIgnoreCase("HBW") || purpose.equalsIgnoreCase("NHBW")) autoMode = "autoSufficiency";
        return autoMode;
    }


    private void microgenerateTrips () {

        TableDataSet regionDefinition = MitoUtil.readCSVfile(rb.getString("household.travel.survey.reg"));
        regionDefinition.buildIndex(regionDefinition.getColumnPosition("SMZRMZ"));

        // Generate trips for each purpose
        int tripCounter = 0;
        for (int purp = 0; purp < td.getPurposes().length; purp++) {
            String strPurp = td.getPurposes()[purp];
            logger.info("  Generating trips with purpose " + strPurp);
            TableDataSet hhTypeDef = createHHTypeDefinition(strPurp);
            int[] hhTypeArray = td.defineHouseholdTypeOfEachSurveyRecords(selectAutoMode(strPurp), hhTypeDef);
            HashMap<String, Integer[]> tripsByHhTypeAndPurpose = td.collectTripFrequencyDistribution(hhTypeArray);
            // Generate trips for each household
            for (MitoHousehold hh: td.getMitoHouseholds()) {
                int region = (int) regionDefinition.getIndexedValueAt(hh.getHomeZone(), "Regions");
                int incCategory = translateIncomeIntoCategory (hh.getIncome());
                int hhType = td.getHhType(selectAutoMode(strPurp), hhTypeDef, hh.getHhSize(), hh.getNumberOfWorkers(),
                        incCategory, hh.getAutos(), region);
                String token = hhType + "_" + strPurp;
                Integer[] tripFrequencies = tripsByHhTypeAndPurpose.get(token);
                if (tripFrequencies == null) {
                    logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
                }
                if (MitoUtil.getSum(tripFrequencies) == 0) continue;
                int numTrips = selectNumberOfTrips(tripFrequencies);
                int mstmIncCat = defineMstmIncomeCategory(hh.getIncome());
                hh.setNumberOfTrips(purp, numTrips);
                tripCounter++;
            }
        }
        logger.info("  Generated " + MitoUtil.customFormat("###,###", tripCounter) + " raw trips.");
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
        return MitoUtil.select(td.getRand(), probabilities);
    }


    private void removeNonMotorizedTrips () {
        // subtract fixed share of trips by purpose and zone that is assumed to be non-motorized

        MitoAccessibility ta = new MitoAccessibility(rb, td);
        ta.calculateAccessibilities();

        int[] zones = td.getZones();
        float[] hhDensity = new float[zones.length];
        float[] actDensity = new float[zones.length];
        for (int zone: zones) {
            hhDensity[td.getZoneIndex(zone)] = td.getHouseholdsByZone(zone) /
                    td.getSizeOfZoneInAcre(zone);
            actDensity[td.getZoneIndex(zone)] = (td.getHouseholdsByZone(zone) + td.getRetailEmplByZone(zone) +
                    td.getTotalEmplByZone(zone)) / td.getSizeOfZoneInAcre(zone);
        }
        logger.info("  Removing non-motorized trips");
        TableDataSet nmFunctions = MitoUtil.readCSVfile(rb.getString("non.motorized.share.functions"));
        nmFunctions.buildStringIndex(nmFunctions.getColumnPosition("Purpose"));

        float[][][] nonMotShare = new float[6][5][zones.length];  // non-motorized share by purpose, income and zone
        for (int zone: zones) {
            for (int purp = 0; purp < td.getPurposes().length; purp++) {
                for (int incomeCategory = 1; incomeCategory <= 5; incomeCategory++) {
                    String purpose;
                    if (purp < 3) {
                        purpose = td.getPurposes()[purp] + incomeCategory;
                    } else {
                        purpose = td.getPurposes()[purp];
                    }
                    nonMotShare[purp][incomeCategory-1][td.getZoneIndex(zone)] =
                            hhDensity[td.getZoneIndex(zone)] * nmFunctions.getStringIndexedValueAt(purpose, "hhDensity") +
                                    actDensity[td.getZoneIndex(zone)] * nmFunctions.getStringIndexedValueAt(purpose, "actDensity") +
                                    ta.getAutoAccessibilityHouseholds(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccHH") +
                                    ta.getAutoAccessibilityRetail(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccRetailEmp") +
                                    ta.getAutoAccessibilityOther(zone) * nmFunctions.getStringIndexedValueAt(purpose, "carAccOtherEmp") +
                                    ta.getTransitAccessibilityOther(zone) * nmFunctions.getStringIndexedValueAt(purpose, "trnAccOtherEmp");
                    if (nonMotShare[purp][incomeCategory-1][td.getZoneIndex(zone)] < 0 ||
                            nonMotShare[purp][incomeCategory-1][td.getZoneIndex(zone)] > 1) logger.warn("Non-motorized share" +
                            "out of range in zone " + zone + ": " + nonMotShare[purp][incomeCategory-1][td.getZoneIndex(zone)]);
                }
            }
        }

        // loop over every household and every trip and randomly remove trips based on nonMotShare[][][]
        int[][] nonMotCounter = new int[td.getPurposes().length][td.getZones().length];
        for (MitoHousehold thh: td.getMitoHouseholds()) {
            int inc = defineMstmIncomeCategory(thh.getIncome());
            for (int purp = 0; purp < td.getPurposes().length; purp++) {

                int allTrips = thh.getNumberOfTrips(purp);
                int nonMot = 0;
                for (int trip = 1; trip <= allTrips; trip++)
                    if (td.getRand().nextFloat() < nonMotShare[purp][inc - 1][td.getZoneIndex(thh.getHomeZone())]) {
                        nonMot++;
                        nonMotCounter[purp][td.getZoneIndex(thh.getHomeZone())]++;
                    }
                thh.setNonMotorizedNumberOfTrips(purp, nonMot);
                thh.setNumberOfTrips(purp, (thh.getNumberOfTrips(purp) - nonMot));
            }
        }

        PrintWriter pw = MitoUtil.openFileForSequentialWriting(rb.getString("non.motorized.trips"), false);
        pw.print("Zone");
        for (String purpose: td.getPurposes()) pw.print("," + purpose);
        pw.println();

        for (int zone: zones) {
            pw.print(zone);
            for (int purp = 0; purp < td.getPurposes().length; purp++)
                pw.print("," + nonMotCounter[purp][td.getZoneIndex(zone)]);
            pw.println();
        }
        pw.close();
        logger.info("  Removed " + MitoUtil.customFormat("###,###", MitoUtil.getSum(nonMotCounter)) + " non-motorized trips");
    }


    private void reduceTripGenAtStudyAreaBorder() {
        // as trips near border of study area that travel to destinations outside of study area are not represented,
        // trip generation near border of study area can be reduced artificially with this method

        logger.info("  Removing short-distance trips that would cross border study area");
        TableDataSet reductionNearBorder = MitoUtil.readCSVfile(rb.getString("reduction.near.outer.border"));
        reductionNearBorder.buildIndex(reductionNearBorder.getColumnPosition("Zone"));

        float[] removedTrips = new float[td.getZones().length];
        for (MitoHousehold thh: td.getMitoHouseholds()) {
            float damper = reductionNearBorder.getIndexedValueAt(thh.getHomeZone(), "damper");
            if (damper == 0) continue;
            for (int purp = 0; purp < td.getPurposes().length; purp++) {
                int eliminatedTrips = 0;
                for (int trip=1; trip <= thh.getNumberOfTrips(purp); trip++) {
                    if (td.getRand().nextFloat() < damper) eliminatedTrips++;
                }
                if (eliminatedTrips > 0) {
                    thh.setNumberOfTrips(purp, (thh.getNumberOfTrips(purp) - eliminatedTrips));
                    removedTrips[td.getZoneIndex(thh.getHomeZone())] += eliminatedTrips;
                }
            }
        }
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(rb.getString("removed.trips.near.border"), false);
        pw.println("Zone,removedTrips");
        for (int zone: td.getZones()) pw.println(zone + "," + removedTrips[td.getZoneIndex(zone)]);
        pw.close();
        logger.info("  Removed " + MitoUtil.customFormat("###,###", MitoUtil.getSum(removedTrips)) +
                " short-distance trips near border of MSTM study area");
    }


    private float[][] calculateTripAttractions () {
        // calculate zonal trip attractions

        logger.info("  Calculating trip attractions");
        TableDataSet attrRates = MitoUtil.readCSVfile(rb.getString("trip.attraction.rates"));
        HashMap<String, Float> attractionRates = getAttractionRates(attrRates);
        String[] independentVariables = attrRates.getColumnAsString("IndependentVariable");

        int[] zones = td.getZones();
        float[][] tripAttr = new float[td.getZones().length][td.getPurposes().length];  // by zones, purposes and income
        for (int zone: zones) {
            for (int purp = 0; purp < td.getPurposes().length; purp++) {
                for (String variable: independentVariables) {
                    String token = td.getPurposes()[purp] + "_" + variable;
                    if (attractionRates.containsKey(token)) {
                        float attribute = 0;
                        if (variable.equals("HH")) attribute = td.getHouseholdsByZone(zone);
                        else if (variable.equals("TOT")) td.getTotalEmplByZone(zone);
                        else if (variable.equals("RE")) td.getRetailEmplByZone(zone);
                        else if (variable.equals("OFF")) td.getOfficeEmplByZone(zone);
                        else if (variable.equals("OTH")) td.getOtherEmplByZone(zone);
                        else if (variable.equals("ENR")) td.getSchoolEnrollmentByZone(zone);
                        tripAttr[zone][purp] += attribute * attractionRates.get(token);
                    }
                }
            }
        }
        return tripAttr;
    }


    private HashMap<String, Float> getAttractionRates (TableDataSet attrRates) {
        // read attraction rate file and create HashMap

        HashMap<String, Float> attractionRates = new HashMap<>();
        for (int row = 1; row <= attrRates.getRowCount(); row++) {
            String generator = attrRates.getStringValueAt(row, "IndependentVariable");
            for (String purp: td.getPurposes()) {
                float rate = attrRates.getValueAt(row, purp);
                String token = purp + "_" + generator;
                attractionRates.put(token, rate);
            }
        }
        return attractionRates;
    }


    private float[][] balanceTripGeneration (float[][] tripAttr) {
        // Balance trip production and trip attraction

        logger.info("  Balancing trip production and attractions");

        for (int purp = 0; purp < td.getPurposes().length; purp++) {
            float attrSum = 0;
            for (int zone: td.getZones()) {
                attrSum += tripAttr[zone][purp];
            }
            // adjust attractions (or productions for NHBW and NHBO)
            for (int zone: td.getZones()) {
                tripAttr[zone][purp] = tripAttr[zone][purp] * td.getTotalNumberOfTripsGeneratedByPurpose(purp) / attrSum;

                // for NHBW and NHBO, we have more confidence in total production, as it is based on the household
                // travel survey. The distribution, however, is better represented by attraction rates. Therefore,
                // attractions are first scaled to productions (step above) and then productions are replaced with
                // zonal level attractions (step below).
                // todo: fix scaling towards attractions. Difficult, because individual households need to give up trips
                // or add trips to match attractions. Maybe it is alright to rely on productions instead.
//                if (tripPurposes.values()[purp] == tripPurposes.NHBW || tripPurposes.values()[purp] == tripPurposes.NHBO)
//                    tripProd[zone][purp][mstmInc] = tripAttr[zone][purp][mstmInc];
            }
        }
        return tripAttr;
    }


    // Scaling trips is challenging, because individual households would have to add or give up trips. The trip frequency distribution of the survey would need to be scaled up or down.
//    private void scaleTripGeneration() {
//        // scale trip generation to account for underreporting in survey
//
//        logger.info("  Scaling trip production and attraction to account for underreporting in survey");
//        String[] token = ResourceUtil.getArray(rb, "trip.gen.scaler.purpose");
//        double[] scaler = ResourceUtil.getDoubleArray(rb, "trip.gen.scaler.factor");
//        HashMap<String, Double[]> scale = new HashMap<>();
//        for (tripPurposes purp: tripPurposes.values()) scale.put(purp.toString(), new Double[]{0d,0d,0d,0d,0d});
//        for (int i = 0; i < token.length; i++) {
//            String[] tokenParts = token[i].split(Pattern.quote("."));
//            if (tokenParts.length == 2) {
//                // purpose is split by income categories
//                Double[] values = scale.get(tokenParts[0]);
//                values[Integer.parseInt(tokenParts[1]) - 1] = scaler[i];
//            } else {
//                // purpose is not split by income categories
//                Double[] values = scale.get(token[i]);
//                for (int inc = 0; inc < values.length; inc++) values[inc] = scaler[i];
//            }
//        }
//        for (int purp = 0; purp < tripPurposes.values().length; purp++) {
//            Double[] scalingFactors = scale.get(tripPurposes.values()[purp].toString());
//            for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
//                if (scalingFactors[mstmInc-1] == 1) continue;
//                for (int zone: geoData.getZones()) {
//                    tripProd[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
//                    tripAttr[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
//                }
//            }
//        }
//    }


    private void writeTripSummary(float[][] tripAttraction) {
        // write number of trips by purpose and zone to output file

        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(rb.getString("trip.production.output"), false);
        PrintWriter pwAttr = MitoUtil.openFileForSequentialWriting(rb.getString("trip.attraction.output"), false);
        pwProd.print("Zone");
        pwAttr.print("Zone");
        for (String tripPurpose: td.getPurposes()) {
            pwProd.print("," + tripPurpose + "P");
            pwAttr.print("," + tripPurpose + "A");
        }

        float[][] tripProd = new float[td.getZones().length][td.getPurposes().length];
        for (MitoHousehold thh: td.getMitoHouseholds()) {
            for (int purp = 0; purp < td.getPurposes().length; purp++) {
                tripProd[td.getZoneIndex(thh.getHomeZone())][purp] += thh.getNumberOfTrips(purp);
            }
        }

        pwProd.println();
        pwAttr.println();
        for (int zone: td.getZones()) {
            pwProd.print(zone);
            pwAttr.print(zone);
            for (int purp = 0; purp < td.getPurposes().length; purp++) {
                pwProd.print("," + tripProd[td.getZoneIndex(zone)][purp]);
                pwAttr.print("," + tripAttraction[td.getZoneIndex(zone)][purp]);
            }

            pwProd.println();
            pwAttr.println();

        }
        pwProd.close();
        pwAttr.close();
        logger.info("  Wrote out a total of " + MitoUtil.customFormat("###,###.#", MitoUtil.getSum(tripProd)) + " trips.");
    }
}


