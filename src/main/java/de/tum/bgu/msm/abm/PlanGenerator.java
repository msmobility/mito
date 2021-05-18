package de.tum.bgu.msm.abm;


import com.google.common.math.LongMath;
import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.DiscretionaryActivityGenerator;
import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.MandatoryActivityGenerator;
import de.tum.bgu.msm.abm.models.planGenerationAndScheduling.SubtourGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Plan;
import org.apache.log4j.Logger;

public class PlanGenerator {

    private final static Logger logger = Logger.getLogger(PlanGenerator.class);

    public void runPlanGenerator(DataSet dataSet){

        MandatoryActivityGenerator mandatoryActivityGenerator = new MandatoryActivityGenerator(dataSet);
        SubtourGenerator subtourGenerator = new SubtourGenerator(dataSet);
        DiscretionaryActivityGenerator discretionaryActivityGenerator = new DiscretionaryActivityGenerator(dataSet);
        int counter = 0;
        for (MitoHousehold household : dataSet.getHouseholds().values()){
            for (MitoPerson person : household.getPersons().values()){
                Plan plan = Plan.initializePlan(person, household);
                mandatoryActivityGenerator.run(plan, person, household);
                subtourGenerator.run(plan, person, household);
                discretionaryActivityGenerator.run(plan, person, household);
                counter++;
                if (LongMath.isPowerOfTwo(counter)){
                 logger.info("Completed " + counter + " persons.");
                }
            }
        }




    }

}