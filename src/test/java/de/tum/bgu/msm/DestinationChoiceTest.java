package de.tum.bgu.msm;

import com.pb.common.matrix.IdentityMatrix;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.modules.DestinationChoice;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DestinationChoiceTest {

    private DataSet dataSet;

    @Before
    public void initializeAndRun() {
        MitoUtil.initializeRandomNumber(new Random(42));
        dataSet = new DataSet();
        String[] purposes = {"HBS"};
        dataSet.setPurposes(purposes);
        Zone zone1 = new Zone(1);
        zone1.setRetailEmpl(100);
        dataSet.getZones().put(zone1.getZoneId(), zone1);
        Zone zone2 = new Zone(2);
        zone2.setRetailEmpl(100);
        dataSet.getZones().put(zone2.getZoneId(), zone2);

        MitoTrip trip1 = new MitoTrip(1, 0 , 0, 1);
        MitoTrip trip2 = new MitoTrip(2, 0 , 0, 2);
        dataSet.getTrips().put(trip1.getTripId(), trip1);
        dataSet.getTrips().put(trip2.getTripId(), trip2);

        dataSet.setDistanceMatrix(new IdentityMatrix(2));

        DestinationChoice destinationChoice = new DestinationChoice(dataSet);
        destinationChoice.run();


    }

    @Test
    public void testHBSTrips() {
        for(MitoTrip trip: dataSet.getTrips().values()) {
            assertThat(trip.getTripDestination(), not(0));
        }
    }

}
