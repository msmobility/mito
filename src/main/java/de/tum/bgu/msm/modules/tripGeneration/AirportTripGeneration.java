package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportTripGeneration {

    private final DataSet dataSet;
    final static AtomicInteger TRIP_ID_COUNTER = new AtomicInteger();
    private int counter = 0;
    private final static Logger LOGGER = Logger.getLogger(AirportTripGeneration.class);
    private final int airportZone;
    private AirportTripRateCalculator calculator;

    public AirportTripGeneration(DataSet dataSet) {
        this.dataSet = dataSet;
        this.TRIP_ID_COUNTER.set(dataSet.getTrips().size());
        this.airportZone = Resources.INSTANCE.getInt(Properties.AIRPORT_ZONE);
        this.calculator = new AirportTripRateCalculator(new InputStreamReader(this.getClass().getResourceAsStream("AirportTripRateCalc")));
    }

    public void run() {
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {
            List<MitoTrip> tripsByHh = new ArrayList<>();
            for (MitoPerson pp : hh.getPersons().values()) {
                if (MitoUtil.getRandomObject().nextDouble() < calculator.calculateTripRate(hh, airportZone, dataSet.getTravelDistancesAuto())) {
                    //temporary, the trip rate including access and egress, as well as including residents and visitors
                    MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), Purpose.AIRPORT);
                    dataSet.addTrip(trip);
                    tripsByHh.add(trip);
                    counter++;
                }
            }
            hh.setTripsByPurpose(tripsByHh, Purpose.AIRPORT);
        }
        LOGGER.info("Generated " + counter + " trips to or from the airport");
    }
}
