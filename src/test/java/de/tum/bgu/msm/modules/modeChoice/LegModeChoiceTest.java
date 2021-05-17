package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.DummyZone;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.modeChoice.calculators.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class LegModeChoiceTest {

    private DataSet dataSet;
    private MitoHousehold household1;
    private MitoTrip trip1;
    private MitoHousehold household2;
    private MitoTrip trip2;


    @Before
    public void setupTest() {
        MitoUtil.initializeRandomNumber(new Random(42));
        Resources.initializeResources("./test/muc/test.properties");

        dataSet = new DataSet();
        dataSet.setTravelDistancesAuto((origin, destination) -> 1000);
        dataSet.setTravelDistancesNMT((origin, destination) -> 1000);
        dataSet.setTravelTimes(new TravelTimes() {
			@Override
			public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
				return 10.;
			}

            @Override
            public double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode) {
                return 0;
            }

            @Override
            public double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode) {
                return 0;
            }


            @Override
            public IndexedDoubleMatrix2D getPeakSkim(String mode) {
                return null;
            }

            @Override
            public TravelTimes duplicate() {
                return null;
            }
        });
        fillDataSet();
    }

    @Test
    public void testModeChoice() {
        ModeChoice modeChoice = new ModeChoice(dataSet, Purpose.getAllPurposes());
        ModeChoice.ModeChoiceByPurpose modeChoiceByPurpose = new ModeChoice.ModeChoiceByPurpose(Purpose.HBW,dataSet, new ModeChoiceCalculatorImpl());
    }


    private void fillDataSet() {
        trip1 = new MitoTrip(1, Purpose.HBW);
        MitoPerson person1 = new MitoPerson(1, MitoOccupationStatus.WORKER, null, 30, MitoGender.MALE, true);
        trip1.setPerson(person1);
        MitoZone zone1 = DummyZone.dummy;
        zone1.setDistanceToNearestRailStop(0.5f);

        trip1.setTripOrigin(zone1);
        MitoZone zone2 = new MitoZone(2, AreaTypes.SGType.CORE_CITY);
        trip1.setTripDestination(zone2);

        household1 = new MitoHousehold(1, 24000, 1);
        household1.setHomeZone(zone1);
        household1.addPerson(person1);
        household1.setTripsByPurpose(Collections.singletonList(trip1), Purpose.HBW);
        dataSet.addTrip(trip1);
        dataSet.addZone(zone1);
        dataSet.addZone(zone2);
        dataSet.addHousehold(household1);
        dataSet.addPerson(person1);

        trip2 = new MitoTrip(2, Purpose.HBO);
        MitoPerson person2 = new MitoPerson(2, MitoOccupationStatus.WORKER, null, 30, MitoGender.MALE, true);
        trip2.setPerson(person2);
        MitoZone zone3 = new MitoZone(3, AreaTypes.SGType.CORE_CITY);
        zone3.setDistanceToNearestRailStop(0.5f);
        trip2.setTripOrigin(zone3);
        MitoZone zone4 = new MitoZone(4, AreaTypes.SGType.CORE_CITY);
        trip2.setTripDestination(zone4);

        household2 = new MitoHousehold(2, 24000, 1);
        household2.setHomeZone(zone3);
        household2.addPerson(person2);
        household2.setTripsByPurpose(Collections.singletonList(trip2), Purpose.HBO);
        dataSet.addTrip(trip2);
        dataSet.addZone(zone3);
        dataSet.addZone(zone4);
        dataSet.addHousehold(household2);
        dataSet.addPerson(person2);

    }
}
