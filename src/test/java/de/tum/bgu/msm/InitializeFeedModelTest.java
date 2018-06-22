package de.tum.bgu.msm;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.Input;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class InitializeFeedModelTest {

    private MitoModel model;

    @Before
    public void setupTest() {

        Map<Integer, MitoZone> zones = new HashMap<>();
        zones.put(1, new MitoZone(1, 10, AreaTypes.SGType.RURAL));

        Map<Integer, MitoHousehold> households = new HashMap<>();
        MitoHousehold household = new MitoHousehold(1, 1, 1, zones.get(1));
        households.put(1, household);
        MitoPerson person = new MitoPerson(1, Occupation.WORKER, 1, 1, Gender.MALE, true);
        household.addPerson(person);
        MitoPerson person2 = new MitoPerson(2, Occupation.UNEMPLOYED, -1, 1, Gender.FEMALE, true);
        household.addPerson(person2);

        Input.InputFeed feed = new Input.InputFeed(zones, (origin, destination, timeOfDay_s, mode) -> 1, households, 2017);
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
