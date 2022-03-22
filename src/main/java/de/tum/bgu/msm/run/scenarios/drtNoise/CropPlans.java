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

//        final Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("D:\\resultStorage\\moia-msm\\cleverShuttleOperationArea\\cleverShuttle.shp");
        final Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("D:\\resultStorage\\moia-msm\\abmtrans\\shapesServiceAreas\\HolzkirchenServiceArea.shp");
        final SimpleFeature feature = features.iterator().next();

        final Geometry initialGeometry = (Geometry) feature.getDefaultGeometry();

        Network fullNetwork = NetworkUtils.createNetwork();
//        new MatsimNetworkReader(fullNetwork).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\studyNetworkDense.xml");
        new MatsimNetworkReader(fullNetwork).readFile("D:\\resultStorage\\moia-msm\\realisticModeChoice\\networkUpdated.xml");
        // new MatsimNetworkReader(fullNetwork).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\pt\\mergedNetwork2018.xml.gz");


//        Network croppedNetwork = NetworkUtils.createNetwork();
        Network croppedNetwork = fullNetwork;
//        new MatsimNetworkReader(croppedNetwork).readFile("D:\\resultStorage\\moia-msm\\cleverShuttleOperationArea\\croppedDenseNetwork.xml.gz");
        //new MatsimNetworkReader(croppedNetwork).readFile("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedCoarse.xml.gz");

//        new NetworkCleaner().run(croppedNetwork);
        //new NetworkWriter(croppedNetwork).writeV2("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedDenseNetwork.xml.gz");


        MutableScenario scenario = ScenarioUtils.createMutableScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("D:\\resultStorage\\moia-msm\\cleverShuttleOperationArea\\iterateFull\\output_plans.xml.gz");
        //new PopulationReader(scenario).readFile("D:\\resultStorage\\silo\\defaultMitoCoarse\\2011\\trafficAssignment\\mito_assignment.output_plans.xml.gz");

        Population croppedPopulation = PopulationUtils.createPopulation(ConfigUtils.createConfig());

        final Collection<? extends Person> values = scenario.getPopulation().getPersons().values();

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
//        new PopulationWriter(croppedPopulation).write("D:\\resultStorage\\moia-msm\\realisticModeChoice\\outputCar\\croppedPopulationNew.xml.gz");
        new PopulationWriter(croppedPopulation).write("D:\\resultStorage\\moia-msm\\abmtrans\\population\\croppedPopulationHolzkirchen.xml.gz");
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
