package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetCalculatorTest {

    private DataSet dataSet;
    private MitoZone dummyZone;
    private TravelTimeBudgetJSCalculator calculator;

    @Before
    public void setup() {

        Resources.initializeResources("./test/muc/test.properties");

        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TravelTimeBudgetCalc"));
        calculator = new TravelTimeBudgetJSCalculator(reader);

        dataSet = new DataSet();
        addZone();
        addHouseholds();
        addPersons();
    }

    @Test
    public void testTotalTravelTimeBudget() {
       
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(47.834, calculator.calculateBudget(emptyHousehold, "Total"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(72.006, calculator.calculateBudget(poorRetirees, "Total"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(100.489, calculator.calculateBudget(poorBigFamily, "Total"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(100.489, calculator.calculateBudget(richBigFamily, "Total"), 0.001);
    }


    @Test
    public void testHBSTravelTimeBudget() {

            String hbs = "HBS";
            MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
            assertEquals(9.9815, calculator.calculateBudget(emptyHousehold, hbs), 0.001);

            MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
            assertEquals(14.523, calculator.calculateBudget(poorRetirees, hbs), 0.001);

            MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
            assertEquals(15.146, calculator.calculateBudget(poorBigFamily, hbs), 0.001);

            MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
            assertEquals(15.146, calculator.calculateBudget(richBigFamily, hbs), 0.001);
    }

    @Test
    public void testHBOTravelTimeBudget() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(39.941, calculator.calculateBudget(emptyHousehold, "HBO"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(53.587, calculator.calculateBudget(poorRetirees, "HBO"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(75.649, calculator.calculateBudget(poorBigFamily, "HBO"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(75.649, calculator.calculateBudget(richBigFamily, "HBO"), 0.001);
    }

    @Test
    public void testNHBWTravelTimeBudget() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(11.818, calculator.calculateBudget(emptyHousehold, "NHBW"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(13.526, calculator.calculateBudget(poorRetirees, "NHBW"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(15.114, calculator.calculateBudget(poorBigFamily, "NHBW"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(15.114, calculator.calculateBudget(richBigFamily, "NHBW"), 0.001);
    }

    @Test
    public void testNHBOTravelTimeBudget() {

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(22.014, calculator.calculateBudget(emptyHousehold, "NHBO"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(28.903, calculator.calculateBudget(poorRetirees, "NHBO"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(51.214, calculator.calculateBudget(poorBigFamily, "NHBO"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(51.214, calculator.calculateBudget(richBigFamily, "NHBO"), 0.001);
    }

    private void addHouseholds() {
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0);
        emptyHousehold.setHomeZone(dummyZone);
        dataSet.addHousehold(emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2, 10000, 0);
        poorRetirees.setHomeZone(dummyZone);
        dataSet.addHousehold(poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3, 10000, 0);
        poorBigFamily.setHomeZone(dummyZone);
        dataSet.addHousehold(poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4, 500000, 0);
        richBigFamily.setHomeZone(dummyZone);
        dataSet.addHousehold(richBigFamily);
    }

    private void addPersons() {

        MitoPerson retiree21 = new MitoPerson(21, MitoOccupationStatus.UNEMPLOYED, null, 70, MitoGender.MALE, false);
        MitoPerson retiree22 = new MitoPerson(22, MitoOccupationStatus.UNEMPLOYED, null, 70, MitoGender.FEMALE, false);
        dataSet.getHouseholds().get(2).addPerson(retiree21);
        dataSet.getHouseholds().get(2).addPerson(retiree22);

        MitoPerson worker31 = new MitoPerson(31, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 45, MitoGender.MALE, true);
        MitoPerson worker32 = new MitoPerson(32, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 45, MitoGender.FEMALE, true);
        MitoPerson worker33 = new MitoPerson(33, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 20, MitoGender.MALE, false);
        MitoPerson child34 = new MitoPerson(34, MitoOccupationStatus.STUDENT, null, 10, MitoGender.FEMALE, false);
        MitoPerson child35 = new MitoPerson(35, MitoOccupationStatus.STUDENT, null, 10, MitoGender.FEMALE, false);

        dataSet.getHouseholds().get(3).addPerson(worker31);
        dataSet.getHouseholds().get(3).addPerson(worker32);
        dataSet.getHouseholds().get(3).addPerson(worker33);
        dataSet.getHouseholds().get(3).addPerson(child34);
        dataSet.getHouseholds().get(3).addPerson(child35);

        MitoPerson worker41 = new MitoPerson(41, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 45, MitoGender.MALE, true);
        MitoPerson worker42 = new MitoPerson(42, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 45, MitoGender.FEMALE, true);
        MitoPerson worker43 = new MitoPerson(43, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 20, MitoGender.MALE, false);
        MitoPerson child44 = new MitoPerson(44, MitoOccupationStatus.STUDENT, null, 10, MitoGender.FEMALE, false);
        MitoPerson child45 = new MitoPerson(45, MitoOccupationStatus.STUDENT, null, 10, MitoGender.FEMALE, false);

        dataSet.getHouseholds().get(4).addPerson(worker41);
        dataSet.getHouseholds().get(4).addPerson(worker42);
        dataSet.getHouseholds().get(4).addPerson(worker43);
        dataSet.getHouseholds().get(4).addPerson(child44);
        dataSet.getHouseholds().get(4).addPerson(child45);
    }

    private void addZone() {
        dummyZone = new MitoZone(1, AreaTypes.SGType.CORE_CITY);
        dataSet.addZone(dummyZone);
    }
}
