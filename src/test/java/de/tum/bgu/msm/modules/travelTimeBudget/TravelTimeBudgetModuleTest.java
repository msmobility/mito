package de.tum.bgu.msm.modules.travelTimeBudget;

import com.pb.common.matrix.IdentityMatrix;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.TravelTimeBudget;
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
        dataSet.setAutoTravelTimes((origin, destination) -> 1);
        addZone();
        addPurposes();
        addHouseholds();
        addWorkers();

        TravelTimeBudget travelTimeBudget = new TravelTimeBudget(dataSet);
        travelTimeBudget.run();
    }

    @Test
    public void testEmptyHousehold() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose("HBW"), 0.);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose("HBE"), 0.);
        assertEquals(0.973, emptyHousehold.getTravelTimeBudgetForPurpose("HBO"), 0.001);
        assertEquals(1.609, emptyHousehold.getTravelTimeBudgetForPurpose("HBS"), 0.001);
        assertEquals(0.128, emptyHousehold.getTravelTimeBudgetForPurpose("NHBW"), 0.001);
        assertEquals(1.850, emptyHousehold.getTravelTimeBudgetForPurpose("NHBO"), 0.001);

        double totalTravelTimeBudget = 0;
        for(String purpose: dataSet.getPurposes()) {
            totalTravelTimeBudget += emptyHousehold.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(4.562, totalTravelTimeBudget, 0.);
    }

    @Test
    public void testPoorRetireesHousehold() {
        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose("HBW"), 0.);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose("HBE"), 0.);
        assertEquals(1.052, poorRetirees.getTravelTimeBudgetForPurpose("HBO"), 0.001);
        assertEquals(1.724, poorRetirees.getTravelTimeBudgetForPurpose("HBS"), 0.001);
        assertEquals(0.144, poorRetirees.getTravelTimeBudgetForPurpose("NHBW"), 0.001);
        assertEquals(1.980, poorRetirees.getTravelTimeBudgetForPurpose("NHBO"), 0.001);

        double totalTravelTimeBudget = 0;
        for(String purpose: dataSet.getPurposes()) {
            totalTravelTimeBudget += poorRetirees.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(4.902, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorBigFamilyHousehold() {
        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(1, poorBigFamily.getTravelTimeBudgetForPurpose("HBW"), 0.);
        assertEquals(0, poorBigFamily.getTravelTimeBudgetForPurpose("HBE"), 0.);
        assertEquals(0.971, poorBigFamily.getTravelTimeBudgetForPurpose("HBO"), 0.001);
        assertEquals(1.567, poorBigFamily.getTravelTimeBudgetForPurpose("HBS"), 0.001);
        assertEquals(0.154, poorBigFamily.getTravelTimeBudgetForPurpose("NHBW"), 0.001);
        assertEquals(1.813, poorBigFamily.getTravelTimeBudgetForPurpose("NHBO"), 0.001);

        double totalTravelTimeBudget = 0;
        for(String purpose: dataSet.getPurposes()) {
            totalTravelTimeBudget += poorBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(5.507, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testRichBigFamilyHousehold() {
        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(1, richBigFamily.getTravelTimeBudgetForPurpose("HBW"), 0.);
        assertEquals(0, richBigFamily.getTravelTimeBudgetForPurpose("HBE"), 0.);
        assertEquals(1.298, richBigFamily.getTravelTimeBudgetForPurpose("HBO"), 0.001);
        assertEquals(0.964, richBigFamily.getTravelTimeBudgetForPurpose("HBS"), 0.001);
        assertEquals(1.165, richBigFamily.getTravelTimeBudgetForPurpose("NHBW"), 0.001);
        assertEquals(1.078, richBigFamily.getTravelTimeBudgetForPurpose("NHBO"), 0.001);

        double totalTravelTimeBudget = 0;
        for(String purpose: dataSet.getPurposes()) {
            totalTravelTimeBudget += richBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(5.507, totalTravelTimeBudget, 0.001);
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

    private void addWorkers() {
        MitoPerson worker11 = new MitoPerson(1, 3, 1, 1);
        worker11.setWorkzone(1);
        MitoPerson worker12 = new MitoPerson(2, 3, 1, 1);
        worker12.setWorkzone(1);
        MitoPerson worker13 = new MitoPerson(3, 3, 1, 1);
        worker13.setWorkzone(1);

        dataSet.getHouseholds().get(3).getPersons().add(worker11);
        dataSet.getHouseholds().get(3).getPersons().add(worker12);
        dataSet.getHouseholds().get(3).getPersons().add(worker13);

        MitoPerson worker21 = new MitoPerson(1, 4, 1, 1);
        worker21.setWorkzone(1);
        MitoPerson worker22 = new MitoPerson(2, 4, 1, 1);
        worker22.setWorkzone(1);
        MitoPerson worker23 = new MitoPerson(3, 4, 1, 1);
        worker23.setWorkzone(1);

        dataSet.getHouseholds().get(4).getPersons().add(worker21);
        dataSet.getHouseholds().get(4).getPersons().add(worker22);
        dataSet.getHouseholds().get(4).getPersons().add(worker23);
    }
}
