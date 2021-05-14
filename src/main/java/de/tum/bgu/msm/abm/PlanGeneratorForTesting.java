package de.tum.bgu.msm.abm;

import de.tum.bgu.msm.data.travelTimes.DummyTravelTimesForABM;
import de.tum.bgu.msm.data.plans.Activity;
import de.tum.bgu.msm.data.plans.Plan;
import de.tum.bgu.msm.data.plans.Purpose;
import de.tum.bgu.msm.data.MitoGender;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoOccupationStatus;
import de.tum.bgu.msm.data.MitoPerson;
import junitx.framework.FileAssert;
import org.locationtech.jts.geom.Coordinate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class PlanGeneratorForTesting {

    public static void main(String[] args) throws FileNotFoundException {

        new PlanGeneratorForTesting().run();
    }


    void run() throws FileNotFoundException {
        MitoHousehold household = new MitoHousehold(0, 2000, 1);
        household.setHomeLocation(new Coordinate(0,0));
        MitoPerson person = new MitoPerson(0, MitoOccupationStatus.WORKER, null, 35, MitoGender.FEMALE, true);
        household.addPerson(person);
        Plan plan = Plan.initializePlan(person, household);
        person.setPlan(plan);
        int interval_s = 10 * 60;

        PrintWriter out = new PrintWriter("output.csv");
        double time = interval_s/2;
        while (time < ScheduleUtils.endOfTheDay()){
            out.print(time + ",");
            time += interval_s;
        }
        double endTime = time+=interval_s;
        out.print(endTime);
        out.println();


        out.println(plan.logPlan(interval_s));

        //mandatory main tours
        Activity workActivity = new Activity(Purpose.W, 8 * 3600, 16 * 3600, new Coordinate(10000, 0));
        PlanUtils.addMainTour(plan, workActivity, new DummyTravelTimesForABM());
        out.println(plan.logPlan(interval_s));

        //mandatory main subtours and stops
        PlanUtils.addStopBefore(plan, new Activity(Purpose.A, 7.50 * 3600, 7.85*3600, new Coordinate(1500,0)), workActivity, new DummyTravelTimesForABM());
        out.println(plan.logPlan(interval_s));

        PlanUtils.addSubtour(plan, new Activity(Purpose.O, 12*3600, 12.75 * 3600, new Coordinate(13000,0)), new DummyTravelTimesForABM());
        out.println(plan.logPlan(interval_s));

        //discretionary tours
        Activity shoppingActivity = new Activity(Purpose.S, 18 * 3600, 18.40 * 3600, new Coordinate(-2000, 0));
        PlanUtils.addMainTour(plan, shoppingActivity, new DummyTravelTimesForABM());
        out.println(plan.logPlan(interval_s));

        //discretionary tour stops
        PlanUtils.addStopAfter(plan, new Activity(Purpose.R, 18.5*3600, 19.5 * 3600, new Coordinate(-4000, 0)), shoppingActivity, new DummyTravelTimesForABM());
        out.println(plan.logPlan(interval_s));

        out.close();

        FileAssert.assertEquals("jobs are different.", new File("output_ref.csv"), new File("output.csv"));
    }

}
