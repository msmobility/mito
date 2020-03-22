package de.tum.bgu.msm.run.scenarios.drtNoise;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.GeometryUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.List;

public class CropPlans {

    public static void main(String[] args) {

        final Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\cleverShuttle.shp");
        final SimpleFeature feature = features.iterator().next();

        final Geometry initialGeometry = (Geometry) feature.getDefaultGeometry();

        Network fullNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(fullNetwork).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\pt\\studyNetworkDenseMerged2020.xml.gz");
        // new MatsimNetworkReader(fullNetwork).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\pt\\mergedNetwork2018.xml.gz");


        Network croppedNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(croppedNetwork).readFile("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedDenseNetwork.xml.gz");
        //new MatsimNetworkReader(croppedNetwork).readFile("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedCoarse.xml.gz");

        new NetworkCleaner().run(croppedNetwork);
        //new NetworkWriter(croppedNetwork).writeV2("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedDenseNetwork.xml.gz");


        MutableScenario scenario = ScenarioUtils.createMutableScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\scenOutput\\defaultMito100p\\2011\\trafficAssignment\\mito_assignment.output_plans.xml.gz");
        //new PopulationReader(scenario).readFile("D:\\resultStorage\\silo\\defaultMitoCoarse\\2011\\trafficAssignment\\mito_assignment.output_plans.xml.gz");

        Population croppedPopulation = PopulationUtils.createPopulation(ConfigUtils.createConfig());

        final Collection<? extends Person> values = scenario.getPopulation().getPersons().values();


        // final int partitionSize = (int) ((double) values.size() / 16);
        //Iterable<? extends List<? extends Person>> partitions = Iterables.partition(values, partitionSize);
        //ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(16);

        //for (List<? extends Person> partition : partitions) {
        //  executor.addTaskToQueue(() -> {
        //  try {
        Geometry geom;
        PreparedGeometry geometry = PreparedGeometryFactory.prepare(initialGeometry);


        for (Person person : values) {
            // try {
            final Plan selectedPlan = person.getSelectedPlan();
            Person personCopy = croppedPopulation.getFactory().createPerson(person.getId());
            Plan planCopy = croppedPopulation.getFactory().createPlan();
            Activity destination = null;
            Activity origin = null;

            boolean complete = true;

            final List<Leg> legs = PopulationUtils.getLegs(selectedPlan);
            for (Leg leg : legs) {
                Activity previous = PopulationUtils.getPreviousActivity(selectedPlan, leg);
                Activity next = PopulationUtils.getNextActivity(selectedPlan, leg);

                final boolean previousInArea = isInArea(geometry, previous, croppedNetwork);
                final boolean nextInArea = isInArea(geometry, next, croppedNetwork);
                if (!previousInArea && !nextInArea) {
                    continue;
                } else if (previousInArea && nextInArea) {
                    if (!planCopy.getPlanElements().contains(previous)) {
                        planCopy.addActivity(previous);
                    }
                    planCopy.addLeg(PopulationUtils.createLeg("car"));
                    planCopy.addActivity(next);
                } else if (previousInArea) {
                    if (!planCopy.getPlanElements().contains(previous)) {
                        planCopy.addActivity(previous);
                    }

                    if (destination == null) {
                        double insideLength = 0;
                        Id<Link> last = null;
                        for (Id<Link> linkId : ((NetworkRoute) leg.getRoute()).getLinkIds()) {
                            if (isWithinArea(geometry, linkId, croppedNetwork)) {
                                insideLength += fullNetwork.getLinks().get(linkId).getLength();
                                last = linkId;
                            } else {
                                break;
                            }
                        }

                        if (last != null) {
                            final Activity activity = PopulationUtils.createActivityFromLinkId(next.getType(), last);
                            final Leg car = PopulationUtils.createLeg("car");
                            origin = activity;

                            double totalDistance = leg.getRoute().getDistance();
                            double outsideDistance = totalDistance - insideLength;
                            double approxTime = outsideDistance / 25.;
                            if(next.getEndTime().isDefined()) {
                                activity.setEndTime(next.getEndTime().seconds() + approxTime);
                            }
                            planCopy.addLeg(car);
                            planCopy.addActivity(activity);
                        } else {
                            complete = false;
                            break;
                        }
                    } else {
                        planCopy.addLeg(PopulationUtils.createLeg("car"));
                        final Activity activity = PopulationUtils.createActivity(destination);
                        activity.setEndTimeUndefined();
                        planCopy.addActivity(activity);
                    }
                } else if (nextInArea) {
                    final Leg car = PopulationUtils.createLeg("car");

                    if (origin == null) {
                        double outSideLength = 0;
                        Id<Link> first = null;
                        for (Id<Link> linkId : ((NetworkRoute) leg.getRoute()).getLinkIds()) {
                            if (isWithinArea(geometry, linkId, croppedNetwork)) {
                                first = linkId;
                                break;
                            } else {
                                outSideLength += fullNetwork.getLinks().get(linkId).getLength();
                            }
                        }

                        if (first != null) {
                            final Activity activity = PopulationUtils.createActivityFromLinkId(previous.getType(), first);

                            double approxTime = outSideLength / 15.;
                            destination = activity;
                            activity.setEndTime(previous.getEndTime().seconds() + approxTime);
                            planCopy.addActivity(activity);
                            planCopy.addLeg(car);
                            planCopy.addActivity(next);
                        } else {
                            complete = false;
                            break;
                        }
                    } else {
                        planCopy.addLeg(car);
                        planCopy.addActivity(next);
                    }
                }
            }
            if (complete && planCopy.getPlanElements().size() == 3 || planCopy.getPlanElements().size() == 5) {
                personCopy.addPlan(planCopy);
                personCopy.setSelectedPlan(planCopy);
                croppedPopulation.addPerson(personCopy);
            }
        }

        //   } catch (Exception e) {
        //  throw new RuntimeException(e);
        // }
        //  return null;
        //   });
        // }
        //  executor.execute();
        new PopulationWriter(croppedPopulation).write("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedPopulation.xml.gz");






/*
                final List<Activity> activities = PopulationUtils.getActivities(selectedPlan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

                Activity previous = null;
                for (Activity activity : activities) {

                    if(isInArea(geometry, previous) && isInArea(geometry, activity)) {
                        //build route between both
                    } else if(isInArea(geometry, previous)) {
                        // build route from previous to border
                    } else if(isInArea(geometry, activity)) {
                        //build route from
                    }

                    if (isInArea(geometry, activity.getCoord())) {
                        planCopy.addActivity(PopulationUtils.createActivity(activity));
                    }

                    previous = activity;
                }

                Activity activity = PopulationUtils.getFirstActivity(selectedPlan);
                if (isInArea(geometry, (activity).getCoord())) {

                }


                for (PlanElement element : selectedPlan.getPlanElements()) {
                    if (element instanceof Activity) {
                        if (isInArea(geometry, ((Activity) element).getCoord())) {
                            final Activity activity = PopulationUtils.createActivity((Activity) element);
                            planCopy.addActivity(activity);
                        }
                    } else if (element instanceof Leg) {

                    }
                }*/

/*
                Activity lastCopiedActivity = null;
                for (PlanElement planElement : selectedPlan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        lastCopiedActivity = (Activity) planElement;
                    } else if (planElement instanceof Leg) {
                        if(!isInArea(geometry, PopulationUtils.getPreviousActivity(selectedPlan, (Leg) planElement))
                            && !isInArea(geometry, PopulationUtils.getNextActivity(selectedPlan, (Leg) planElement))) {
                            continue;
                        }
                        final Route route = ((Leg) planElement).getRoute();
                        double outOfAreaDistanceBefore = 0;
                        if (route instanceof NetworkRoute) {
                            final Id<Link> startLinkId = route.getStartLinkId();

                            Id<Link> startLinkIdCopy = null;
                            List<Id<Link>> routeCopy = new ArrayList<>();
                            Id<Link> endLinkIdCopy = null;

                            if (isWithinArea(geometry, startLinkId, croppedNetwork)) {
                                startLinkIdCopy = startLinkId;
                            } else {
                                outOfAreaDistanceBefore += fullNetwork.getLinks().get(startLinkId).getLength();
                            }
                            for (Id<Link> link : ((NetworkRoute) route).getLinkIds()) {
                                if (isWithinArea(geometry, startLinkId, croppedNetwork)) {
                                    if (startLinkIdCopy == null) {
                                        startLinkIdCopy = link;
                                    } else {
                                        routeCopy.add(link);
                                    }
                                } else {
                                    if (startLinkIdCopy == null) {
                                        outOfAreaDistanceBefore += fullNetwork.getLinks().get(link).getLength();
                                    } else {
                                        break;
                                    }
                                }
                            }

                            if (!routeCopy.isEmpty()) {
                                if (isWithinArea(geometry, route.getEndLinkId(), croppedNetwork)) {
                                    endLinkIdCopy = route.getEndLinkId();
                                } else {
                                    endLinkIdCopy = routeCopy.remove(routeCopy.size() - 1);
                                }
                            }

                            NetworkRoute linkNetworkRouteImpl = null;
                            if (startLinkIdCopy != null && !routeCopy.isEmpty() && endLinkIdCopy != null) {
                                linkNetworkRouteImpl = RouteUtils.createLinkNetworkRouteImpl(startLinkIdCopy, routeCopy, endLinkIdCopy);
                            }

                            if (linkNetworkRouteImpl != null) {
                                double totalDistance = route.getDistance();
                                double fractionOfOutOfAreaDistance = outOfAreaDistanceBefore / totalDistance;

                                double endTime = lastCopiedActivity.getEndTime().seconds() + route.getTravelTime() * fractionOfOutOfAreaDistance;

                                Activity activityCopy = croppedPopulation.getFactory().createActivityFromLinkId(lastCopiedActivity.getType(), linkNetworkRouteImpl.getStartLinkId());
                                activityCopy.setEndTime(endTime);
                                planCopy.addActivity(activityCopy);
                                final Leg car = croppedPopulation.getFactory().createLeg("car");
                                car.setRoute(linkNetworkRouteImpl);
                                planCopy.addLeg(car);

                            }
                        }
                    }
                }

                if (!planCopy.getPlanElements().isEmpty() && planCopy.getPlanElements().size() >= 2) {
                    final PlanElement element = planCopy.getPlanElements().get(planCopy.getPlanElements().size() - 1);
                    if (element instanceof Leg) {
                        Activity activityCopy = croppedPopulation.getFactory().createActivityFromLinkId(lastCopiedActivity.getType(), ((Leg) element).getRoute().getEndLinkId());
                        planCopy.addActivity(activityCopy);
                    }

                    personCopy.setSelectedPlan(planCopy);
                    croppedPopulation.addPerson(personCopy);
                    for (PlanElement pE : planCopy.getPlanElements()) {
                        if (pE instanceof Leg) {
                            ((Leg) pE).setRoute(null);
                            ((Leg) pE).setTravelTime(Time.getUndefinedTime());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }*/
        //}
        // }

    }

    private static boolean isInArea(PreparedGeometry geometry, Activity activity, Network network) {
        return activity != null && isInArea(geometry, activity.getCoord()) && isWithinArea(geometry, activity.getLinkId(), network);
    }

    private static boolean isInArea(PreparedGeometry geometry, Coord coord) {
        return geometry.contains(GeometryUtils.createGeotoolsPoint(coord));
    }

    private static boolean isWithinArea(PreparedGeometry geometry, Id<Link> startLinkId, Network network) {
        final Link link = network.getLinks().get(startLinkId);
        return link != null
                && isInArea(geometry, link.getFromNode().getCoord())
                && isInArea(geometry, link.getToNode().getCoord());
    }
}
