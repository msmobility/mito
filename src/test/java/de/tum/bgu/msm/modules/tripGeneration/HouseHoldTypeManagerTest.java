package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Nico on 26/07/2017.
 */
public class HouseHoldTypeManagerTest {

    private MitoZone zone;

    @Before
    public void setupTest() {
        Resources.initializeResources("./testInput/test.properties");

        zone = new MitoZone(1, AreaTypes.SGType.RURAL);
    }

    @Test
    public final void test() {
        HouseholdTypeManager manager = new HouseholdTypeManager(Purpose.HBW);
        List<HouseholdType> types = manager.householdTypes;
        Assert.assertEquals(20, types.size());
        for(HouseholdType type: types) {
            Assert.assertEquals(0, type.getNumberOfRecords());
        }

        MitoHousehold household1 = new MitoHousehold(1,  4, 1);
        household1.setHomeZone(new MitoZone(1, AreaTypes.SGType.CORE_CITY));
        household1.addPerson(new MitoPerson(1, MitoOccupationStatus.WORKER, null, 30, MitoGender.MALE, true));
        MitoHousehold household2 = new MitoHousehold(2,  4, 1);
        household2.setHomeZone(zone);
        household2.addPerson(new MitoPerson(2, MitoOccupationStatus.WORKER, null, 30, MitoGender.MALE, true));
        Assert.assertNull(manager.determineHouseholdType(household1));

        household2.setEconomicStatus(1);
        HouseholdType determinedType = manager.determineHouseholdType(household2);
        Assert.assertNotNull(determinedType);
        Assert.assertEquals(1, determinedType.getNumberOfRecords());
    }
}
