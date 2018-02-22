package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.Implementation;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Nico on 26/07/2017.
 */
public class HouseHoldTypeManagerTest {

    private MitoZone zone;

    @Before
    public void setupTest() {
        try {
            Resources.initializeResources("./testInput/test.properties", Implementation.MUNICH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        zone = new MitoZone(1, 10, AreaType.RURAL);
    }

    @Test
    public final void test() {
        HouseholdTypeManager manager = new HouseholdTypeManager(Purpose.HBW);
        manager.createHouseHoldTypeDefinitions();
        List<HouseholdType> types = manager.householdTypes;
        Assert.assertEquals(24, types.size());
        for(HouseholdType type: types) {
            Assert.assertEquals(0, type.getNumberOfRecords());
        }

        MitoHousehold household1 = new MitoHousehold(1,  4, 1, null);
        household1.addPerson(new MitoPerson(1, Occupation.WORKER, -1, 30, Gender.MALE, true));
        MitoHousehold household2 = new MitoHousehold(2,  4, 1, zone);
        household2.addPerson(new MitoPerson(2, Occupation.WORKER, -1, 30, Gender.MALE, true));
        Assert.assertNull(manager.determineHouseholdType(household1));

        HouseholdType determinedType = manager.determineHouseholdType(household2);
        Assert.assertNotNull(determinedType);
        Assert.assertEquals(1, determinedType.getNumberOfRecords());
    }
}
