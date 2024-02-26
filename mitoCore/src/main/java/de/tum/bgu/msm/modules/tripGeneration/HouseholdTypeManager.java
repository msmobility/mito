package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.readers.GenericCsvReader;
import de.tum.bgu.msm.io.input.readers.GenericCsvReader.GenericCsvTable;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nico
 * @date 20.07.2017
 */
public class HouseholdTypeManager {

    private static final Logger logger = Logger.getLogger(HouseholdTypeManager.class);

    private final Purpose purpose;
    private final Map<HouseholdType, Integer[]> tripFrequency;

    final List<HouseholdType> householdTypes = new ArrayList<>();

    public HouseholdTypeManager(Purpose purpose) {
        this.purpose = purpose;
        createHouseHoldTypeDefinitions();
        tripFrequency = readTripFrequencies();
    }

    public Integer[] getTripFrequenciesForHouseholdType(HouseholdType ht) {
        return tripFrequency.get(ht);
    }

    private Map<HouseholdType, Integer[]> readTripFrequencies() {
        Path filePath = Resources.instance.getTripFrequenciesFilePath(purpose);
        GenericCsvReader csvReader = new GenericCsvReader(filePath);
        csvReader.read();
        GenericCsvTable dataTable = csvReader.getTable();
        Map<HouseholdType, Integer[]> tripFrequency = new HashMap<>();

        for (HouseholdType ht: householdTypes) {
            List<Integer> tripFrequencyThisHousehold = new ArrayList<>();
            boolean foundThisHhType = false;
            for (int row = 0; row < dataTable.getRowCount(); row++) {
                String purpose = dataTable.getString(row, dataTable.getColumnIndexOf("typePurpose"));
                if (!purpose.equals(this.purpose.toString())) {
                    logger.error("File " + filePath + " contains trip purpose " + purpose +
                            ", which is different from expected purpose " + this.purpose);
                    throw new RuntimeException("File " + filePath + " contains trip purpose " + purpose +
                            ", which is different from expected purpose " + this.purpose);
                }

                if (ht.hasTheseAttributes(dataTable.getInt(row, "hhSize_L"),
                        dataTable.getInt(row, "hhSize_H"),
                        dataTable.getInt(row, "workers_L"),
                        dataTable.getInt(row, "workers_H"),
                        dataTable.getInt(row, "econStatus_L"),
                        dataTable.getInt(row, "econStatus_H"),
                        dataTable.getInt(row, "autos_L"),
                        dataTable.getInt(row, "autos_H"),
                        dataTable.getInt(row, "region_L"),
                        dataTable.getInt(row, "region_H"))) {
                    foundThisHhType = true;
                    for (int trips = 0; trips < 100; trips++) {
                        String columnName = "trips_" + trips;
                        if (dataTable.containsColumn(columnName)) {
                            tripFrequencyThisHousehold.add(dataTable.getInt(row, columnName));
                        }
                    }
                    tripFrequency.put(ht, tripFrequencyThisHousehold.toArray(new Integer[]{}));
                }
            }
            if (!foundThisHhType) {
                logger.error("Could not find household type " + ht.getId() + " in file " + filePath);
            }
        }
        return tripFrequency;
    }

    private void createHouseHoldTypeDefinitions() {
        // todo: should this not read the token from the class Properties.java?
        String[] householdDefinitionToken = Resources.instance.getArray("hh.type." + purpose);
        String sizeToken = householdDefinitionToken[1];
        String[] sizePortions = sizeToken.split("\\.");
        String workerToken = householdDefinitionToken[2];
        String[] workerPortions = workerToken.split("\\.");
        String incomeToken = householdDefinitionToken[3];
        String[] economicStatusPortions = incomeToken.split("\\.");
        String autoToken = householdDefinitionToken[4];
        String[] autoPortions = autoToken.split("\\.");
        String regionToken = householdDefinitionToken[5];
        String[] regionPortions = regionToken.split("\\.");

        createHouseholdTypes(sizePortions, workerPortions,
                economicStatusPortions, autoPortions, regionPortions);
    }

    private void createHouseholdTypes(String[] sizePortions, String[] workerPortions,
                                      String[] economicStatusPortions, String[] autoPortions, String[] regionPortions) {
        int id = 0;
        for (String sizeToken : sizePortions) {
            String[] sizeParts = sizeToken.split("-");
            for (String workerToken : workerPortions) {
                String[] workerParts = workerToken.split("-");
                for (String economicStatusToken : economicStatusPortions) {
                    String[] economicStatusParts = economicStatusToken.split("-");
                    for (String autoToken : autoPortions) {
                        String[] autoParts = autoToken.split("-");
                        for (String regionToken : regionPortions) {
                            String[] regionParts = regionToken.split("-");
                            final int sizeL = Integer.parseInt(sizeParts[0]);
                            final int sizeH = Integer.parseInt(sizeParts[1]);
                            final int workersL = Integer.parseInt(workerParts[0]);
                            final int workersH = Integer.parseInt(workerParts[1]);
                            final int economicStatusL = Integer.parseInt(economicStatusParts[0]);
                            final int economicStatusH = Integer.parseInt(economicStatusParts[1]);
                            final int autosL = Integer.parseInt(autoParts[0]);
                            final int autosH = Integer.parseInt(autoParts[1]);
                            final int regionL = Integer.parseInt(regionParts[0]);
                            final int regionH = Integer.parseInt(regionParts[1]);

                            householdTypes.add(new HouseholdType(id, sizeL, sizeH, workersL, workersH,
                                    economicStatusL, economicStatusH, autosL, autosH, regionL,
                                    regionH));
                            id++;
                        }
                    }
                }
            }
        }
    }

    HouseholdType determineHouseholdType(MitoHousehold hh) {
        int areaType = -1;
        if (hh.getHomeZone() != null) {
            areaType = hh.getHomeZone().getAreaTypeSG().code() / 10;
        } else {
            logger.info("Home MitoZone for Household  " + hh.getId() + " is null!");
        }
        return determineHouseholdType(hh.getHhSize(), DataSet.getNumberOfWorkersForHousehold(hh),
                hh.getEconomicStatus(), hh.getAutos(), areaType);
    }

    private HouseholdType determineHouseholdType(int hhSze, int hhWrk, int hhEconStatus, int hhVeh, int hhReg) {

        hhSze = Math.min(hhSze, 8);
        hhWrk = Math.min(hhWrk, 4);
        hhVeh = Math.min(hhVeh, 3);

        for (HouseholdType type : householdTypes) {
            if (type.applies(hhSze, hhWrk, hhEconStatus, hhVeh, hhReg)) {
                return type;
            }
        }
        logger.error("Could not define household type: " + hhSze + " " + hhWrk + " " + hhEconStatus + " " + hhVeh + " " + hhReg);
        return null;
    }
}
