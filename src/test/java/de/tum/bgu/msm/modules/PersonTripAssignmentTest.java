package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoTrip;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersonTripAssignmentTest {

    private DataSet dataSet;

    @Before
    public void setupAndRun() {
        MitoUtil.initializeRandomNumber(new Random(42));
        dataSet = new DataSet();
        String[] purposes = {"HBW", "HBE","HBS", "HBO", "NHBW", "NHBO"};
        dataSet.setPurposes(purposes);

        dataSet.setTripDistribution(new TripDistribution() {
            @Override
            public double getWeight(MitoPerson person, String purpose) {
                return 1;
            }
        });

        MitoHousehold household = new MitoHousehold(1, 5, 3, 2, 0, 1, 2, 2, 2, 1, 1, 1);
        household.getPersons().add(new MitoPerson(1, 1, 1, 1));
        household.getPersons().add(new MitoPerson(2, 1, 1, 1));
        household.getPersons().add(new MitoPerson(3, 1, 3, 1));
        household.getPersons().add(new MitoPerson(4, 1, 3, 1));
        dataSet.getHouseholds().put(household.getHhId(), household);

        MitoTrip tripHBW = new MitoTrip(1, 1, 0, 1);
        household.getTripsByPurpose().put(0, Collections.singletonList(tripHBW));
        MitoTrip tripHBE = new MitoTrip(2, 1, 1, 1);
        household.getTripsByPurpose().put(1, Collections.singletonList(tripHBE));
        MitoTrip tripHBS = new MitoTrip(3, 1, 2, 1);
        household.getTripsByPurpose().put(2, Collections.singletonList(tripHBS));
        MitoTrip tripHBO = new MitoTrip(4, 1, 3, 1);
        household.getTripsByPurpose().put(3, Collections.singletonList(tripHBO));
        MitoTrip tripNHBW = new MitoTrip(5, 1, 4, 1);
        household.getTripsByPurpose().put(4, Collections.singletonList(tripNHBW));
        MitoTrip tripNHBO = new MitoTrip(6, 1, 5, 1);
        household.getTripsByPurpose().put(5, Collections.singletonList(tripNHBO));

        dataSet.getTrips().put(1, tripHBW);
        dataSet.getTrips().put(2, tripHBE);
        dataSet.getTrips().put(3, tripHBS);
        dataSet.getTrips().put(4, tripHBO);
        dataSet.getTrips().put(5, tripNHBW);
        dataSet.getTrips().put(6, tripNHBO);

        Person2TripAssignment assignment = new Person2TripAssignment(dataSet);
        assignment.run();
    }

    @Test
    public void testAssignment() {


        for(MitoTrip trip: dataSet.getTrips().values()) {
            assertNotNull("No Person set for trip " + trip, trip.getPerson());
            if(dataSet.getPurposes()[trip.getTripPurpose()] == "HBW") {
                assertEquals(1, trip.getPerson().getOccupation());
            } else if( dataSet.getPurposes()[trip.getTripPurpose()] == "HBE") {
                assertEquals(3, trip.getPerson().getOccupation());
            }
        }
    }
}
