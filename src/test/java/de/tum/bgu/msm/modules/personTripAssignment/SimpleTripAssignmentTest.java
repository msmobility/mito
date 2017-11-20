package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Gender;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SimpleTripAssignmentTest {

    @Before
    public void setup() {
        MitoUtil.initializeRandomNumber(new Random(42));
    }

    @Test
    public void testAssignment() {

        MitoPerson worker = new MitoPerson(1, Occupation.WORKER, 1, 35, Gender.MALE, true);
        MitoPerson student = new MitoPerson(4, Occupation.STUDENT, 1, 15, Gender.MALE, false);
        MitoPerson retiree = new MitoPerson(5, Occupation.UNEMPLOYED, 1, 70, Gender.FEMALE, false);
        MitoHousehold household = new MitoHousehold(1, 1000, 1, null);
        household.addPerson(worker);
        household.addPerson(student);
        household.addPerson(retiree);

        MitoTrip tripHBW = new MitoTrip(1, Purpose.HBW);
        MitoTrip tripHBE = new MitoTrip(2, Purpose.HBE);
        MitoTrip tripHBS = new MitoTrip(3, Purpose.HBS);
        MitoTrip tripHBO = new MitoTrip(4, Purpose.HBO);
        MitoTrip tripNHBW = new MitoTrip(5, Purpose.NHBW);
        MitoTrip tripNHBO = new MitoTrip(6, Purpose.NHBO);

        SimpleTripAssignmentFactory factory = new SimpleTripAssignmentFactory();
        TripAssignment distribution = factory.createTripDistribution();

        Map<MitoPerson, Double> probabilityByPersonForTripHBE = distribution.getProbabilityByPersonForTrip(household, tripHBE);
        Map<MitoPerson, Double> probabilityByPersonForTripHBO = distribution.getProbabilityByPersonForTrip(household, tripHBO);
        Map<MitoPerson, Double> probabilityByPersonForTripHBW = distribution.getProbabilityByPersonForTrip(household, tripHBW);
        Map<MitoPerson, Double> probabilityByPersonForTripHBS = distribution.getProbabilityByPersonForTrip(household, tripHBS);
        Map<MitoPerson, Double> probabilityByPersonForTripNHBW = distribution.getProbabilityByPersonForTrip(household, tripNHBW);
        Map<MitoPerson, Double> probabilityByPersonForTripNHBO = distribution.getProbabilityByPersonForTrip(household, tripNHBO);

        assertEquals(0, probabilityByPersonForTripHBE.get(worker), 0.);
        assertEquals(1, probabilityByPersonForTripHBW.get(worker), 0.);
        assertEquals(1, probabilityByPersonForTripHBO.get(worker), 0.);
        assertEquals(1, probabilityByPersonForTripHBS.get(worker), 0.);
        assertEquals(1, probabilityByPersonForTripNHBW.get(worker), 0.);
        assertEquals(1, probabilityByPersonForTripNHBO.get(worker), 0.);

        assertEquals(1, probabilityByPersonForTripHBE.get(student), 0.);
        assertEquals(0, probabilityByPersonForTripHBW.get(student), 0.);
        assertEquals(1, probabilityByPersonForTripHBO.get(student), 0.);
        assertEquals(1, probabilityByPersonForTripHBS.get(student), 0.);
        assertEquals(1, probabilityByPersonForTripNHBW.get(student), 0.);
        assertEquals(1, probabilityByPersonForTripNHBO.get(student), 0.);

        assertEquals(0, probabilityByPersonForTripHBE.get(retiree), 0.);
        assertEquals(0, probabilityByPersonForTripHBW.get(retiree), 0.);
        assertEquals(1, probabilityByPersonForTripHBO.get(retiree), 0.);
        assertEquals(1, probabilityByPersonForTripHBS.get(retiree), 0.);
        assertEquals(1, probabilityByPersonForTripNHBW.get(retiree), 0.);
        assertEquals(1, probabilityByPersonForTripNHBO.get(retiree), 0.);
    }
}
