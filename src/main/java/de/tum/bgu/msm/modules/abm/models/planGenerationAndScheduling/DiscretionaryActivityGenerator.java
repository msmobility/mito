package de.tum.bgu.msm.modules.abm.models.planGenerationAndScheduling;

import de.tum.bgu.msm.data.plans.ActivityPurpose;
import de.tum.bgu.msm.data.plans.Tour;
import de.tum.bgu.msm.data.timeOfDay.AvailableTimeOfDay;
import de.tum.bgu.msm.modules.abm.models.ModuleAbm;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DiscretionaryActivityGenerator extends ModuleAbm {

    public DiscretionaryActivityGenerator(DataSet dataSetAbm) {
        super(dataSetAbm);
    }

    @Override
    public void run(Plan plan, MitoPerson person, MitoHousehold household) {

        AvailableTimeOfDay availableTimeOfDay = new AvailableTimeOfDay();

        for (Tour tour : plan.getTours().values()) {
            availableTimeOfDay.blockTime((int) tour.getTrips().firstKey().getEndTime_s(), (int) tour.getTrips().lastKey().getStartTime_s());
        }


        //trip generation: how many activities (similar to a trip generation model)
        Map<ActivityPurpose, Integer> tripsByPurpose = new HashMap<>();
        Random randomObject = MitoUtil.getRandomObject();
        tripsByPurpose.put(ActivityPurpose.A, randomObject.nextInt(2) - 1);
        tripsByPurpose.put(ActivityPurpose.R, randomObject.nextInt(2) - 1);
        tripsByPurpose.put(ActivityPurpose.S, randomObject.nextInt(2) - 1);
        tripsByPurpose.put(ActivityPurpose.O, randomObject.nextInt(2) - 1);

        //schedule as main tour or as stop in an existing tour, based on already existing activities
        for (ActivityPurpose activityPurpose : tripsByPurpose.keySet()) {
            int trips = tripsByPurpose.get(activityPurpose);
        }
        //destination choice (different if main tour or if stop)

        //mode (different if main tour or if stop)

        //add to plan
    }

}
