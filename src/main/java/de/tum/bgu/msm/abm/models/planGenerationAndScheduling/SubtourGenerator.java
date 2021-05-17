package de.tum.bgu.msm.abm.models.planGenerationAndScheduling;

import de.tum.bgu.msm.abm.models.ModuleAbm;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;

public class SubtourGenerator extends ModuleAbm {

    public SubtourGenerator(DataSet dataSetAbm) {
        super(dataSetAbm);
    }

    @Override
    public void run(Plan Plan, MitoPerson person, MitoHousehold household) {

        //trip generation: how many activities (similar to a trip generation model)

        //schedule as main tour but contained in an existing main tour

        //destination choice

        //mode

        //add to plan
    }
}
