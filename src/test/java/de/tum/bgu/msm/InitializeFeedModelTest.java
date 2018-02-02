package de.tum.bgu.msm;

import cern.colt.matrix.tfloat.FloatFactory2D;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.InputFeed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InitializeFeedModelTest {

    private MitoModel model;

    @Before
    public void setupTest() {

        Map<Integer, MitoZone> zones = new HashMap<>();
        zones.put(1, new MitoZone(1, 10, AreaType.RURAL));

        Map<Integer, MitoHousehold> households = new HashMap<>();
        MitoHousehold household = new MitoHousehold(1, 1, 1, zones.get(1));
        households.put(1, household);
        MitoPerson person = new MitoPerson(1, Occupation.WORKER, 1, 1, Gender.MALE, true);
        household.addPerson(person);
        MitoPerson person2 = new MitoPerson(2, Occupation.UNEMPLOYED, -1, 1, Gender.FEMALE, true);
        household.addPerson(person2);

        Map<String, TravelTimes> map = new LinkedHashMap<>();
        map.put("car", new MatrixTravelTimes(FloatFactory2D.dense.identity(2)));
        map.put("pt", new MatrixTravelTimes(FloatFactory2D.dense.identity(2)));

        InputFeed feed = new InputFeed(zones, map, households);
        model = MitoModel.createModelWithInitialFeed("./testInput/test.properties", Implementation.MUNICH, feed);
    }

    @Test
    public void testInput() {
        Assert.assertEquals(1, model.getData().getZones().size());
        Assert.assertEquals(1, model.getData().getHouseholds().size());
        Assert.assertEquals(2, model.getData().getPersons().size());
        Assert.assertNotNull(model.getData().getSurvey());
        Assert.assertEquals(1., model.getData().getTravelTimes("car").getTravelTime(1, 1, 0), 0.);
    }
}
