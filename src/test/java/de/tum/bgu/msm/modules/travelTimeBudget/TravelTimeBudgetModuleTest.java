package de.tum.bgu.msm.modules.travelTimeBudget;

import com.pb.common.matrix.IdentityMatrix;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.TravelTimeBudget;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetModuleTest {

    private DataSet dataSet;

    @Before
    public void setup() {

        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);

        dataSet = new DataSet();
        dataSet.setAutoTravelTimes((origin, destination) -> 20);
        addZone();
        addHouseholds();
        addPersons();

        TravelTimeBudget travelTimeBudget = new TravelTimeBudget(dataSet);
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
        MitoHousehold emptyHousehold = new MitoHousehold(1, 10000, 0, 1);
        dataSet.getHouseholds().put(emptyHousehold.getHhId(), emptyHousehold);

        MitoHousehold poorRetirees = new MitoHousehold(2,  10000, 0, 1);
        dataSet.getHouseholds().put(poorRetirees.getHhId(), poorRetirees);

        MitoHousehold poorBigFamily = new MitoHousehold(3,  10000, 0, 1);
        dataSet.getHouseholds().put(poorBigFamily.getHhId(), poorBigFamily);

        MitoHousehold richBigFamily = new MitoHousehold(4,  500000, 0, 1);
        dataSet.getHouseholds().put(richBigFamily.getHhId(), richBigFamily);
    }

    private void addZone() {
        Zone zone = new Zone(1);
        zone.setRegion(1);
        dataSet.getZones().put(zone.getZoneId(), zone);
    }

    private void addPersons() {

        MitoPerson retiree21 = new MitoPerson(21, 2, Occupation.UNEMPLOYED, -1, 70, Gender.MALE, false);
        MitoPerson retiree22 = new MitoPerson(22, 2, Occupation.UNEMPLOYED, -1, 70, Gender.FEMALE, false);
        dataSet.getHouseholds().get(2).getPersons().add(retiree21);
        dataSet.getHouseholds().get(2).getPersons().add(retiree22);

        MitoPerson worker31 = new MitoPerson(31, 3,  Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker31.setWorkzone(1);
        MitoPerson worker32 = new MitoPerson(32, 3, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker32.setWorkzone(1);
        MitoPerson worker33 = new MitoPerson(33, 3, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker33.setWorkzone(1);
        MitoPerson child34 = new MitoPerson(34, 3, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child35 = new MitoPerson(35, 3, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(3).getPersons().add(worker31);
        dataSet.getHouseholds().get(3).getPersons().add(worker32);
        dataSet.getHouseholds().get(3).getPersons().add(worker33);
        dataSet.getHouseholds().get(3).getPersons().add(child34);
        dataSet.getHouseholds().get(3).getPersons().add(child35);

        MitoPerson worker41 = new MitoPerson(41, 3,  Occupation.WORKER, 1, 45, Gender.MALE, true);
        worker41.setWorkzone(1);
        MitoPerson worker42 = new MitoPerson(42, 3, Occupation.WORKER, 1, 45, Gender.FEMALE, true);
        worker42.setWorkzone(1);
        MitoPerson worker43 = new MitoPerson(43, 3, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker43.setWorkzone(1);
        MitoPerson child44 = new MitoPerson(44, 3, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);
        MitoPerson child45 = new MitoPerson(45, 3, Occupation.STUDENT, -1, 10, Gender.FEMALE, false);

        dataSet.getHouseholds().get(4).getPersons().add(worker41);
        dataSet.getHouseholds().get(4).getPersons().add(worker42);
        dataSet.getHouseholds().get(4).getPersons().add(worker43);
        dataSet.getHouseholds().get(4).getPersons().add(child44);
        dataSet.getHouseholds().get(4).getPersons().add(child45);
    }
}
