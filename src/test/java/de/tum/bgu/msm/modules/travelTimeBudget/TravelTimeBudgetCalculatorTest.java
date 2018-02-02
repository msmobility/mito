package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.Implementation;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.*;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetCalculatorTest {

    private DataSet dataSet;
    private MitoZone dummyZone;
    private TravelTimeBudgetJSCalculator calculator;

    @Before
    public void setup() {
        try {
            Resources.initializeResources("./testInput/test.properties", Implementation.MUNICH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Reader reader = new FileReader(Resources.INSTANCE.getString(Properties.TRAVEL_TIME_BUDGET_JS));
            calculator = new TravelTimeBudgetJSCalculator(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        dataSet = new DataSet();
        addZone();
        addHouseholds();
        addPersons();
    }

    @Test
    public void testTotalTravelTimeBudget() {
       
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(50.121, calculator.calculateBudget(emptyHousehold, "Total"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(70.418, calculator.calculateBudget(poorRetirees, "Total"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(128.953, calculator.calculateBudget(poorBigFamily, "Total"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(128.953, calculator.calculateBudget(richBigFamily, "Total"), 0.001);
    }


    @Test
    public void testHBSTravelTimeBudget() {

            String hbs = "HBS";
            MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
            assertEquals(16.586, calculator.calculateBudget(emptyHousehold, hbs), 0.001);

            MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
            assertEquals(21.085, calculator.calculateBudget(poorRetirees, hbs), 0.001);

            MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
            assertEquals(24.424, calculator.calculateBudget(poorBigFamily, hbs), 0.001);

            MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
            assertEquals(17.649, calculator.calculateBudget(richBigFamily, hbs), 0.001);
    }

    @Test
    public void testHBOTravelTimeBudget() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(30.005, calculator.calculateBudget(emptyHousehold, "HBO"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(43.267, calculator.calculateBudget(poorRetirees, "HBO"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(69.783, calculator.calculateBudget(poorBigFamily, "HBO"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(58.270, calculator.calculateBudget(richBigFamily, "HBO"), 0.001);
    }

    @Test
    public void testNHBWTravelTimeBudget() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(15.481, calculator.calculateBudget(emptyHousehold, "NHBW"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(18.758, calculator.calculateBudget(poorRetirees, "NHBW"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(34.662, calculator.calculateBudget(poorBigFamily, "NHBW"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(34.662, calculator.calculateBudget(richBigFamily, "NHBW"), 0.001);
    }

    @Test
    public void testNHBOTravelTimeBudget() {

        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(17.881, calculator.calculateBudget(emptyHousehold, "NHBO"), 0.001);

        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(21.818, calculator.calculateBudget(poorRetirees, "NHBO"), 0.001);

        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(37.440, calculator.calculateBudget(poorBigFamily, "NHBO"), 0.001);

        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(25.681, calculator.calculateBudget(richBigFamily, "NHBO"), 0.001);
    }

    private void addHouseholds() {
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0, dummyZone);
        dataSet.addHousehold(emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2, 10000, 0, dummyZone);
        dataSet.addHousehold(poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3, 10000, 0, dummyZone);
        dataSet.addHousehold(poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4, 500000, 0, dummyZone);
        dataSet.addHousehold(richBigFamily);
    }

    private void addPersons() {

        MitoPerson retiree21 = new MitoPerson(21, Occupation.UNEMPLOYED, -1, 70, Gender.MALE, false);
        MitoPerson retiree22 = new MitoPerson(22, Occupation.UNEMPLOYED, -1, 70, Gender.FEMALE, false);
        dataSet.getHouseholds().get(2).addPerson(retiree21);
        dataSet.getHouseholds().get(2).addPerson(retiree22);

        MitoPerson worker31 = new MitoPerson(31, Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker31.setOccupationZone(dummyZone);
        MitoPerson worker32 = new MitoPerson(32, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker32.setOccupationZone(dummyZone);
        MitoPerson worker33 = new MitoPerson(33, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker33.setOccupationZone(dummyZone);
        MitoPerson child34 = new MitoPerson(34, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child35 = new MitoPerson(35, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(3).addPerson(worker31);
        dataSet.getHouseholds().get(3).addPerson(worker32);
        dataSet.getHouseholds().get(3).addPerson(worker33);
        dataSet.getHouseholds().get(3).addPerson(child34);
        dataSet.getHouseholds().get(3).addPerson(child35);

        MitoPerson worker41 = new MitoPerson(41, Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker41.setOccupationZone(dummyZone);
        MitoPerson worker42 = new MitoPerson(42, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker42.setOccupationZone(dummyZone);
        MitoPerson worker43 = new MitoPerson(43, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker43.setOccupationZone(dummyZone);
        MitoPerson child44 = new MitoPerson(44, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child45 = new MitoPerson(45, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(4).addPerson(worker41);
        dataSet.getHouseholds().get(4).addPerson(worker42);
        dataSet.getHouseholds().get(4).addPerson(worker43);
        dataSet.getHouseholds().get(4).addPerson(child44);
        dataSet.getHouseholds().get(4).addPerson(child45);
    }

    private void addZone() {
        dummyZone = new MitoZone(1, 1, AreaType.URBAN);
        dataSet.addZone(dummyZone);
    }
}
