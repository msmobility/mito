package de.tum.bgu.msm.data.plans;

import java.util.SortedMap;
import java.util.TreeMap;

public class Tour {

    private final SortedMap<Double, Activity> activities;
    private final SortedMap<Activity, Leg> trips;
    private final Activity mainActivity;
    private final SortedMap<Double, Tour> subtours;


    public Tour(Activity mainActivity) {
        this.mainActivity = mainActivity;
        activities = new TreeMap<>();
        activities.put(mainActivity.getStartTime_s(), mainActivity);
        trips = new TreeMap<>();
        subtours = new TreeMap<>();
    }

    public SortedMap<Activity, Leg> getTrips() {
        return trips;
    }

    public SortedMap<Double, Activity> getActivities() {
        return activities;
    }

    public SortedMap<Double, Tour> getSubtours() {
        return subtours;
    }

    public Activity getMainActivity() {
        return mainActivity;
    }
}
