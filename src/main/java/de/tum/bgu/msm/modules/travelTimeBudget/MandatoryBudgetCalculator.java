package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoOccupationStatus;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.Collection;

public class MandatoryBudgetCalculator implements Runnable {

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetModule.class);

    private final double defaultBudget;
    private final Collection<MitoHousehold> households;
    private final Purpose activityPurpose;
    private final MitoOccupationStatus mitoOccupationStatus;
    private final TravelTimes travelTimes;
    private final double timeOfDay;
    private int defaultBudgeted = 0;

    MandatoryBudgetCalculator(Collection<MitoHousehold> households, Purpose activityPurpose, TravelTimes travelTimes, double timeOfDay) {
        this.households = households;
        this.activityPurpose = activityPurpose;
        this.defaultBudget = Resources.instance.getDouble(Properties.DEFAULT_BUDGET + activityPurpose, 30.);
        this.travelTimes = travelTimes;
        this.timeOfDay = timeOfDay;
        if(activityPurpose == Purpose.HBW) {
            mitoOccupationStatus = MitoOccupationStatus.WORKER;
        } else if(activityPurpose == Purpose.HBE) {
            mitoOccupationStatus = MitoOccupationStatus.STUDENT;
        } else {
            throw new RuntimeException("MandatoryBudgetCalculator can only be initialized with HBW or HBE activityPurpose!");
        }
    }

    @Override
    public void run() {
        for (MitoHousehold household : households) {
            double budget = 0;
            for (MitoTrip trip : household.getTripsForPurpose(activityPurpose)) {
                if (specifiedByOccupation(trip)) {
                    //Multiply by 2, as the budget should contain the return trip of home based trips as well
                    budget += 2 * travelTimes.getTravelTime(household.getHomeZone(),
                            trip.getPerson().getOccupation(), timeOfDay, TransportMode.car);
                } else {
                    budget += defaultBudget;
                    defaultBudgeted ++;
                }
            }
            household.setTravelTimeBudgetByPurpose(activityPurpose, budget);
        }
        if (defaultBudgeted > 0) {
            logger.warn("There have been " + defaultBudgeted + " " + activityPurpose
                    + " trips that were accounted for with the default budget of "
                    + defaultBudget + " minutes in the " + activityPurpose + " travel time budgets"
                    + " because no " + mitoOccupationStatus + " was assigned (or occupation zone missing).");
        }
    }

    private boolean specifiedByOccupation(MitoTrip trip) {
        return trip.getPerson().getMitoOccupationStatus().equals(mitoOccupationStatus)
                && trip.getPerson().getOccupation() != null
                && trip.getPerson().getOccupation().getOccupationZone() != null;
    }
}
