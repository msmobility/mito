package de.tum.bgu.msm.modules.personTripAssignment;

import com.google.common.collect.Lists;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Gender;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.Implementation;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

public class PersonTripAssignmentTest {

    private DataSet dataSet;

    public void setupAndRun() {
        MitoUtil.initializeRandomNumber(new Random(42));
        dataSet = new DataSet();

        MitoHousehold household = new MitoHousehold(1, 1, 1, null);
        household.addPerson(new MitoPerson(1, Occupation.WORKER, 1, 35, Gender.MALE, true));
        household.addPerson(new MitoPerson(2, Occupation.WORKER, 1, 30, Gender.FEMALE, true));
        household.addPerson(new MitoPerson(3, Occupation.STUDENT, 1, 10, Gender.FEMALE, false));
        household.addPerson(new MitoPerson(4, Occupation.STUDENT, 1, 15, Gender.MALE, false));
        household.addPerson(new MitoPerson(5, Occupation.UNEMPLOYED, 1, 70, Gender.FEMALE, false));
        dataSet.addHousehold(household);

        MitoTrip tripHBW = new MitoTrip(1, Purpose.HBW);
        household.setTripsByPurpose(Lists.newArrayList(tripHBW), Purpose.HBW);
        MitoTrip tripHBE = new MitoTrip(2, Purpose.HBE);
        household.setTripsByPurpose(Lists.newArrayList(tripHBE), Purpose.HBE);
        MitoTrip tripHBS = new MitoTrip(3, Purpose.HBS);
        household.setTripsByPurpose(Lists.newArrayList(tripHBS), Purpose.HBS);
        MitoTrip tripHBO = new MitoTrip(4, Purpose.HBO);
        household.setTripsByPurpose(Lists.newArrayList(tripHBO), Purpose.HBO);
        MitoTrip tripNHBW = new MitoTrip(5, Purpose.NHBW);
        household.setTripsByPurpose(Lists.newArrayList(tripNHBW), Purpose.NHBW);
        MitoTrip tripNHBO = new MitoTrip(6, Purpose.NHBO);
        household.setTripsByPurpose(Lists.newArrayList(tripNHBO), Purpose.NHBO);

        dataSet.addTrip(tripHBW);
        dataSet.addTrip(tripHBE);
        dataSet.addTrip(tripHBS);
        dataSet.addTrip(tripHBO);
        dataSet.addTrip(tripNHBW);
        dataSet.addTrip(tripNHBO);

        PersonTripAssignment assignment = new PersonTripAssignment(dataSet);
        assignment.run();
    }

    @Test
    public void testAssignment() throws IOException {
        Resources.initializeResources("./testInput/test.properties", Implementation.MUNICH);

        Resources.INSTANCE.setTripAssignmentFactory(new SimpleTripAssignmentFactory());
        setupAndRun();
        for (MitoTrip trip : dataSet.getTrips().values()) {
            assertNotNull("No Person set for trip " + trip, trip.getPerson());
            if (trip.getTripPurpose().equals(Purpose.HBW)) {
                assertEquals(Occupation.WORKER, trip.getPerson().getOccupation());
            } else if (trip.getTripPurpose().equals(Purpose.HBE)) {
                assertEquals(Occupation.STUDENT, trip.getPerson().getOccupation());
            }
        }
    }

    @Test
    public void testFailedAssignment() throws IOException {
        Resources.initializeResources("./testInput/test.properties", Implementation.MUNICH);
        Resources.INSTANCE.setTripAssignmentFactory(() -> (household, trip) -> null);
        setupAndRun();
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for(Purpose purpose: Purpose.values()) {
                assertTrue(household.getTripsForPurpose(purpose).isEmpty());
            }
        }
    }
}
