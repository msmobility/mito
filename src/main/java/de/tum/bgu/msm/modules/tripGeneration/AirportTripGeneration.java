package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportTripGeneration {

    private final DataSet dataSet;
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();
    private int counter = 0;
    private final static Logger LOGGER = Logger.getLogger(AirportTripGeneration.class);

    public AirportTripGeneration(DataSet dataSet) {
        this.dataSet = dataSet;
        this.TRIP_ID_COUNTER.set(dataSet.getTrips().size());
    }

    public void run() {
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            for (MitoPerson person : hh.getPersons().values() ) {
                List<MitoTrip> tripsByHh = new ArrayList<>();
                if (MitoUtil.getRandomObject().nextDouble() < 0.006092*2) {
                    //temporary, the trip rate including access and egress, as well as including residents and visitors
                    MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), Purpose.AIRPORT);
                    dataSet.addTrip(trip);
                    tripsByHh.add(trip);
                    hh.setTripsByPurpose(tripsByHh, Purpose.AIRPORT);
                    counter++;
                }
            }
        }
        LOGGER.info("Generated " + counter + " trips to or from the airport");
    }
}
