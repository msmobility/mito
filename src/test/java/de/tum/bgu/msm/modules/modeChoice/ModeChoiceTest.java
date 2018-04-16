package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Random;

public class ModeChoiceTest {

    private DataSet dataSet;
    private MitoHousehold household1;
    private MitoTrip trip1;
    private MitoHousehold household2;
    private MitoTrip trip2;


    @Before
    public void setupTest() {
        MitoUtil.initializeRandomNumber(new Random(42));

        dataSet = new DataSet();
        dataSet.setTravelDistancesAuto((origin, destination) -> 1000);
        dataSet.setTravelDistancesNMT((origin, destination) -> 1000);
        dataSet.addTravelTimeForMode("car", (origin, destination, timeOfDay_s) -> 10);
        dataSet.addTravelTimeForMode("autoP", (origin, destination, timeOfDay_s) -> 10);
        dataSet.addTravelTimeForMode("bus", (origin, destination, timeOfDay_s) -> 10);
        dataSet.addTravelTimeForMode("tramMetro", (origin, destination, timeOfDay_s) -> 10);
        dataSet.addTravelTimeForMode("train", (origin, destination, timeOfDay_s) -> 10);
        fillDataSet();
    }

    @Test
    public void testModeChoice() throws Exception {
        ModeChoice modeChoice = new ModeChoice(dataSet);
        ModeChoice.ModeChoiceByPurpose modeChoiceByPurpose = new ModeChoice.ModeChoiceByPurpose(Purpose.HBW,dataSet, false);
    }


    private void fillDataSet() {
        trip1 = new MitoTrip(1, Purpose.HBW);
        MitoPerson person1 = new MitoPerson(1, Occupation.WORKER, -1, 30, Gender.MALE, true);
        trip1.setPerson(person1);
        MitoZone zone1 = new MitoZone(1, 100, AreaType.URBAN);
        zone1.setDistanceToNearestTransitStop(0.5f);
        zone1.setAreaTypeHBWModeChoice(AreaTypeForModeChoice.HBW_coreCity);
        trip1.setTripOrigin(zone1);
        MitoZone zone2 = new MitoZone(2, 100, AreaType.URBAN);
        zone2.setAreaTypeHBWModeChoice(AreaTypeForModeChoice.HBW_coreCity);
        trip1.setTripDestination(zone2);

        household1 = new MitoHousehold(1, 24000, 1, zone1);
        household1.addPerson(person1);
        household1.setTripsByPurpose(Collections.singletonList(trip1), Purpose.HBW);
        dataSet.addTrip(trip1);
        dataSet.addZone(zone1);
        dataSet.addZone(zone2);
        dataSet.addHousehold(household1);
        dataSet.addPerson(person1);

        trip2 = new MitoTrip(2, Purpose.HBO);
        MitoPerson person2 = new MitoPerson(2, Occupation.WORKER, -1, 30, Gender.MALE, true);
        trip2.setPerson(person2);
        MitoZone zone3 = new MitoZone(3, 100, AreaType.URBAN);
        zone3.setDistanceToNearestTransitStop(0.5f);
        zone3.setAreaTypeHBWModeChoice(AreaTypeForModeChoice.HBW_coreCity);
        trip2.setTripOrigin(zone3);
        MitoZone zone4 = new MitoZone(4, 100, AreaType.URBAN);
        zone4.setAreaTypeHBWModeChoice(AreaTypeForModeChoice.HBW_coreCity);
        trip2.setTripDestination(zone4);

        household2 = new MitoHousehold(2, 24000, 1, zone3);
        household2.addPerson(person2);
        household2.setTripsByPurpose(Collections.singletonList(trip2), Purpose.HBO);
        dataSet.addTrip(trip2);
        dataSet.addZone(zone3);
        dataSet.addZone(zone4);
        dataSet.addHousehold(household2);
        dataSet.addPerson(person2);

    }
}
