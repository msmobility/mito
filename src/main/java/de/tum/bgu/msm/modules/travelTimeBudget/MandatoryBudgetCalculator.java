package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.Collection;
import java.util.concurrent.Callable;

public class MandatoryBudgetCalculator implements Callable<Void>{

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetModule.class);

    private final double defaultBudget;
    private final Collection<MitoHousehold> households;
    private final Purpose purpose;
    private final Occupation occupation;
    private final TravelTimes travelTimes;
    private final double timeOfDay;
    private int defaultBudgeted = 0;

    MandatoryBudgetCalculator(Collection<MitoHousehold> households, Purpose purpose, TravelTimes travelTimes, double timeOfDay) {
        this.households = households;
        this.purpose = purpose;
        this.defaultBudget = Resources.INSTANCE.getDouble(Properties.DEFAULT_BUDGET + purpose, 30.);
        this.travelTimes = travelTimes;
        this.timeOfDay = timeOfDay;
        if(purpose == Purpose.HBW) {
            occupation = Occupation.WORKER;
        } else if(purpose == Purpose.HBE) {
            occupation = Occupation.STUDENT;
        } else {
            throw new RuntimeException("MandatoryBudgetCalculator can only be initialized with HBW or HBE purpose!");
        }
    }

    @Override
    public Void call() throws Exception {
        for (MitoHousehold household : households) {
            double budget = 0;
            for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                if (specifiedByOccupation(trip)) {
                    budget += travelTimes.getTravelTime(household.getHomeZone().getId(),
                            trip.getPerson().getOccupationZone().getId(), timeOfDay, TransportMode.car);
                } else {
                    budget += defaultBudget;
                    defaultBudgeted ++;
                }
            }
            household.setTravelTimeBudgetByPurpose(purpose, budget);
        }
        if (defaultBudgeted > 0) {
            logger.warn("There have been " + defaultBudgeted + " " + purpose
                    + " trips that were accounted for with the default budget of "
                    + defaultBudget + " minutes in the " + purpose + " travel time budgets"
                    + " because no " + occupation + " was assigned (or occupation zone missing).");
        }
        return null;
    }

    private boolean specifiedByOccupation(MitoTrip trip) {
        return trip.getPerson().getOccupation().equals(occupation) && trip.getPerson().getOccupationZone() != null;
    }
}
