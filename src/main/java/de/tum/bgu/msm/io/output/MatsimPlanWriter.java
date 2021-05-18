package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.plans.Activity;
import de.tum.bgu.msm.data.plans.Leg;
import de.tum.bgu.msm.data.plans.Plan;
import de.tum.bgu.msm.data.plans.Tour;
import de.tum.bgu.msm.util.MitoUtil;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

import java.util.SortedMap;
import java.util.TreeMap;

public class MatsimPlanWriter {


    private final DataSet dataSet;
    private final double scaleFactor;


    public MatsimPlanWriter(DataSet dataSet, double scaleFactor) {
        this.dataSet = dataSet;
        this.scaleFactor = scaleFactor;
    }


    public void run(String outputFile){
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        for (MitoHousehold household : dataSet.getHouseholds().values()) {
            for (MitoPerson person : household.getPersons().values()) {
                if (MitoUtil.getRandomObject().nextDouble() < scaleFactor){
                    Person matsimPerson = population.getFactory().createPerson(Id.createPersonId(person.getId()));
                    population.addPerson(matsimPerson);
                    SortedMap<Double, Activity> allActivities = new TreeMap<>();
                    SortedMap<Activity, Leg> allTrips = new TreeMap<>();
                    Plan plan = person.getPlan();
                    allActivities.putAll(plan.getHomeActivities());
                    for (Tour tour : plan.getTours().values()){
                        allActivities.putAll(tour.getActivities());
                        allTrips.putAll(tour.getTrips());
                        for (Tour subtour : tour.getSubtours().values()){
                            allActivities.putAll(subtour.getActivities());
                            allTrips.putAll(subtour.getTrips());
                        }
                    }
                    org.matsim.api.core.v01.population.Plan matsimPlan = PopulationUtils.createPlan();
                    matsimPerson.addPlan(matsimPlan);
                    for (Activity activity : allActivities.values()){
                        Coordinate c = activity.getCoordinate();
                        org.matsim.api.core.v01.population.Activity matsimActivity = PopulationUtils.createActivityFromCoord(activity.getPurpose().toString(), new Coord(c.x, c.y));
                        matsimPlan.addActivity(matsimActivity);
                        Leg leg = allTrips.get(activity);
                        if (leg != null){
                            matsimActivity.setEndTime(activity.getEndTime_s());
                            org.matsim.api.core.v01.population.Leg matsimLeg = PopulationUtils.createLeg(leg.getMode().toString());
                            matsimPlan.addLeg(matsimLeg);
                        }
                    }


                }
            }
        }
        PopulationUtils.writePopulation(population, outputFile);
    }
}
