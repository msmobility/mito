package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.MitoHousehold;

public interface TravelTimeBudgetCalculator {
    double calculateBudget(MitoHousehold household, String activityPurpose);
}
