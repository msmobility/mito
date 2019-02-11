package de.tum.bgu.msm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.Input;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

public class InitializeFeedModelTest {

    private MitoModel model;

    @Before
    public void setupTest() {

        Map<Integer, MitoZone> zones = new HashMap<>();
        zones.put(1, new MitoZone(1, AreaTypes.SGType.RURAL));

        Map<Integer,SimpleFeature> zoneFeatureMap = new HashMap<>();
        zoneFeatureMap.put(1, null);

        Map<Integer, MitoHousehold> households = new HashMap<>();
        MitoHousehold household = new MitoHousehold(1, 1, 1, zones.get(1));
        households.put(1, household);
        MitoPerson person = new MitoPerson(1, MitoOccupation.WORKER, 1, 1, MitoGender.MALE, true);
        household.addPerson(person);
        MitoPerson person2 = new MitoPerson(2, MitoOccupation.UNEMPLOYED, -1, 1, MitoGender.FEMALE, true);
        household.addPerson(person2);

        Input.InputFeed feed = new Input.InputFeed(zones, new TravelTimes() {
			@Override
			public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
				return 1.;
			}
			
			@Override
			public double getTravelTime(int origin, int destination, double timeOfDay_s, String mode) {
				return 1.;
			}

			@Override
			public double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode) {
				return 0;
			}
		}, (origin, destination) -> 1, households, 2017,zoneFeatureMap);
        model = MitoModel.createModelWithInitialFeed("./testInput/test.properties", feed);
    }

    @Ignore
    public void testInput() {
        Assert.assertEquals(1, model.getData().getZones().size());
        Assert.assertEquals(1, model.getData().getHouseholds().size());
        Assert.assertEquals(2, model.getData().getPersons().size());
        Assert.assertEquals(1., model.getData().getTravelTimes().getTravelTime(1, 1, 0, "car"), 0.);
    }
}
