package de.tum.bgu.msm.data.plans;
import de.tum.bgu.msm.modules.abm.ScheduleUtils;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;

import java.util.SortedMap;
import java.util.TreeMap;

public class Plan {

    private MitoPerson person;
    private MitoHousehold household;
    private SortedMap<Double, Activity> homeActivities;
    private SortedMap<Double, Tour> tours;

    private Plan() {

    }

    public SortedMap<Double, Tour> getTours() {
        return tours;
    }

    public static Plan initializePlan(MitoPerson person, MitoHousehold household) {
        Plan plan = new Plan();
        plan.person = person;
        plan.household = household;
        plan.homeActivities = new TreeMap<>();
        plan.homeActivities.put(0., new Activity(ActivityPurpose.H, ScheduleUtils.startOfTheDay(), ScheduleUtils.endOfTheDay(), household.getCoordinate()));
        plan.tours = new TreeMap<>();
        person.setPlan(plan);
        return plan;
    }


    public MitoPerson getPerson() {
        return person;
    }

    public SortedMap<Double, Activity> getHomeActivities() {
        return homeActivities;
    }


    public String logPlan(double interval_s) {
        double time = ScheduleUtils.startOfTheDay() + interval_s / 2;
        StringBuilder string = new StringBuilder();
        int size = 0;
        while (time <= ScheduleUtils.endOfTheDay()) {
            for (Activity a : homeActivities.values()) {
                if (time <= a.getEndTime_s() && time > a.getStartTime_s()) {
                    string.append(a.getPurpose().toString()).append(",");
                    size++;
                    time += interval_s;
                    break;
                }
            }
            for (Tour tour : tours.values()) {
                for (Activity a : tour.getActivities().values()) {
                    if (time <= a.getEndTime_s() && time > a.getStartTime_s()) {
                        string.append(a.getPurpose().toString()).append(",");
                        size++;
                        time += interval_s;
                        break;
                    }
                }
                for (Leg t : tour.getTrips().values()) {
                    if (time >= t.getPreviousActivity().getEndTime_s() && time < t.getNextActivity().getStartTime_s()) {
                        string.append("T" + ",");
                        size++;
                        time += interval_s;
                        break;
                    }
                }
                for (Tour subtour : tour.getSubtours().values()) {
                    for (Activity a : subtour.getActivities().values()) {
                        if (time <= a.getEndTime_s() && time > a.getStartTime_s()) {
                            string.append(a.getPurpose().toString()).append(",");
                            size++;
                            time += interval_s;
                            break;
                        }
                    }
                    for (Leg t : subtour.getTrips().values()) {
                        if (time >= t.getPreviousActivity().getEndTime_s() && time < t.getNextActivity().getStartTime_s()) {
                            string.append("T" + ",");
                            size++;
                            time += interval_s;
                            break;
                        }
                    }
                }

            }



        }
        string.append("H");
        return string.toString();
    }

}
