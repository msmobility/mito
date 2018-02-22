package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.survey.SurveyRecord;
import de.tum.bgu.msm.data.survey.TravelSurvey;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nico on 20.07.2017.
 */
public class HouseholdTypeManager {

    private static final Logger logger = Logger.getLogger(HouseholdTypeManager.class);

    private final Purpose purpose;

    final List<HouseholdType> householdTypes = new ArrayList<>();

    public HouseholdTypeManager(Purpose purpose) {
        this.purpose = purpose;
    }

    public void createHouseHoldTypeDefinitions() {
        String[] householdDefinitionToken = Resources.INSTANCE.getArray("hh.type." + purpose);
        String sizeToken = householdDefinitionToken[2];
        String[] sizePortions = sizeToken.split("\\.");
        String workerToken = householdDefinitionToken[3];
        String[] workerPortions = workerToken.split("\\.");
        String incomeToken = householdDefinitionToken[4];
        String[] incomePortions = incomeToken.split("\\.");
        String autoToken = householdDefinitionToken[5];
        String[] autoPortions = autoToken.split("\\.");
        String regionToken = householdDefinitionToken[6];
        String[] regionPortions = regionToken.split("\\.");

        createHouseholdTypes(sizePortions, workerPortions,
                incomePortions, autoPortions, regionPortions);
    }

    private void createHouseholdTypes(String[] sizePortions, String[] workerPortions,
                                      String[] incomePortions, String[] autoPortions, String[] regionPortions) {
        int id = 0;
        for (String sizeToken : sizePortions) {
            String[] sizeParts = sizeToken.split("-");
            for (String workerToken : workerPortions) {
                String[] workerParts = workerToken.split("-");
                for (String incomeToken : incomePortions) {
                    String[] incomeParts = incomeToken.split("-");
                    for (String autoToken : autoPortions) {
                        String[] autoParts = autoToken.split("-");
                        for (String regionToken : regionPortions) {
                            String[] regionParts = regionToken.split("-");
                            final int sizeL = Integer.parseInt(sizeParts[0]);
                            final int sizeH = Integer.parseInt(sizeParts[1]);
                            final int workersL = Integer.parseInt(workerParts[0]) - 1;
                            final int workersH = Integer.parseInt(workerParts[1]) - 1;
                            final int incomeL = Integer.parseInt(incomeParts[0]);
                            final int incomeH = Integer.parseInt(incomeParts[1]);
                            final int autosL = Integer.parseInt(autoParts[0]) - 1;
                            final int autosH = Integer.parseInt(autoParts[1]) - 1;
                            final int regionL = Integer.parseInt(regionParts[0]);
                            final int regionH = Integer.parseInt(regionParts[1]);

                            householdTypes.add(new HouseholdType(id, sizeL, sizeH, workersL, workersH,
                                    incomeL, incomeH, autosL, autosH, regionL,
                                    regionH));
                            id++;
                        }
                    }
                }
            }
        }
    }

    public Map<Integer, HouseholdType> assignHouseholdTypeOfEachSurveyRecord(TravelSurvey<? extends SurveyRecord> survey) {
        // Count number of household records per predefined type

        Map<Integer, HouseholdType> householdTypeBySample = new HashMap<>();

        for (SurveyRecord record: survey.getRecords().values()) {
            int hhSze = record.getHouseholdSize();
            hhSze = Math.min(hhSze, 7);    // hhsiz 8 has only 19 records, aggregate with hhsiz 7
            int hhWrk = record.getWorkers();
            hhWrk = Math.min(hhWrk, 4);    // hhwrk 6 has 1 and hhwrk 5 has 7 records, aggregate with hhwrk 4
            int hhInc = record.getIncome();
            int hhVeh = record.getVehicleNumber();
            hhVeh = Math.min(hhVeh, 3);   // Auto-ownership model will generate groups 0, 1, 2, 3+ only.
            int region = record.getRegion();
            int sampleId = record.getId();
            HouseholdType type = determineHouseholdType(hhSze, hhWrk, hhInc, hhVeh, region);
            householdTypeBySample.put(sampleId, type);

        }
        // analyze if every household type has a sufficient number of records
        cancelOutInsufficientRecords(householdTypeBySample);
        return householdTypeBySample;
    }

    public HouseholdType determineHouseholdType(MitoHousehold hh) {
        int incCategory = translateIncomeIntoCategory(hh.getIncome());
        int areaType = -1;
        if (hh.getHomeZone() != null) {
            areaType = hh.getHomeZone().getAreaType().ordinal()+1;
        } else {
            logger.info("Home MitoZone for Household  " + hh.getId() + " is null!");
        }
        return determineHouseholdType(hh.getHhSize(), DataSet.getNumberOfWorkersForHousehold(hh),
                incCategory, hh.getAutos(), areaType);
    }

    private HouseholdType determineHouseholdType(int hhSze, int hhWrk, int hhInc, int hhVeh, int hhReg) {

        hhSze = Math.min(hhSze, 7);
        hhWrk = Math.min(hhWrk, 4);

        int hhAut;
        String autoDef = selectAutoMode();

        if ("autos".equalsIgnoreCase(autoDef)) {
            hhAut = Math.min(hhVeh, 3);
        } else {
            if (hhVeh < hhWrk) {
                hhAut = 0;        // fewer autos than workers
            } else if (hhVeh == hhWrk) {
                hhAut = 1;  // equal number of autos and workers
            } else {
                hhAut = 2;                      // more autos than workers
            }
        }

        for (HouseholdType type : householdTypes) {
            if (type.applies(hhSze, hhWrk, hhInc, hhAut, hhReg)) {
                return type;
            }
        }
        logger.error("Could not define household type: " + hhSze + " " + hhWrk + " " + hhInc + " " + hhVeh + " " + hhReg);
        return null;
    }

    private String selectAutoMode() {
        // return autos or autoSufficiency depending on mode chosen
        String autoMode = "autos";
        if (purpose.equals(Purpose.HBW) || purpose.equals(Purpose.NHBW)) {
            autoMode = "autoSufficiency";
        }
        return autoMode;
    }

    private void cancelOutInsufficientRecords(Map<Integer, HouseholdType> householdTypeBySample) {
        for (Map.Entry<Integer, HouseholdType> entry : householdTypeBySample.entrySet()) {
            if (entry.getValue().getNumberOfRecords() < 30) {
                householdTypeBySample.remove(entry.getKey());// marker that this hhTypeDef is not worth analyzing
                logger.info("HouseholdType " +  entry.getValue().getId() + "_" + purpose + " does not meet min requirement of 30 records. " +
                        "Will not consider this type when creating trips.");
            }
        }
    }

    private int translateIncomeIntoCategory(int hhIncome) {
        // translate income in absolute dollars into household travel survey income categories

        if (hhIncome < 10000) {
            return 1;
        } else if (hhIncome < 15000) {
            return 2;
        } else if (hhIncome < 30000) {
            return 3;
        } else if (hhIncome < 40000) {
            return 4;
        } else if (hhIncome < 50000) {
            return 5;
        } else if (hhIncome < 60000) {
            return 6;
        } else if (hhIncome < 75000) {
            return 7;
        } else if (hhIncome < 100000) {
            return 8;
        } else if (hhIncome < 125000) {
            return 9;
        } else if (hhIncome < 150000) {
            return 10;
        } else if (hhIncome < 200000) {
            return 11;
        } else {
            return 12;
        }
    }
}
