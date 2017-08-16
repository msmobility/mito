package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetCalculatorTest {

    private DataSet dataSet;
    private int[] totalAvailable;


    @Before
    public void setup() {
        totalAvailable = new int[2];
        totalAvailable[0] = 1;
        totalAvailable[1] = 1;

        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);

        dataSet = new DataSet();
        addZone();
        addHouseholds();
    }

    @Test
    public void testTotalTravelTimeBudget() {
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, "Total", dataSet, totalTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(4.562, calculator.calculateTTB(emptyHousehold, totalAvailable), 0);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(4.902, calculator.calculateTTB(poorRetirees, totalAvailable), 0);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(5.507, calculator.calculateTTB(poorBigFamily, totalAvailable), 0);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(5.507, calculator.calculateTTB(richBigFamily, totalAvailable), 0);
    }


    @Test
    public void testHBSTravelTimeBudget() {
        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.HBS.toString(), dataSet, hbsTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(40.755, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(40.995, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(41.142, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(3.355, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testHBOTravelTimeBudget() {
        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.HBO.toString(), dataSet, hboTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(24.645, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(25.011, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(25.489, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(4.518, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testNHBWTravelTimeBudget() {
        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.NHBW.toString(), dataSet, nhbwTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(3.248, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(3.440, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(4.054, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(4.054, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testNHBOTravelTimeBudget() {
        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.NHBO.toString(), dataSet, nhboTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(46.863, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(47.062, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);

        assertEquals(47.602, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(3.754, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
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

    private void addZone() {
        Zone zone = new Zone(1);
        zone.setRegion(1);
        dataSet.getZones().put(zone.getZoneId(), zone);
    }
}
