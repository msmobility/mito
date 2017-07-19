package de.tum.bgu.msm.modules;

import com.pb.common.datafile.TableDataSet;
import com.pb.sawdust.calculator.Function1;
import com.pb.sawdust.util.array.ArrayUtil;
import com.pb.sawdust.util.concurrent.ForkJoinPoolFactory;
import com.pb.sawdust.util.concurrent.IteratorAction;
import de.tum.bgu.msm.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.*;


import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration extends Module{

    private static Logger logger = Logger.getLogger(MitoTravelDemand.class);

    private int counterDroppedTripsAtBorder;

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        generateTrips();
    }

    private void generateTrips () {
        // Run trip generation model

        logger.info("  Started microscopic trip generation model.");
        microgenerateTrips();
        Map<Integer, Map<String, Float>> tripAttr = calculateTripAttractions();
        balanceTripGeneration(tripAttr);
        writeTripSummary(tripAttr);
        SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
        logger.info("  Completed microscopic trip generation model.");
    }


    private String selectAutoMode (String purpose) {
        // return autos or autoSufficiency depending on mode chosen

        String autoMode = "autos";
        if (purpose.equalsIgnoreCase("HBW") || purpose.equalsIgnoreCase("NHBW")) autoMode = "autoSufficiency";
        return autoMode;
    }


    private void microgenerateTrips () {

        counterDroppedTripsAtBorder = 0;

        // Multi-threading code
        Function1<String,Void> tripGenByPurposeMethod = new Function1<String,Void>() {
            public Void apply(String purp) {
                microgenerateTripsByPurpose(purp);
                return null;
            }
        };

        // Generate trips for each purpose
        Iterator<String> tripPurposeIterator = ArrayUtil.getIterator(dataSet.getPurposes());
        IteratorAction<String> itTask = new IteratorAction<>(tripPurposeIterator, tripGenByPurposeMethod);
        ForkJoinPool pool = ForkJoinPoolFactory.getForkJoinPool();
        pool.execute(itTask);
        itTask.waitForCompletion();

        int rawTrips = dataSet.getTripDataManager().getTotalNumberOfTrips() + counterDroppedTripsAtBorder;
        logger.info("  Generated " + MitoUtil.customFormat("###,###", rawTrips) + " raw trips.");
        if (counterDroppedTripsAtBorder > 0)
            logger.info(MitoUtil.customFormat("  " + "###,###", counterDroppedTripsAtBorder) + " trips were dropped at boundary of study area.");
    }


    private void microgenerateTripsByPurpose (String strPurp) {

            logger.info("  Generating trips with purpose " + strPurp + " (multi-threaded)");
            TableDataSet hhTypeDef = createHHTypeDefinition(strPurp);
            int[] hhTypeArray = defineHouseholdTypeOfEachSurveyRecords(selectAutoMode(strPurp), hhTypeDef);
            HashMap<String, Integer[]> tripsByHhTypeAndPurpose = collectTripFrequencyDistribution(hhTypeArray);
            int purposeNum = dataSet.getPurposeIndex(strPurp);
            // Generate trips for each household
        for (MitoHousehold hh: dataSet.getHouseholds().values()) {
                int incCategory = translateIncomeIntoCategory (hh.getIncome());
                int hhType = getHhType(selectAutoMode(strPurp), hhTypeDef, hh.getHhSize(), hh.getNumberOfWorkers(),
                        incCategory, hh.getAutos(), dataSet.getZones().get(hh.getHomeZone()).getRegion());
                String token = hhType + "_" + strPurp;
                Integer[] tripFrequencies = tripsByHhTypeAndPurpose.get(token);
                if (tripFrequencies == null) {
                    logger.error("Could not find trip frequencies for this hhType/Purpose: " + token);
                }
                if (MitoUtil.getSum(tripFrequencies) == 0) continue;
                int numTrips = selectNumberOfTrips(tripFrequencies);
                for (int i = 0; i < numTrips; i++) {
                    // todo: for non-home based trips, do not set origin as home
                    int tripOrigin = hh.getHomeZone();
                    boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(tripOrigin);
                    if (dropThisTrip) counterDroppedTripsAtBorder++;
                    if (dropThisTrip) continue;
                    synchronized (MitoHousehold.class) {
                        MitoTrip trip = new MitoTrip(TripDataManager.getNextTripId(), hh.getHhId(), purposeNum, tripOrigin);
                        dataSet.getTripDataManager().addTrip(trip);
                        hh.addTrip(trip);
                    }
                }
            }
    }


    private TableDataSet createHHTypeDefinition (String purpose) {
        // create household type definition file
        String[] hhDefToken = Resources.INSTANCE.getArray("hh.type." + purpose);
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
        int[] hhCounter = defineHouseholdTypeOfEachSurveyRecords(selectAutoMode(purpose), hhTypeDef);
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


    private TableDataSet createHouseholdTypeTableDataSet (int numCategories, String[] sizePortions, String[] workerPortions,
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


    private int selectNumberOfTrips (Integer[] tripFrequencies) {
        // select number of trips
        double[] probabilities = new double[tripFrequencies.length];
        for (int i = 0; i < tripFrequencies.length; i++) probabilities[i] = (double) tripFrequencies[i];
        return MitoUtil.select(MitoUtil.getRand(), probabilities);
    }


    private boolean reduceTripGenAtStudyAreaBorder(int tripOrigin) {
        // as trips near border of study area that travel to destinations outside of study area are not represented,
        // trip generation near border of study area can be reduced artificially with this method

        if (!Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            return false;
        }

        float damper = dataSet.getZones().get(tripOrigin).getReductionAtBorderDamper();
        return MitoUtil.getRand().nextFloat() < damper;
    }


    private Map<Integer, Map<String, Float>> calculateTripAttractions () {
        // calculate zonal trip attractions

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


    private Map<Integer, Map<String, Float>> balanceTripGeneration (Map<Integer, Map<String, Float>> tripAttr) {
        // Balance trip production and trip attraction

        logger.info("  Balancing trip production and attractions");

        for (int purp = 0; purp < dataSet.getPurposes().length; purp++) {
            int tripsByPurp = dataSet.getTripDataManager().getTotalNumberOfTripsGeneratedByPurpose(purp);
            float attrSum = 0;
            String purpose = dataSet.getPurposes()[purp];
            for (Zone zone: dataSet.getZones().values()) {
                try {
                    attrSum += tripAttr.get(zone.getZoneId()).get(purpose);
                } catch (Exception e) {
                    logger.error(e.getMessage());
//                    System.exit(-1);
                }
            }
            if (attrSum == 0) {
                logger.warn("No trips for purpose " + dataSet.getPurposes()[purp] + " were generated.");
                continue;
            }
            // adjust attractions (or productions for NHBW and NHBO)
            for (Zone zone: dataSet.getZones().values()) {
                final float attrSumFinal = attrSum;
                tripAttr.get(zone.getZoneId()).replaceAll((k,v) -> v * tripsByPurp / attrSumFinal);

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


    private void writeTripSummary(Map<Integer, Map<String, Float>> tripAttractionByZoneAndPurp) {
        // write number of trips by purpose and zone to output file

        String fileNameProd = MitoUtil.generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_PRODUCTION_OUTPUT));
        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(fileNameProd, false);
        String fileNameAttr = MitoUtil.generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_ATTRACTION_OUTPUT));
        PrintWriter pwAttr = MitoUtil.openFileForSequentialWriting(fileNameAttr, false);
        pwProd.print("Zone");
        pwAttr.print("Zone");
        for (String tripPurpose: dataSet.getPurposes()) {
            pwProd.print("," + tripPurpose + "P");
            pwAttr.print("," + tripPurpose + "A");
        }

        Map<Integer, Map<String, Integer>> tripProdByZoneAndPurp = new HashMap<>();

        for(Integer zoneId: dataSet.getZones().keySet()) {
            Map<String, Integer> initialValues = new HashMap<>();
            for(String purp: dataSet.getPurposes()) {
                initialValues.put(purp, 0);
            }
            tripProdByZoneAndPurp.put(zoneId, initialValues);
        }

        for (MitoTrip trip: dataSet.getTripDataManager().getTrips().values()) {
            String purp = dataSet.getPurposes()[trip.getTripPurpose()];
            int number = tripProdByZoneAndPurp.get(trip.getTripOrigin()).get(purp);
            tripProdByZoneAndPurp.get(trip.getTripOrigin()).replace(purp, (number + 1));
        }

        int totalTrips = 0;
        pwProd.println();
        pwAttr.println();
        for (int zoneId: dataSet.getZones().keySet()) {
            pwProd.print(zoneId);
            pwAttr.print(zoneId);
            for (String purp: dataSet.getPurposes()) {
                int tripProdTmp = tripProdByZoneAndPurp.get(zoneId).get(purp);
                totalTrips += tripProdTmp;
                pwProd.print("," + tripProdTmp);
                pwAttr.print("," + tripAttractionByZoneAndPurp.get(zoneId).get(purp));
            }
            pwProd.println();
            pwAttr.println();
        }
        pwProd.close();
        pwAttr.close();
        logger.info("  Wrote out " + MitoUtil.customFormat("###,###", totalTrips)
                + " aggregate trips balanced against attractions.");
    }

    public int[] defineHouseholdTypeOfEachSurveyRecords(String autoDef, TableDataSet hhTypeDef) {
        // Count number of household records per predefined typ

        int[] hhTypeCounter = new int[MitoUtil.getHighestVal(hhTypeDef.getColumnAsInt("hhType")) + 1];

        TableDataSet travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        int[] hhTypeArray = new int[travelSurveyHouseholdTable.getRowCount() + 1];

        for (int row = 1; row <= travelSurveyHouseholdTable.getRowCount(); row++) {
            int hhSze = (int) travelSurveyHouseholdTable.getValueAt(row, "hhsiz");
            hhSze = Math.min(hhSze, 7);    // hhsiz 8 has only 19 records, aggregate with hhsiz 7
            int hhWrk = (int) travelSurveyHouseholdTable.getValueAt(row, "hhwrk");
            hhWrk = Math.min(hhWrk, 4);    // hhwrk 6 has 1 and hhwrk 5 has 7 records, aggregate with hhwrk 4
            int hhInc = (int) travelSurveyHouseholdTable.getValueAt(row, "incom");
            int hhVeh = (int) travelSurveyHouseholdTable.getValueAt(row, "hhveh");
            hhVeh = Math.min (hhVeh, 3);   // Auto-ownership model will generate groups 0, 1, 2, 3+ only.
            int region = (int) travelSurveyHouseholdTable.getValueAt(row, "urbanSuburbanRural");

            int hhTypeId = getHhType(autoDef, hhTypeDef, hhSze, hhWrk, hhInc, hhVeh, region);
            hhTypeArray[row] = hhTypeId;
            hhTypeCounter[hhTypeId]++;
        }
        // analyze if every household type has a sufficient number of records
        for (int hht = 1; hht < hhTypeCounter.length; hht++) {
            if (hhTypeCounter[hht] < 30) hhTypeArray[0] = -1;  // marker that this hhTypeDef is not worth analyzing
        }
        return hhTypeArray;
    }

    public HashMap<String, Integer[]> collectTripFrequencyDistribution (int[] hhTypeArray) {
        // Summarize frequency of number of trips for each household type by each trip purpose
        //
        // Storage Structure
        //   HashMap<String, Integer> tripsByHhTypeAndPurpose: Token is hhType_TripPurpose
        //   |
        //   contains -> Integer[] tripFrequencyList: Frequency of 0, 1, 2, 3, ... trips

        HashMap<String, Integer[]> tripsByHhTypeAndPurpose = new HashMap<>();  // contains trips by hhtype and purpose

        for (int hhType = 1; hhType < hhTypeArray.length; hhType++) {
            for (String purp: dataSet.getPurposes()) {
                String token = String.valueOf(hhType) + "_" + purp;
                // fill Storage structure from bottom       0                  10                  20                  30
                Integer[] tripFrequencyList = new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};  // space for up to 30 trips
                tripsByHhTypeAndPurpose.put(token, tripFrequencyList);
            }
        }

        // Read through household file of HTS
        int pos = 1;
        TableDataSet travelSurveyHouseholdTable = dataSet.getTravelSurveyHouseholdTable();
        for (int hhRow = 1; hhRow <= travelSurveyHouseholdTable.getRowCount(); hhRow++) {
            int sampleId = (int) travelSurveyHouseholdTable.getValueAt(hhRow, "sampn");
            int hhType = hhTypeArray[hhRow];
            int[] tripsOfThisHouseholdByPurposes = new int[dataSet.getPurposes().length];
            // Ready through trip file of HTS
            TableDataSet travelSurveyTripsDable = dataSet.getTravelSurveyTripsTable();
            for (int trRow = pos; trRow <= travelSurveyTripsDable.getRowCount(); trRow++) {
                if ((int) travelSurveyTripsDable.getValueAt(trRow, "sampn") == sampleId) {

                    // add this trip to this household
                    pos++;
                    String htsTripPurpose = travelSurveyTripsDable.getStringValueAt(trRow, "mainPurpose");
                    tripsOfThisHouseholdByPurposes[MitoUtil.findPositionInArray(htsTripPurpose, dataSet.getPurposes())]++;
                } else {
                    // This trip record does not belong to this household
                    break;
                }
            }
            for (int p = 0; p < dataSet.getPurposes().length; p++) {
                String token = String.valueOf(hhType) + "_" + dataSet.getPurposes()[p];
                Integer[] tripsOfThisHouseholdType = tripsByHhTypeAndPurpose.get(token);
                int count = tripsOfThisHouseholdByPurposes[p];
                tripsOfThisHouseholdType[count]++;
                tripsByHhTypeAndPurpose.put(token, tripsOfThisHouseholdType);
            }
        }
        return tripsByHhTypeAndPurpose;
    }

    public int getHhType (String autoDef, TableDataSet hhTypeDef, int hhSze, int hhWrk, int hhInc, int hhVeh, int hhReg) {
        // Define household type

        hhSze = Math.min (hhSze, 7);
        hhWrk = Math.min (hhWrk, 4);
        int hhAut;
        if (autoDef.equalsIgnoreCase("autos")) {
            hhAut = Math.min(hhVeh, 3);
        } else {
            if (hhVeh < hhWrk) {
                hhAut = 0;        // fewer autos than workers
            }
            else if (hhVeh == hhWrk) {
                hhAut = 1;  // equal number of autos and workers
            } else {
                hhAut = 2;                      // more autos than workers
            }
        }
        for (int hhType = 1; hhType <= hhTypeDef.getRowCount(); hhType++) {
            if (hhSze >= hhTypeDef.getIndexedValueAt(hhType, "size_l") &&          // Household size
                    hhSze <= hhTypeDef.getIndexedValueAt(hhType, "size_h") &&
                    hhWrk >= hhTypeDef.getIndexedValueAt(hhType, "workers_l") &&   // Number of workers
                    hhWrk <= hhTypeDef.getIndexedValueAt(hhType, "workers_h") &&
                    hhInc >= hhTypeDef.getIndexedValueAt(hhType, "income_l") &&    // Household income
                    hhInc <= hhTypeDef.getIndexedValueAt(hhType, "income_h") &&
                    hhAut >= hhTypeDef.getIndexedValueAt(hhType, "autos_l") &&     // Number of vehicles
                    hhAut <= hhTypeDef.getIndexedValueAt(hhType, "autos_h") &&
                    hhReg >= hhTypeDef.getIndexedValueAt(hhType, "region_l") &&    // Region (urban, suburban, rural)
                    hhReg <= hhTypeDef.getIndexedValueAt(hhType, "region_h")) {
                return (int) hhTypeDef.getIndexedValueAt(hhType, "hhType");
            }
        }
        logger.error ("Could not define household type: " + hhSze + " " + hhWrk + " " + hhInc + " " + hhVeh + " " + hhReg);
        for (int hhType = 1; hhType <= hhTypeDef.getRowCount(); hhType++) {
            logger.error(hhType+": "+hhTypeDef.getIndexedValueAt(hhType, "size_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "size_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "workers_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "workers_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "income_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "income_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "autos_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "autos_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "region_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "region_h"));
        }
        return -1;
    }
}


