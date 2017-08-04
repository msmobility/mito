package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.tripGeneration.HouseholdType;
import de.tum.bgu.msm.modules.tripGeneration.HouseholdTypeManager;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Nico on 26/07/2017.
 */
public class HouseHoldTypeManagerTest {

    @Before
    public void setupTest() {
        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
    }

    @Test
    public final void test() {

        DataSet dataSet = new DataSet();
        HouseholdTypeManager manager = new HouseholdTypeManager(dataSet, "testPurpose");
        List<HouseholdType> types = manager.createHouseHoldTypeDefinitions();
        Assert.assertEquals(30, types.size());
        for(HouseholdType type: types) {
            Assert.assertEquals(0, type.getNumberOfRecords());
        }

        MitoHousehold household = new MitoHousehold(1, 3, 2, 1, 0, 0 , 2, 0, 2, 4, 1, 1);
        Assert.assertNull(manager.determineHouseholdType(household));

        Zone zone = new Zone(1, 10);
        zone.setRegion(1);
        dataSet.getZones().put(1, zone);

        HouseholdType determinedType = manager.determineHouseholdType(household);
        Assert.assertNotNull(determinedType);
        Assert.assertEquals(1, determinedType.getNumberOfRecords());
    }
}
