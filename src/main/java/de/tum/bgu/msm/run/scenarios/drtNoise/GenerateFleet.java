package de.tum.bgu.msm.run.scenarios.drtNoise;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateFleet {

    public static void main(String[] args)  {

        Integer amountOfVehicles = 10000;
        Integer seats = 6;

        List<Link> linkList = new ArrayList<>();

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/croppedDenseNetwork.xml.gz");
        Link hubLink = scenario.getNetwork().getLinks().get(Id.createLinkId("42251"));
        Link hubLink2 = scenario.getNetwork().getLinks().get(Id.createLinkId("110407"));
        Link hubLink3 = scenario.getNetwork().getLinks().get(Id.createLinkId("123725"));
        Link hubLink4 = scenario.getNetwork().getLinks().get(Id.createLinkId("306831"));
        Link hubLink5 = scenario.getNetwork().getLinks().get(Id.createLinkId("400634"));
        Link hubLink6 = scenario.getNetwork().getLinks().get(Id.createLinkId("182186"));


        linkList.add(hubLink);
        linkList.add(hubLink2);
        linkList.add(hubLink3);
        linkList.add(hubLink4);
        linkList.add(hubLink5);
        linkList.add(hubLink6);


        Network net = scenario.getNetwork();

        List<DvrpVehicleSpecification> vehicles = new ArrayList<>();

        for (int i = 0 ; i < amountOfVehicles ; i++) {
            Random rand = new Random();

            DvrpVehicleSpecification v = ImmutableDvrpVehicleSpecification.newBuilder()
                    .id(Id.create("drt_"+ i, DvrpVehicle.class))
                    .startLinkId(linkList.get(rand.nextInt(linkList.size())).getId())
                    .capacity(seats)
                    .serviceBeginTime(0)
                    .serviceEndTime(108000)
                    .build();
            vehicles.add(v);
        }

        new FleetWriter(vehicles.stream()).write("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/baseMatsimInput/fleet_drt_"+amountOfVehicles+".xml.gz");

        System.out.println("done");
    }
}
