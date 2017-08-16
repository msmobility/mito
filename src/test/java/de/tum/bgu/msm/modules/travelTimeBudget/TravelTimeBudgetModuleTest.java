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
        dataSet.setAutoTravelTimes((origin, destination) -> 30);
        addZone();
        addHouseholds();
        addWorkers();

        TravelTimeBudget travelTimeBudget = new TravelTimeBudget(dataSet);
        travelTimeBudget.run();
    }

    @Test
    public void testEmptyHousehold() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(19.445, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(9.083, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(12.377, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(9.215, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

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
        assertEquals(29.994, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(12.351, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(16.043, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(12.028, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorRetirees.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(70.418, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorBigFamilyHousehold() {
        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(90.0, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(0, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(16.680, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(4.933, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(10.221, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(7.117, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(128.953, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testRichBigFamilyHousehold() {
        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(90.0, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBW), 0.001);
        assertEquals(0, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBE), 0.001);
        assertEquals(16.680, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(4.933, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(10.221, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(7.117, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += richBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(128.953, totalTravelTimeBudget, 0.001);
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

    private void addWorkers() {
        MitoPerson worker11 = new MitoPerson(1, 3, Occupation.WORKER, 1, 20, Gender.MALE, true);
        worker11.setWorkzone(1);
        MitoPerson worker12 = new MitoPerson(2, 3, Occupation.WORKER, 1, 20, Gender.MALE, true);
        worker12.setWorkzone(1);
        MitoPerson worker13 = new MitoPerson(3, 3, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker13.setWorkzone(1);

        dataSet.getHouseholds().get(3).getPersons().add(worker11);
        dataSet.getHouseholds().get(3).getPersons().add(worker12);
        dataSet.getHouseholds().get(3).getPersons().add(worker13);

        MitoPerson worker21 = new MitoPerson(1, 4, Occupation.WORKER, 1, 20, Gender.MALE, true);
        worker21.setWorkzone(1);
        MitoPerson worker22 = new MitoPerson(2, 4, Occupation.WORKER, 1, 20, Gender.MALE, true);
        worker22.setWorkzone(1);
        MitoPerson worker23 = new MitoPerson(3, 4, Occupation.WORKER, 1, 20, Gender.MALE, false);
        worker23.setWorkzone(1);

        dataSet.getHouseholds().get(4).getPersons().add(worker21);
        dataSet.getHouseholds().get(4).getPersons().add(worker22);
        dataSet.getHouseholds().get(4).getPersons().add(worker23);
    }
}
