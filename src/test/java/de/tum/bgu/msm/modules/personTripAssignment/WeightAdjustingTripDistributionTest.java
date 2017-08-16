package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class WeightAdjustingTripDistributionTest {
    @Before
    public void setup() {
        MitoUtil.initializeRandomNumber(new Random(42));
    }

    @Test
    public void testAssignment() {

        MitoPerson worker = new MitoPerson(1, Occupation.WORKER, 1, 35, Gender.MALE, true);
        MitoPerson student = new MitoPerson(4, Occupation.STUDENT, 1, 15,Gender.MALE, false);
        MitoPerson retiree = new MitoPerson(5, Occupation.UNEMPLOYED, 1, 70,Gender.FEMALE, false);

        MitoHousehold household = new MitoHousehold(1,  1, 1, 1);
        household.getPersons().add(worker);
        household.getPersons().add(student);
        household.getPersons().add(retiree);

        MitoTrip tripHBW1 = new MitoTrip(1, 1, Purpose.HBW, 1);
        MitoTrip tripHBW2 = new MitoTrip(2, 1, Purpose.HBW, 1);
        MitoTrip tripHBW3 = new MitoTrip(3, 1, Purpose.HBW, 1);

        List<MitoTrip> trips = new ArrayList<>();
        trips.add(tripHBW1);
        trips.add(tripHBW2);
        trips.add(tripHBW3);
        household.getTripsByPurpose().put(Purpose.HBW, trips);

        TripDistributionFactory factory = new WeightAdjustingTripDistributionFactory();
        TripDistribution distribution = factory.createTripDistribution();

        assertEquals( 1, distribution.getWeight(household, worker, tripHBW1), 0.);
        tripHBW1.setPerson(worker);
        assertEquals( 0.5, distribution.getWeight(household, worker, tripHBW2), 0.);
        tripHBW2.setPerson(worker);
        assertEquals( 0.33, distribution.getWeight(household, worker, tripHBW3), 0.01);
    }
}
