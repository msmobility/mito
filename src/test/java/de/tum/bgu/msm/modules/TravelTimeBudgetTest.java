package de.tum.bgu.msm.modules;

import com.pb.common.calculator2.UtilityExpressionCalculator;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ResourceBundle;

public class TravelTimeBudgetTest {

    private DataSet dataSet;
    private String uecFileName;
    private int dataSheetNumber;
    private int[] totalAvailable;


    @Before
    public void setup() {
        totalAvailable = new int[2];
        totalAvailable[0] = 1;
        totalAvailable[1] = 1;

        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
        uecFileName = Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_UEC_FILE);
        dataSheetNumber = Resources.INSTANCE.getInt(Properties.TRAVEL_TIME_BUDGET_UEC_DATA_SHEET);

        dataSet = new DataSet();
        addZone();
        addPurposes();
        addHouseholds();
    }

    @Test
    public void testTotalTravelTimeBudget() {
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetDMU dmu = new TravelTimeBudgetDMU();
        UtilityExpressionCalculator totalTtbUec = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, totalTtbSheetNumber, dataSheetNumber, dmu);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(totalTtbUec, true, "Total", dataSet);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        setupDMU(emptyHousehold, dmu);
        assertEquals(4.562, calculator.calculateTTB(emptyHousehold, dmu, totalAvailable), 0);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        setupDMU(poorRetirees, dmu);
        assertEquals(4.902, calculator.calculateTTB(poorRetirees, dmu, totalAvailable), 0);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        setupDMU(poorBigFamily, dmu);
        assertEquals(5.507, calculator.calculateTTB(poorBigFamily, dmu, totalAvailable), 0);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        setupDMU(richBigFamily, dmu);
        assertEquals(5.507, calculator.calculateTTB(richBigFamily, dmu, totalAvailable), 0);
    }


    @Test
    public void testHBSTravelTimeBudget() {
        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetDMU dmu = new TravelTimeBudgetDMU();
        UtilityExpressionCalculator hbsTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, hbsTtbSheetNumber, dataSheetNumber, dmu);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(hbsTtbUtility, true, "HBS", dataSet);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        setupDMU(emptyHousehold, dmu);
        assertEquals(40.755, calculator.calculateTTB(emptyHousehold, dmu, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        setupDMU(poorRetirees, dmu);
        assertEquals(40.995, calculator.calculateTTB(poorRetirees, dmu, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        setupDMU(poorBigFamily, dmu);
        assertEquals(41.142, calculator.calculateTTB(poorBigFamily, dmu, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        setupDMU(richBigFamily, dmu);
        assertEquals(3.355, calculator.calculateTTB(richBigFamily, dmu, totalAvailable), 0.001);
    }

    @Test
    public void testHBOTravelTimeBudget() {
        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetDMU dmu = new TravelTimeBudgetDMU();
        UtilityExpressionCalculator hboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, hboTtbSheetNumber, dataSheetNumber, dmu);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(hboTtbUtility, true, "HBO", dataSet);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        setupDMU(emptyHousehold, dmu);
        assertEquals(24.645, calculator.calculateTTB(emptyHousehold, dmu, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        setupDMU(poorRetirees, dmu);
        assertEquals(25.011, calculator.calculateTTB(poorRetirees, dmu, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        setupDMU(poorBigFamily, dmu);
        assertEquals(25.489, calculator.calculateTTB(poorBigFamily, dmu, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        setupDMU(richBigFamily, dmu);
        assertEquals(4.518, calculator.calculateTTB(richBigFamily, dmu, totalAvailable), 0.001);
    }

    @Test
    public void testNHBWTravelTimeBudget() {
        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetDMU dmu = new TravelTimeBudgetDMU();
        UtilityExpressionCalculator nhbwTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, nhbwTtbSheetNumber, dataSheetNumber, dmu);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(nhbwTtbUtility, true, "NHBW", dataSet);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        setupDMU(emptyHousehold, dmu);
        assertEquals(3.248, calculator.calculateTTB(emptyHousehold, dmu, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        setupDMU(poorRetirees, dmu);
        assertEquals(3.440, calculator.calculateTTB(poorRetirees, dmu, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        setupDMU(poorBigFamily, dmu);
        assertEquals(4.054, calculator.calculateTTB(poorBigFamily, dmu, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        setupDMU(richBigFamily, dmu);
        assertEquals(4.054, calculator.calculateTTB(richBigFamily, dmu, totalAvailable), 0.001);
    }

    @Test
    public void testNHBOTravelTimeBudget() {
        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetDMU dmu = new TravelTimeBudgetDMU();
        UtilityExpressionCalculator nhboTtbUtility = Resources.INSTANCE.getUtilityExpressionCalculator2(uecFileName, nhboTtbSheetNumber, dataSheetNumber, dmu);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(nhboTtbUtility, true, "NHBO", dataSet);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        setupDMU(emptyHousehold, dmu);
        assertEquals(46.863, calculator.calculateTTB(emptyHousehold, dmu, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        setupDMU(poorRetirees, dmu);
        assertEquals(47.062, calculator.calculateTTB(poorRetirees, dmu, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        setupDMU(poorBigFamily, dmu);
        assertEquals(47.602, calculator.calculateTTB(poorBigFamily, dmu, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        setupDMU(richBigFamily, dmu);
        assertEquals(3.754, calculator.calculateTTB(richBigFamily, dmu, totalAvailable), 0.001);
    }

    private void addHouseholds() {
        MitoHousehold emptyHousehold = new MitoHousehold(1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1);
        dataSet.getHouseholds().put(emptyHousehold.getHhId(), emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2, 2, 1, 0, 0, 2, 0, 0, 0, 1, 0, 1);
        dataSet.getHouseholds().put(poorRetirees.getHhId(), poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3, 5, 3, 2, 1, 0, 3, 2, 2, 1, 0, 1);
        dataSet.getHouseholds().put(poorBigFamily.getHhId(), poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4, 5, 3, 2, 1, 0, 3, 2, 2, 500000, 0, 1);
        dataSet.getHouseholds().put(richBigFamily.getHhId(), richBigFamily);
    }

    private void addPurposes() {
        String[] purposes = {"HBW", "HBE", "HBS", "HBO", "NHBW", "NHBO"};
        dataSet.setPurposes(purposes);
    }

    private void addZone() {
        Zone zone = new Zone(1);
        zone.setRegion(1);
        dataSet.getZones().put(zone.getZoneId(), zone);
    }

    private void setupDMU(MitoHousehold hh, TravelTimeBudgetDMU ttbDMU) {
        // set DMU attributes
        ttbDMU.setHouseholdSize(hh.getHhSize());
        ttbDMU.setFemales(hh.getFemales());
        ttbDMU.setChildren(hh.getChildren());
        ttbDMU.setYoungAdults(hh.getYoungAdults());
        ttbDMU.setRetirees(hh.getRetirees());
        ttbDMU.setWorkers(hh.getNumberOfWorkers());
        ttbDMU.setStudents(hh.getStudents());
        ttbDMU.setCars(hh.getAutos());
        ttbDMU.setLicenseHolders(hh.getLicenseHolders());
        ttbDMU.setIncome(hh.getIncome());
        ttbDMU.setAreaType(dataSet.getZones().get(hh.getHomeZone()).getRegion());
    }
}
