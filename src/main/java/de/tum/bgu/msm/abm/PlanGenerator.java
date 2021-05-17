package de.tum.bgu.msm.abm;


import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.DiscretionaryActivityGenerator;
import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.MandatoryActivityGenerator;
import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.SubtourGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;

public class PlanGenerator {


    public void runPlanGenerator(DataSet dataSet){

        MandatoryActivityGenerator mandatoryActivityGenerator = new MandatoryActivityGenerator(dataSet);
        SubtourGenerator subtourGenerator = new SubtourGenerator(dataSet);
        DiscretionaryActivityGenerator discretionaryActivityGenerator = new DiscretionaryActivityGenerator(dataSet);

        for (MitoHousehold household : dataSet.getHouseholds().values()){
            for (MitoPerson person : household.getPersons().values()){
                Plan plan = Plan.initializePlan(person, household);
                mandatoryActivityGenerator.run(plan, person, household);
                subtourGenerator.run(plan, person, household);
                discretionaryActivityGenerator.run(plan, person, household);
            }
        }




    }

}