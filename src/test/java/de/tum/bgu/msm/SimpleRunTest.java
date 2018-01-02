package de.tum.bgu.msm;

import com.pb.common.matrix.IdentityMatrix;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.InputFeed;
import de.tum.bgu.msm.resources.Implementation;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SimpleRunTest {

    private MitoModel model;

    @Before
    public void setupTest() {

        Map<Integer, Zone> zones = new HashMap<>();
        zones.put(1, new Zone(1, 10, AreaType.RURAL));

        Map<Integer, MitoHousehold> households = new HashMap<>();
        MitoHousehold household = new MitoHousehold(1, 1, 1, zones.get(1));
        households.put(1, household);
        Map<Integer, MitoPerson> persons = new HashMap<>();
        MitoPerson person = new MitoPerson(1, Occupation.WORKER, 1, 1, Gender.MALE, true);
        persons.put(1, person);
        household.addPerson(person);
        MitoPerson person2 = new MitoPerson(2, Occupation.UNEMPLOYED, -1, 1, Gender.FEMALE, true);
        household.addPerson(person2);

        Map<String, TravelTimes> map = new LinkedHashMap<>();
        map.put("car", new MatrixTravelTimes(new IdentityMatrix(1)));
        map.put("pt", new MatrixTravelTimes(new IdentityMatrix(1)));

        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        MitoUtil.setBaseDirectory("./testInput/");
        InputFeed feed = new InputFeed(zones, map, households);
        model = MitoModel.createModelWithInitialFeed(bundle, Implementation.MUNICH, feed);
    }

    @Test
    public void testInput() {
        Assert.assertEquals(1, model.getTravelDemand().getZones().size());
        Assert.assertEquals(1, model.getTravelDemand().getHouseholds().size());
        Assert.assertEquals(2, model.getTravelDemand().getPersons().size());
        Assert.assertNotNull(model.getTravelDemand().getSurvey());
        Assert.assertEquals(1., model.getTravelDemand().getTravelTimes("car").getTravelTime(1, 1), 0.);
        model.runModel();
    }
}
