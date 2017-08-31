package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.resources.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetCalculatorTest {

    private DataSet dataSet;
    private int[] totalAvailable;
    private Zone dummyZone;

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
        addPersons();
    }

    @Test
    public void testTotalTravelTimeBudget() {
        int totalTtbSheetNumber = Resources.INSTANCE.getInt(Properties.TOTAL_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, "Total", dataSet, totalTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(50.121, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(70.418, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(128.953, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(128.953, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }


    @Test
    public void testHBSTravelTimeBudget() {
        int hbsTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBS_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.HBS.toString(), dataSet, hbsTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(16.586, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(21.085, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(24.424, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(17.649, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testHBOTravelTimeBudget() {
        int hboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.HBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.HBO.toString(), dataSet, hboTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(30.005, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(43.267, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(69.783, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(58.270, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testNHBWTravelTimeBudget() {
        int nhbwTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBW_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.NHBW.toString(), dataSet, nhbwTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(15.481, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(18.758, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(34.662, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(34.662, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    @Test
    public void testNHBOTravelTimeBudget() {
        int nhboTtbSheetNumber = Resources.INSTANCE.getInt(Properties.NHBO_TRAVEL_TIME_BUDGET_UEC_UTILITY);
        TravelTimeBudgetCalculator calculator = new TravelTimeBudgetCalculator(true, Purpose.NHBO.toString(), dataSet, nhboTtbSheetNumber);

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(17.881, calculator.calculateTTB(emptyHousehold, totalAvailable), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(21.818, calculator.calculateTTB(poorRetirees, totalAvailable), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);

        assertEquals(37.440, calculator.calculateTTB(poorBigFamily, totalAvailable), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(25.681, calculator.calculateTTB(richBigFamily, totalAvailable), 0.001);
    }

    private void addHouseholds() {
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0, dummyZone);
        dataSet.addHousehold(emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2,  10000, 0, dummyZone);
        dataSet.addHousehold(poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3,   10000, 0, dummyZone);
        dataSet.addHousehold(poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4,500000, 0, dummyZone);
        dataSet.addHousehold(richBigFamily);
    }

    private void addPersons() {

        MitoPerson retiree21 = new MitoPerson(21, Occupation.UNEMPLOYED, -1, 70, Gender.MALE, false);
        MitoPerson retiree22 = new MitoPerson(22,  Occupation.UNEMPLOYED, -1, 70, Gender.FEMALE, false);
        dataSet.getHouseholds().get(2).addPerson(retiree21);
        dataSet.getHouseholds().get(2).addPerson(retiree22);

        MitoPerson worker31 = new MitoPerson(31, Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker31.setWorkzone(dummyZone);
        MitoPerson worker32 = new MitoPerson(32, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker32.setWorkzone(dummyZone);
        MitoPerson worker33 = new MitoPerson(33, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker33.setWorkzone(dummyZone);
        MitoPerson child34 = new MitoPerson(34, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child35 = new MitoPerson(35, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(3).addPerson(worker31);
        dataSet.getHouseholds().get(3).addPerson(worker32);
        dataSet.getHouseholds().get(3).addPerson(worker33);
        dataSet.getHouseholds().get(3).addPerson(child34);
        dataSet.getHouseholds().get(3).addPerson(child35);

        MitoPerson worker41 = new MitoPerson(41, Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker41.setWorkzone(dummyZone);
        MitoPerson worker42 = new MitoPerson(42, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker42.setWorkzone(dummyZone);
        MitoPerson worker43 = new MitoPerson(43, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker43.setWorkzone(dummyZone);
        MitoPerson child44 = new MitoPerson(44, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child45 = new MitoPerson(45, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(4).addPerson(worker41);
        dataSet.getHouseholds().get(4).addPerson(worker42);
        dataSet.getHouseholds().get(4).addPerson(worker43);
        dataSet.getHouseholds().get(4).addPerson(child44);
        dataSet.getHouseholds().get(4).addPerson(child45);
    }

    private void addZone() {
        dummyZone = new Zone(1);
        dummyZone.setRegion(1);
        dataSet.addZone(dummyZone);
    }
}
