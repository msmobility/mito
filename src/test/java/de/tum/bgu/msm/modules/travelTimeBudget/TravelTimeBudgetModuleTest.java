package de.tum.bgu.msm.modules.travelTimeBudget;

import com.google.common.collect.Lists;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import static de.tum.bgu.msm.data.Purpose.HBE;
import static de.tum.bgu.msm.data.Purpose.HBW;
import static org.junit.Assert.assertEquals;

public class TravelTimeBudgetModuleTest {

    private DataSet dataSet;
    private MitoZone dummyZone;

    @Before
    public void setup() {

        Resources.initializeResources("./testInput/test.properties");

        dataSet = new DataSet();
        dataSet.setTravelTimes(new TravelTimes() {
			@Override
			public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
				return 10.;
			}
			
			@Override
			public double getTravelTime(int origin, int destination, double timeOfDay_s, String mode) {
				return 10.;
			}

			@Override
			public double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode) {
				return 0;
			}
		});
        addZone();
        addHouseholds();
        addPersons();

        TravelTimeBudgetModule travelTimeBudget = new TravelTimeBudgetModule(dataSet);
        travelTimeBudget.run();
    }

    @Test
    public void testEmptyHousehold() {
        MitoHousehold emptyHousehold = dataSet.getHouseholds().get(1);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(HBW), 0.001);
        assertEquals(0, emptyHousehold.getTravelTimeBudgetForPurpose(HBE), 0.001);
        assertEquals(22.811, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(5.700, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(6.749, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(12.572, emptyHousehold.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += emptyHousehold.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(47.8349, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorRetireesHousehold() {
        MitoHousehold poorRetirees = dataSet.getHouseholds().get(2);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose(HBW), 0.001);
        assertEquals(0, poorRetirees.getTravelTimeBudgetForPurpose(HBE), 0.001);
        assertEquals(34.907, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(9.460, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(8.811, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(18.827, poorRetirees.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorRetirees.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(72.006, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testPoorBigFamilyHousehold() {
        MitoHousehold poorBigFamily = dataSet.getHouseholds().get(3);
        assertEquals(60.0, poorBigFamily.getTravelTimeBudgetForPurpose(HBW), 0.001);
        assertEquals(40, poorBigFamily.getTravelTimeBudgetForPurpose(HBE), 0.001);
        assertEquals(10.134, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(4.482, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(3.743, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(7.259, poorBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += poorBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(125.619, totalTravelTimeBudget, 0.001);
    }

    @Test
    public void testRichBigFamilyHousehold() {
        MitoHousehold richBigFamily = dataSet.getHouseholds().get(4);
        assertEquals(60.0, richBigFamily.getTravelTimeBudgetForPurpose(HBW), 0.001);
        assertEquals(40, richBigFamily.getTravelTimeBudgetForPurpose(HBE), 0.001);
        assertEquals(10.134, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBO), 0.001);
        assertEquals(4.482, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.HBS), 0.001);
        assertEquals(3.743, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBW), 0.001);
        assertEquals(7.259, richBigFamily.getTravelTimeBudgetForPurpose(Purpose.NHBO), 0.001);

        double totalTravelTimeBudget = 0;
        for(Purpose purpose: Purpose.values()) {
            totalTravelTimeBudget += richBigFamily.getTravelTimeBudgetForPurpose(purpose);
        }
        assertEquals(125.619, totalTravelTimeBudget, 0.001);
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
        dummyZone = new MitoZone(1, 10, AreaTypes.SGType.CORE_CITY);
        dataSet.addZone(dummyZone);
    }

    private void addPersons() {

        MitoPerson retiree21 = new MitoPerson(21, MitoOccupation.UNEMPLOYED, -1, 70, MitoGender.MALE, false);
        MitoPerson retiree22 = new MitoPerson(22, MitoOccupation.UNEMPLOYED, -1, 70, MitoGender.FEMALE, false);
        dataSet.getHouseholds().get(2).addPerson(retiree21);
        dataSet.getHouseholds().get(2).addPerson(retiree22);

        MitoPerson worker31 = new MitoPerson(31, MitoOccupation.WORKER, 1, 45, MitoGender.MALE, true);
        worker31.setOccupationZone(dummyZone);
        MitoTrip trip31 = new MitoTrip(31, HBW);
        worker31.addTrip(trip31);
        MitoPerson worker32 = new MitoPerson(32, MitoOccupation.WORKER, 1, 45, MitoGender.FEMALE, true);
        worker32.setOccupationZone(dummyZone);
        MitoTrip trip32 = new MitoTrip(32, HBW);
        worker32.addTrip(trip32);
        MitoPerson worker33 = new MitoPerson(33, MitoOccupation.WORKER, 1, 20, MitoGender.MALE, false);
        worker33.setOccupationZone(dummyZone);
        MitoTrip trip33 = new MitoTrip(33, HBW);
        worker33.addTrip(trip33);
        MitoPerson child34 = new MitoPerson(34, MitoOccupation.STUDENT, -1, 10, MitoGender.FEMALE, false);
        child34.setOccupationZone(dummyZone);
        MitoTrip trip34 = new MitoTrip(34, HBE);
        child34.addTrip(trip34);
        MitoPerson child35 = new MitoPerson(35, MitoOccupation.STUDENT, -1, 10, MitoGender.FEMALE, false);
        child35.setOccupationZone(dummyZone);
        MitoTrip trip35 = new MitoTrip(35, HBE);
        child35.addTrip(trip35);

        dataSet.getHouseholds().get(3).addPerson(worker31);
        dataSet.getHouseholds().get(3).addPerson(worker32);
        dataSet.getHouseholds().get(3).addPerson(worker33);
        dataSet.getHouseholds().get(3).addPerson(child34);
        dataSet.getHouseholds().get(3).addPerson(child35);

        MitoPerson worker41 = new MitoPerson(41, MitoOccupation.WORKER, 1, 45, MitoGender.MALE, true);
        worker41.setOccupationZone(dummyZone);
        MitoTrip trip41 = new MitoTrip(1, HBW);
        worker41.addTrip(trip41);
        MitoPerson worker42 = new MitoPerson(42, MitoOccupation.WORKER, 1, 45, MitoGender.FEMALE, true);
        worker42.setOccupationZone(dummyZone);
        MitoTrip trip42 = new MitoTrip(2, HBW);
        worker42.addTrip(trip42);
        MitoPerson worker43 = new MitoPerson(43, MitoOccupation.WORKER, 1, 20, MitoGender.MALE, false);
        worker43.setOccupationZone(dummyZone);
        MitoTrip trip43 = new MitoTrip(3, HBW);
        worker43.addTrip(trip43);
        MitoPerson child44 = new MitoPerson(44, MitoOccupation.STUDENT, -1, 10, MitoGender.FEMALE, false);
        child44.setOccupationZone(dummyZone);
        MitoTrip trip44 = new MitoTrip(4, HBE);
        child44.addTrip(trip44);
        MitoPerson child45 = new MitoPerson(45, MitoOccupation.STUDENT, -1, 10, MitoGender.FEMALE, false);
        child45.setOccupationZone(dummyZone);
        MitoTrip trip45 = new MitoTrip(5, HBE);
        child45.addTrip(trip45);

        dataSet.getHouseholds().get(3).setTripsByPurpose(Lists.newArrayList(trip31, trip32, trip33), HBW);
        dataSet.getHouseholds().get(4).setTripsByPurpose(Lists.newArrayList(trip41, trip42, trip43), HBW);

        dataSet.getHouseholds().get(3).setTripsByPurpose(Lists.newArrayList(trip34, trip35), HBE);
        dataSet.getHouseholds().get(4).setTripsByPurpose(Lists.newArrayList(trip44, trip45), HBE);

        dataSet.getHouseholds().get(4).addPerson(worker41);
        dataSet.getHouseholds().get(4).addPerson(worker42);
        dataSet.getHouseholds().get(4).addPerson(worker43);
        dataSet.getHouseholds().get(4).addPerson(child44);
        dataSet.getHouseholds().get(4).addPerson(child45);
    }
}
