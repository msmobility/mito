package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.data.Gender;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.Implementation;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetModuleTest {

    private DataSet dataSet;
    private MitoZone dummyZone;

    @Before
    public void setup() {

        try {
            Resources.initializeResources("./testInput/test.properties", Implementation.MUNICH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataSet = new DataSet();
        TravelTimes travelTimes = (origin, destination, time) -> 20;
        dataSet.addTravelTimeForMode("car", travelTimes);
        dataSet.addTravelTimeForMode("pt", travelTimes);
        addZone();
        addHouseholds();
        addPersons();

        TravelTimeBudgetModule travelTimeBudget = new TravelTimeBudgetModule(dataSet);
        travelTimeBudget.run();
    }

    @Test
    public void testEmptyHousehold() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(18.809, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(10.397, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(9.704, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(11.209, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += emptyHousehold.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(50.121, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorRetireesHousehold() {
        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(29.036, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(14.150, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(12.588, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(14.642, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorRetirees.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(70.418, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorBigFamilyHousehold() {
        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(60.0, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(40, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(12.148, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(4.252, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(6.034, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(6.518, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(128.953, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testRichBigFamilyHousehold() {
        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(60.0, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(40, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(12.381, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(3.750, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(7.365, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(5.456, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += richBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(128.953, totalTravelTimeBudget, 0.001);
    }

    private void addHouseholds() {
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0, dummyZone);
        dataSet.addHousehold(emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2,  10000, 0, dummyZone);
        dataSet.addHousehold(poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3,  10000, 0, dummyZone);
        dataSet.addHousehold(poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4,  500000, 0, dummyZone);
        dataSet.addHousehold(richBigFamily);
    }

    private void addZone() {
        dummyZone = new MitoZone(1, 10, AreaType.URBAN);
        dataSet.addZone(dummyZone);
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
        child34.setOccupationZone(dummyZone);
        MitoPerson child35 = new MitoPerson(35, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        child35.setOccupationZone(dummyZone);

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
        child44.setOccupationZone(dummyZone);
        MitoPerson child45 = new MitoPerson(45, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        child45.setOccupationZone(dummyZone);

        dataSet.getHouseholds().get(4).addPerson(worker41);
        dataSet.getHouseholds().get(4).addPerson(worker42);
        dataSet.getHouseholds().get(4).addPerson(worker43);
        dataSet.getHouseholds().get(4).addPerson(child44);
        dataSet.getHouseholds().get(4).addPerson(child45);
    }
}
