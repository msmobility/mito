package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.tripGeneration.HouseholdType;
import de.tum.bgu.msm.modules.tripGeneration.HouseholdTypeManager;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
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

    private Zone zone;

    @Before
    public void setupTest() {
        ResourceBundle bundle = MitoUtil.createResourceBundle("./testInput/test.properties");
        Resources.INSTANCE.setResources(bundle);
        zone = new Zone(1, 10);
        zone.setRegion(1);
    }

    @Test
    public final void test() {

        DataSet dataSet = new DataSet();
        HouseholdTypeManager manager = new HouseholdTypeManager(dataSet, Purpose.HBW);
        List<HouseholdType> types = manager.createHouseHoldTypeDefinitions();
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
