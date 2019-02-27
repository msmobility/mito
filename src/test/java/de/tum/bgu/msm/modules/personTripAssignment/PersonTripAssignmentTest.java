package de.tum.bgu.msm.modules.personTripAssignment;

import com.google.common.collect.Lists;
import de.tum.bgu.msm.DummyOccupation;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersonTripAssignmentTest {

    private DataSet dataSet;

    public void setupAndRun() {
        MitoUtil.initializeRandomNumber(new Random(42));
        dataSet = new DataSet();

        MitoHousehold household = new MitoHousehold(1, 1, 1, null);
        household.addPerson(new MitoPerson(1, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 35, MitoGender.MALE, true));
        household.addPerson(new MitoPerson(2, MitoOccupationStatus.WORKER, DummyOccupation.dummy, 30, MitoGender.FEMALE, true));
        household.addPerson(new MitoPerson(3, MitoOccupationStatus.STUDENT, DummyOccupation.dummy, 10, MitoGender.FEMALE, false));
        household.addPerson(new MitoPerson(4, MitoOccupationStatus.STUDENT, DummyOccupation.dummy, 15, MitoGender.MALE, false));
        household.addPerson(new MitoPerson(5, MitoOccupationStatus.UNEMPLOYED, null, 70, MitoGender.FEMALE, false));
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
        Resources.initializeResources("./testInput/test.properties");

        setupAndRun();
        for (MitoTrip trip : dataSet.getTrips().values()) {
            assertNotNull("No Person set for trip " + trip, trip.getPerson());
            if (trip.getTripPurpose().equals(Purpose.HBW)) {
                assertEquals(MitoOccupationStatus.WORKER, trip.getPerson().getMitoOccupationStatus());
            } else if (trip.getTripPurpose().equals(Purpose.HBE)) {
                assertEquals(MitoOccupationStatus.STUDENT, trip.getPerson().getMitoOccupationStatus());
            }
        }
    }
}
