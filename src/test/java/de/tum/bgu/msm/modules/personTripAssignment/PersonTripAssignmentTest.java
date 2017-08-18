package de.tum.bgu.msm.modules.personTripAssignment;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.modules.PersonTripAssignment;
import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;
import de.tum.bgu.msm.resources.Purpose;
import de.tum.bgu.msm.resources.Resources;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PersonTripAssignmentTest {

    private DataSet dataSet;

    public void setupAndRun() {
        MitoUtil.initializeRandomNumber(new Random(42));
        dataSet = new DataSet();

        MitoHousehold household = new MitoHousehold(1, 1, 1, 1);
        household.getPersons().add(new MitoPerson(1, Occupation.WORKER, 1, 35, Gender.MALE, true));
        household.getPersons().add(new MitoPerson(2, Occupation.WORKER, 1,30,Gender.FEMALE, true));
        household.getPersons().add(new MitoPerson(3, Occupation.STUDENT, 1, 10,Gender.FEMALE, false));
        household.getPersons().add(new MitoPerson(4, Occupation.STUDENT, 1, 15,Gender.MALE, false));
        household.getPersons().add(new MitoPerson(5, Occupation.UNEMPLOYED, 1, 70,Gender.FEMALE, false));
        dataSet.getHouseholds().put(household.getHhId(), household);

        MitoTrip tripHBW = new MitoTrip(1, 1, Purpose.HBW);
        household.getTripsByPurpose().put(Purpose.HBW, Collections.singletonList(tripHBW));
        MitoTrip tripHBE = new MitoTrip(2, 1, Purpose.HBE);
        household.getTripsByPurpose().put(Purpose.HBE, Collections.singletonList(tripHBE));
        MitoTrip tripHBS = new MitoTrip(3, 1, Purpose.HBS);
        household.getTripsByPurpose().put(Purpose.HBS, Collections.singletonList(tripHBS));
        MitoTrip tripHBO = new MitoTrip(4, 1, Purpose.HBO);
        household.getTripsByPurpose().put(Purpose.HBO, Collections.singletonList(tripHBO));
        MitoTrip tripNHBW = new MitoTrip(5, 1, Purpose.NHBW);
        household.getTripsByPurpose().put(Purpose.NHBW, Collections.singletonList(tripNHBW));
        MitoTrip tripNHBO = new MitoTrip(6, 1, Purpose.NHBO);
        household.getTripsByPurpose().put(Purpose.NHBO, Collections.singletonList(tripNHBO));

        dataSet.getTrips().put(1, tripHBW);
        dataSet.getTrips().put(2, tripHBE);
        dataSet.getTrips().put(3, tripHBS);
        dataSet.getTrips().put(4, tripHBO);
        dataSet.getTrips().put(5, tripNHBW);
        dataSet.getTrips().put(6, tripNHBO);

        PersonTripAssignment assignment = new PersonTripAssignment(dataSet);
        assignment.run();
    }

    @Test
    public void testAssignment() {
        Resources.INSTANCE.setTripDistributionFactory(new SimpleTripDistributionFactory());
        setupAndRun();
        for(MitoTrip trip: dataSet.getTrips().values()) {
            assertNotNull("No Person set for trip " + trip, trip.getPerson());
            if(trip.getTripPurpose().equals(Purpose.HBW)) {
                assertEquals(Occupation.WORKER, trip.getPerson().getOccupation());
            } else if(trip.getTripPurpose().equals(Purpose.HBE)) {
                assertEquals(Occupation.STUDENT, trip.getPerson().getOccupation());
            }
        }
    }

    @Test
    public void testFailedAssignment() {
        Resources.INSTANCE.setTripDistributionFactory(() -> (household, trip) -> null);
        setupAndRun();
        assertTrue(dataSet.getTrips().isEmpty());
        for(MitoHousehold household: dataSet.getHouseholds().values()) {
            for(List<MitoTrip> trips: household.getTripsByPurpose().values()) {
                assertTrue(trips.isEmpty());
            }
        }
    }
}
