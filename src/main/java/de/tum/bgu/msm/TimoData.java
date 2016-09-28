package de.tum.bgu.msm;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Holds data for the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TimoData {

    private static Logger logger = Logger.getLogger(TimoData.class);
    private ResourceBundle rb;
    private int[] zones;
    private int[] zoneIndex;
    private float[] sizeOfZonesInAcre;
    private TimoHousehold[] timoHouseholds;
    private int[] householdsByZone;
    private int[] retailEmplByZone;
    private int[] officeEmplByZone;
    private int[] otherEmplByZone;
    private int[] totalEmplByZone;
    private int[] schoolEnrollmentByZone;
    private Matrix autoTravelTimes;
    private Matrix transitTravelTimes;
    private TableDataSet htsHH;
    private TableDataSet htsTR;
    private String[] purposes;
    private Random rand;


    public TimoData(ResourceBundle rb) {
        this.rb = rb;
        initializeRandomNumber();
    }


    private void initializeRandomNumber() {
        // initialize random number generator
        int seed = ResourceUtil.getIntegerProperty(rb, "random.seed");
        if (seed == -1)
            rand = new Random();
        else
            rand = new Random(seed);
    }


    public Random getRand () {
        return rand;
    }


    public void setZones(int[] zones) {
        this.zones = zones;
        zoneIndex = TimoUtil.createIndexArray(zones);
    }

    public int[] getZones() {
        return zones;
    }

    public int getZoneIndex(int zone) {
        return zoneIndex[zone];
    }

    public void setAutoTravelTimes (Matrix autoTravelTimes) {
        this.autoTravelTimes = autoTravelTimes;
    }

    public float getAutoTravelTimes(int origin, int destination) {
        return autoTravelTimes.getValueAt(origin, destination);
    }

    public void setTransitTravelTimes (Matrix transitTravelTimes) {
        this.transitTravelTimes = transitTravelTimes;
    }

    public float getTransitTravelTimes(int origin, int destination) {
        return transitTravelTimes.getValueAt(origin, destination);
    }

    public void setHouseholds(TimoHousehold[] timoHouseholds) {
        // store households in memory as TimoHousehold objects
        this.timoHouseholds = timoHouseholds;
        // fill householdsByZone array
        householdsByZone = new int[getZones().length];
        for (TimoHousehold thh: timoHouseholds) householdsByZone[getZoneIndex(thh.getHomeZone())]++;
    }

    public TimoHousehold[] getTimoHouseholds () {
        return timoHouseholds;
    }

    public int getHouseholdsByZone (int zone) {
        return householdsByZone[getZoneIndex(zone)];
    }

    public void readInputData() {
        // read all required input data
        readHouseholdTravelSurvey();
        purposes = ResourceUtil.getArray(rb, "trip.purposes");
        // create placeholder for number of trips by purpose for every household
        for (TimoHousehold thh: timoHouseholds) thh.createTripByPurposeArray(purposes.length);
    }

    public void setRetailEmplByZone(int[] retailEmplByZone) {
        this.retailEmplByZone = retailEmplByZone;
    }

    public int getRetailEmplByZone (int zone) {
        return retailEmplByZone[zone];
    }

    public void setOfficeEmplByZone(int[] officeEmplByZone) {
        this.officeEmplByZone = officeEmplByZone;
    }

    public int getOfficeEmplByZone(int zone) {
        return officeEmplByZone[zone];
    }

    public void setOtherEmplByZone(int[] otherEmplByZone) {
        this.otherEmplByZone = otherEmplByZone;
    }

    public int getOtherEmplByZone (int zone) {
        return otherEmplByZone[zone];
    }

    public void setTotalEmplByZone(int[] totalEmplByZone) {
        this.totalEmplByZone = totalEmplByZone;
    }

    public int getTotalEmplByZone (int zone) {
        return totalEmplByZone[zone];
    }

    public void setSchoolEnrollmentByZone(int[] schoolEnrollmentByZone) {
        this.schoolEnrollmentByZone = schoolEnrollmentByZone;
    }

    public int getSchoolEnrollmentByZone (int zone) {
        return schoolEnrollmentByZone[zone];
    }

    public void setSizeOfZonesInAcre(float[] sizeOfZonesInAcre) {
        this.sizeOfZonesInAcre = sizeOfZonesInAcre;
    }

    public float getSizeOfZoneInAcre (int zone) {
        return sizeOfZonesInAcre[zone];
    }

    private void readHouseholdTravelSurvey() {
        // read household travel survey

        logger.info("  Reading household travel survey");
        htsHH = TimoUtil.readCSVfile(rb.getString("household.travel.survey.hh"));
        htsTR = TimoUtil.readCSVfile(rb.getString("household.travel.survey.trips"));
    }


    public String[] getPurposes () {
        return purposes;
    }


    public int[] defineHouseholdTypeOfEachSurveyRecords(String autoDef, TableDataSet hhTypeDef) {
        // Count number of household records per predefined typ

        int[] hhTypeCounter = new int[TimoUtil.getHighestVal(hhTypeDef.getColumnAsInt("hhType")) + 1];
        int[] hhTypeArray = new int[htsHH.getRowCount() + 1];

        for (int row = 1; row <= htsHH.getRowCount(); row++) {
            int hhSze = (int) htsHH.getValueAt(row, "hhsiz");
            hhSze = Math.min(hhSze, 7);    // hhsiz 8 has only 19 records, aggregate with hhsiz 7
            int hhWrk = (int) htsHH.getValueAt(row, "hhwrk");
            hhWrk = Math.min(hhWrk, 4);    // hhwrk 6 has 1 and hhwrk 5 has 7 records, aggregate with hhwrk 4
            int hhInc = (int) htsHH.getValueAt(row, "incom");
            int hhVeh = (int) htsHH.getValueAt(row, "hhveh");
            hhVeh = Math.min (hhVeh, 3);   // Auto-ownership model will generate groups 0, 1, 2, 3+ only.
            int region = (int) htsHH.getValueAt(row, "urbanSuburbanRural");

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


    public int getHhType (String autoDef, TableDataSet hhTypeDef, int hhSze, int hhWrk, int hhInc, int hhVeh, int hhReg) {
        // Define household type

        hhSze = Math.min (hhSze, 7);
        hhWrk = Math.min (hhWrk, 4);
        int hhAut;
        if (autoDef.equalsIgnoreCase("autos")) {
            hhAut = Math.min(hhVeh, 3);
        } else {
            if (hhVeh < hhWrk) hhAut = 0;        // fewer autos than workers
            else if (hhVeh == hhWrk) hhAut = 1;  // equal number of autos and workers
            else hhAut = 2;                      // more autos than workers
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
            System.out.println(hhType+": "+hhTypeDef.getIndexedValueAt(hhType, "size_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "size_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "workers_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "workers_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "income_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "income_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "autos_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "autos_h")
                    +","+hhTypeDef.getIndexedValueAt(hhType, "region_l")+"-"+hhTypeDef.getIndexedValueAt(hhType, "region_h"));
        }
        return -1;
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
            for (String purp: purposes) {
                String token = String.valueOf(hhType) + "_" + purp;
                // fill Storage structure from bottom       0                  10                  20                  30
                Integer[] tripFrequencyList = new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};  // space for up to 30 trips
                tripsByHhTypeAndPurpose.put(token, tripFrequencyList);
            }
        }

        // Read through household file of HTS
        int pos = 1;
        for (int hhRow = 1; hhRow <= htsHH.getRowCount(); hhRow++) {
            int sampleId = (int) htsHH.getValueAt(hhRow, "sampn");
            int hhType = hhTypeArray[hhRow];
            int[] tripsOfThisHouseholdByPurposes = new int[purposes.length];
            // Ready through trip file of HTS
            for (int trRow = pos; trRow <= htsTR.getRowCount(); trRow++) {
                if ((int) htsTR.getValueAt(trRow, "sampn") == sampleId) {

                    // add this trip to this household
                    pos++;
                    String htsTripPurpose = htsTR.getStringValueAt(trRow, "mainPurpose");
                    tripsOfThisHouseholdByPurposes[TimoUtil.findPositionInArray(htsTripPurpose, purposes)]++;
                } else {
                    // This trip record does not belong to this household
                    break;
                }
            }
            for (int p = 0; p < purposes.length; p++) {
                String token = String.valueOf(hhType) + "_" + purposes[p];
                Integer[] tripsOfThisHouseholdType = tripsByHhTypeAndPurpose.get(token);
                int count = tripsOfThisHouseholdByPurposes[p];
                tripsOfThisHouseholdType[count]++;
                tripsByHhTypeAndPurpose.put(token, tripsOfThisHouseholdType);
            }
        }
        return tripsByHhTypeAndPurpose;
    }


    public int getTotalNumberOfTripsGeneratedByPurpose (int purpose) {
        // sum up trips generated by purpose

        int prodSum = 0;
        for (TimoHousehold thh: getTimoHouseholds()) {
            prodSum += thh.getNumberOfTrips(purpose);
        }
        return prodSum;
    }

}
