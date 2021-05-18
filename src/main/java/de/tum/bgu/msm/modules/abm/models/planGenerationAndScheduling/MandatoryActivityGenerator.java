package de.tum.bgu.msm.modules.abm.models.planGenerationAndScheduling;

import de.tum.bgu.msm.modules.abm.PlanUtils;
import de.tum.bgu.msm.modules.abm.models.ModuleAbm;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoOccupationStatus;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Activity;
import de.tum.bgu.msm.data.plans.ActivityPurpose;
import de.tum.bgu.msm.data.plans.LegMode;
import de.tum.bgu.msm.data.plans.Plan;

public class MandatoryActivityGenerator extends ModuleAbm {

    public MandatoryActivityGenerator(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(Plan plan, MitoPerson person, MitoHousehold household) {
        //trip generation (1 activity?)
        MitoOccupationStatus mitoOccupationStatus = person.getMitoOccupationStatus();
        if (mitoOccupationStatus.equals(MitoOccupationStatus.STUDENT) || mitoOccupationStatus.equals(MitoOccupationStatus.WORKER)) {
            if (person.getOccupation() != null) {
                Activity mandatoryActivity;

                ActivityPurpose activityPurpose;
                if (mitoOccupationStatus.equals(MitoOccupationStatus.WORKER)) {
                    activityPurpose = ActivityPurpose.W;
                } else {
                    activityPurpose = ActivityPurpose.E;
                }

                int startTime = dataSet.getStartingTimeDistribution().get(activityPurpose).selectTime()*60;
                int duration = dataSet.getActivityDurationDistribution().get(activityPurpose).selectTime()*60;
                mandatoryActivity = new Activity(activityPurpose, startTime, startTime + duration, person.getOccupation().getCoordinate());
                PlanUtils.addMainTourWithMode(plan, mandatoryActivity, dataSet.getTravelTimes(), LegMode.UNKNOWN);
            }

        }

    }

}
