package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SimpleTripDistributionTest {

    @Before
    public void setup() {
        MitoUtil.initializeRandomNumber(new Random(42));
    }

    @Test
    public void testAssignment() {

        MitoPerson worker = new MitoPerson(1, 0, Occupation.WORKER, 1, 35, Gender.MALE, true);
        MitoPerson student = new MitoPerson(4, 0, Occupation.STUDENT, 1, 15,Gender.MALE, false);
        MitoPerson retiree = new MitoPerson(5, 0, Occupation.UNEMPLOYED, 1, 70,Gender.FEMALE, false);

        MitoTrip tripHBW = new MitoTrip(1, 1, Purpose.HBW, 1);
        MitoTrip tripHBE = new MitoTrip(2, 1, Purpose.HBE, 1);
        MitoTrip tripHBS = new MitoTrip(3, 1, Purpose.HBS, 1);
        MitoTrip tripHBO = new MitoTrip(4, 1, Purpose.HBO, 1);
        MitoTrip tripNHBW = new MitoTrip(5, 1, Purpose.NHBW, 1);
        MitoTrip tripNHBO = new MitoTrip(6, 1, Purpose.NHBO, 1);

        SimpleTripDistributionFactory factory = new SimpleTripDistributionFactory();
        TripDistribution distribution = factory.createTripDistribution();

        assertEquals( 0, distribution.getWeight(null, worker, tripHBE), 0.);
        assertEquals( 1, distribution.getWeight(null, worker, tripHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, worker, tripHBO), 0.);
        assertEquals( 1, distribution.getWeight(null, worker, tripHBS), 0.);
        assertEquals( 1, distribution.getWeight(null, worker, tripNHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, worker, tripNHBO), 0.);

        assertEquals( 1, distribution.getWeight(null, student, tripHBE), 0.);
        assertEquals( 0, distribution.getWeight(null, student, tripHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, student, tripHBO), 0.);
        assertEquals( 1, distribution.getWeight(null, student, tripHBS), 0.);
        assertEquals( 1, distribution.getWeight(null, student, tripNHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, student, tripNHBO), 0.);

        assertEquals( 0, distribution.getWeight(null, retiree, tripHBE), 0.);
        assertEquals( 0, distribution.getWeight(null, retiree, tripHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, retiree, tripHBO), 0.);
        assertEquals( 1, distribution.getWeight(null, retiree, tripHBS), 0.);
        assertEquals( 1, distribution.getWeight(null, retiree, tripNHBW), 0.);
        assertEquals( 1, distribution.getWeight(null, retiree, tripNHBO), 0.);

    }

}
