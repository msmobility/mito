package de.tum.bgu.msm.abm.models.planGenerationAndScheduling;

import de.tum.bgu.msm.abm.models.ModuleAbm;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;

public class MandatoryActivityGenerator extends ModuleAbm {

    public MandatoryActivityGenerator(DataSet dataSetAbm) {
        super(dataSetAbm);
    }

    @Override
    public void run(Plan Plan, MitoPerson person, MitoHousehold household) {

        //trip generation (1 activity?)

        //destinaiton choice (given by SP)

        //scehdule as main tour (single tour)

        //mode (main mode tour)

        //add to plan

    }


}
