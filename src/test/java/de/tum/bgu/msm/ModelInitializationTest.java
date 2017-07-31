package de.tum.bgu.msm;

import com.pb.common.matrix.IdentityMatrix;
import com.pb.common.matrix.Matrix;
import com.pb.common.matrix.SquareMatrix;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.InputFeed;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

public class ModelInitializationTest {

    private MitoModel model;

    @Before
    public void setupTest() {
        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
        model = new MitoModel(bundle);
        model.setBaseDirectory("./testInput/");
    }

    @Test
    public final void standaloneInitialization() {
        model.initializeStandAlone();
        testSetInput();
    }

    @Test
    public final void fedInitialization() {

        int[] zone = {1};

        Matrix autoTravelTimes = new IdentityMatrix(2);
        Matrix transitTravelTimes = new IdentityMatrix(2);
        MitoHousehold[] mitoHouseholds = {new MitoHousehold(1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1)};
        MitoPerson[] mitoPersons = {new MitoPerson(1, 1, 1, 1), new MitoPerson(2, 1, 1, 1)};
        int[] retail = {1};
        int[] office = {1};
        int[] other = {1};
        int[] total = {3};
        float[] zoneSize = {1};

        InputFeed feed = new InputFeed(zone, autoTravelTimes, transitTravelTimes, mitoHouseholds, mitoPersons, retail, office, other, total, zoneSize);
        model.feedData(feed);
        testSetInput();
    }

    private void testSetInput() {
        Assert.assertEquals(1, model.getTravelDemand().getZones().size());
        Assert.assertEquals(6, model.getTravelDemand().getPurposes().length);
        Assert.assertEquals(1, model.getTravelDemand().getHouseholds().size());
        Assert.assertEquals(2, model.getTravelDemand().getPersons().size());
        Assert.assertNotNull(model.getTravelDemand().getTripAttractionRates());
        Assert.assertNotNull(model.getTravelDemand().getTravelSurveyHouseholdTable());
        Assert.assertEquals(1., model.getTravelDemand().getAutoTravelTimeFromTo(1, 1), 0.);
        Assert.assertEquals(1., model.getTravelDemand().getTransitTravelTimedFromTo(1, 1), 0.);
    }
}
