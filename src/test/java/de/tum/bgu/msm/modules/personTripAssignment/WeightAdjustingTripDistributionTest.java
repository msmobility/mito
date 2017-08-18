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
import java.util.Map;
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

        MitoTrip tripHBW1 = new MitoTrip(1, 1, Purpose.HBW);
        MitoTrip tripHBW2 = new MitoTrip(2, 1, Purpose.HBW);
        MitoTrip tripHBW3 = new MitoTrip(3, 1, Purpose.HBW);

        List<MitoTrip> trips = new ArrayList<>();
        trips.add(tripHBW1);
        trips.add(tripHBW2);
        trips.add(tripHBW3);
        household.getTripsByPurpose().put(Purpose.HBW, trips);

        TripDistributionFactory factory = new WeightAdjustingTripDistributionFactory();
        TripDistribution distribution = factory.createTripDistribution();
        Map<MitoPerson, Double> probabilitiesByPersonForTrip1 = distribution.getProbabilityByPersonForTrip(household, tripHBW1);

        assertEquals( 1, probabilitiesByPersonForTrip1.get(worker), 0.);
        tripHBW1.setPerson(worker);
        Map<MitoPerson, Double> probabilitiesByPersonForTrip2 = distribution.getProbabilityByPersonForTrip(household, tripHBW2);
        assertEquals( 0.5,probabilitiesByPersonForTrip2.get(worker), 0.);
        tripHBW2.setPerson(worker);
        Map<MitoPerson, Double> probabilitiesByPersonForTrip3 = distribution.getProbabilityByPersonForTrip(household, tripHBW3);
        assertEquals( 0.33, probabilitiesByPersonForTrip3.get(worker), 0.01);
    }
}
