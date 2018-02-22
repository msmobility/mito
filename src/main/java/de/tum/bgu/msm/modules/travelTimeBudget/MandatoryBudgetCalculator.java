package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.util.concurrent.ConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.Collection;

public class MandatoryBudgetCalculator implements ConcurrentFunction{

    private static final Logger logger = Logger.getLogger(TravelTimeBudgetModule.class);

    private final Collection<MitoHousehold> households;
    private final Purpose purpose;
    private final Occupation occupation;
    private final TravelTimes travelTimes;
    private final double timeOfDay;
    private int ignored = 0;

    public MandatoryBudgetCalculator(Collection<MitoHousehold> households, Purpose purpose, TravelTimes travelTimes, double timeOfDay) {
        this.households = households;
        this.purpose = purpose;
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
    public void execute() {
        for(MitoHousehold household: households) {
            double budget = 0;
            for (MitoPerson person : household.getPersons().values()) {
                if (person.getOccupation().equals(occupation)) {
                    if (person.getOccupationZone() == null) {
                        logger.debug(occupation + " with workzone null will not be considered for travel time budget.");
                        ignored++;
                        continue;
                    }
                    budget += travelTimes.getTravelTime(household.getHomeZone().getId(), person.getOccupationZone().getId(), timeOfDay);
                }
            }
            household.setTravelTimeBudgetByPurpose(purpose, budget);
        }
        if (ignored > 0) {
            logger.warn("There have been " + ignored + " " + occupation + " that were ignored in the " + purpose + " travel time budgets"
                    + " because they had no workzone assigned.");
        }
    }
}
