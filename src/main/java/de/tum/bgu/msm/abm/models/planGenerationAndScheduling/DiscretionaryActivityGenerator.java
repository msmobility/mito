package de.tum.bgu.msm.abm.models.planGenerationAndScheduling;

import de.tum.bgu.msm.abm.models.ModuleAbm;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;

public class DiscretionaryActivityGenerator extends ModuleAbm {

    public DiscretionaryActivityGenerator(DataSet dataSetAbm) {
        super(dataSetAbm);
    }

    @Override
    public void run(Plan plan, MitoPerson person, MitoHousehold household) {

        //trip generation: how many activities (similar to a trip generation model)

        //schedule as main tour or as stop in an existing tour, based on already existing activities

        //destination choice (different if main tour or if stop)

        //mode (different if main tour or if stop)

        //add to plan
    }

}
