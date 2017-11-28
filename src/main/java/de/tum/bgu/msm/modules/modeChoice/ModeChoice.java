package de.tum.bgu.msm.modules.modeChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;


import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class ModeChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);

    private final ModeChoiceJSCalculator calculator;

    public ModeChoice(DataSet dataSet) {
        super(dataSet);
        logger.info(" Calculating mode choice probabilities for each trip. Modes considered - 1. Auto driver, 2. Auto passenger, 3. Bicycle, 4. Bus, 5. Train, 6. Tram or Metro, 7. Walk ");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoice"));
        calculator = new ModeChoiceJSCalculator(reader);
    }

    @Override
    public void run() {
        for (Purpose purpose : Purpose.values()) {
            calculator.setPurpose(purpose);
            for (MitoHousehold household : dataSet.getHouseholds().values()) {
                calculator.setHouseholdAttributes(household);
                for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                    calculator.setTravelDistance(dataSet.getTravelDistances().getTravelDistanceFromTo(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()));
                    calculator.setPersonAttributes(trip.getPerson());
                    calculator.setAreaTypesAndDistanceToTransit(trip);
                    for (Map.Entry<String, TravelTimes> entry : dataSet.getTravelTimes().entrySet()) {
                        calculator.setTravelTimeForMode(entry.getKey(), entry.getValue().getTravelTimeFromTo(trip.getTripOrigin().getZoneId(), trip.getTripDestination().getZoneId()));
                    }
                    assignMode();
                }
            }
        }
    }

    private void assignMode() {
        // Get probabilities
        // Assign mode based on probabilities
    }
}
