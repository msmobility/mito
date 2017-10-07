package de.tum.bgu.msm;

import com.pb.common.matrix.IdentityMatrix;
import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.input.InputFeed;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SimpleRunTest {

    private MitoModel model;

    @Before
    public void setupTest() {
        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
        model = new MitoModel(bundle);
        model.setBaseDirectory("./testInput/");
    }

    @Test
    public final void fedInitialization() {

        Map<Integer, Zone> zones = new HashMap();
        zones.put(1, new Zone(1));

        Matrix autoTravelTimes = new IdentityMatrix(2);
        Matrix transitTravelTimes = new IdentityMatrix(2);

        Map<Integer, MitoHousehold> households = new HashMap();
        MitoHousehold household = new MitoHousehold(1, 1, 1, zones.get(1));
        households.put(1, household);
        Map<Integer, MitoPerson> persons = new HashMap();
        MitoPerson person = new MitoPerson(1, Occupation.WORKER, 1, 1, Gender.MALE, true);
        persons.put(1, person);
        household.addPerson(person);
        MitoPerson person2 = new MitoPerson(2, Occupation.UNEMPLOYED, -1, 1, Gender.FEMALE, true);
        household.addPerson(person2);

        InputFeed feed = new InputFeed(zones, autoTravelTimes, transitTravelTimes, households);
        model.feedData(feed);
        testSetInput();
    }

    private void testSetInput() {
        Assert.assertEquals(1, model.getTravelDemand().getZones().size());
        Assert.assertEquals(1, model.getTravelDemand().getHouseholds().size());
        Assert.assertEquals(2, model.getTravelDemand().getPersons().size());
        Assert.assertNotNull(model.getTravelDemand().getTripAttractionRates());
        Assert.assertNotNull(model.getTravelDemand().getSurvey());
        Assert.assertEquals(1., model.getTravelDemand().getAutoTravelTimes().getTravelTimeFromTo(new Zone(1,10), new Zone(1,10)), 0.);

        model.runModel();
    }
}
