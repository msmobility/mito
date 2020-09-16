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

        Integer amountOfVehicles = 20000;
        Integer seats = 6;

        List<Link> linkList = new ArrayList<>();

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile("/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/moia-msm/cleverShuttleOperationArea/croppedDenseNetwork.xml.gz");
        Link hubLink1 = scenario.getNetwork().getLinks().get(Id.createLinkId("42251"));
        Link hubLink2 = scenario.getNetwork().getLinks().get(Id.createLinkId("110407"));
        Link hubLink3 = scenario.getNetwork().getLinks().get(Id.createLinkId("123725"));
        Link hubLink4 = scenario.getNetwork().getLinks().get(Id.createLinkId("306831"));
        Link hubLink5 = scenario.getNetwork().getLinks().get(Id.createLinkId("400634"));
        Link hubLink6 = scenario.getNetwork().getLinks().get(Id.createLinkId("182186"));
        Link hubLink7 = scenario.getNetwork().getLinks().get(Id.createLinkId("25380"));
        Link hubLink8 = scenario.getNetwork().getLinks().get(Id.createLinkId("66570"));
        Link hubLink9 = scenario.getNetwork().getLinks().get(Id.createLinkId("69757"));
        Link hubLink10 = scenario.getNetwork().getLinks().get(Id.createLinkId("396778"));

        Link hubLink11 = scenario.getNetwork().getLinks().get(Id.createLinkId("374058"));
        Link hubLink12 = scenario.getNetwork().getLinks().get(Id.createLinkId("378182"));
        Link hubLink13 = scenario.getNetwork().getLinks().get(Id.createLinkId("428232"));
        Link hubLink14 = scenario.getNetwork().getLinks().get(Id.createLinkId("331305"));
        Link hubLink15 = scenario.getNetwork().getLinks().get(Id.createLinkId("289752"));
        Link hubLink16 = scenario.getNetwork().getLinks().get(Id.createLinkId("412391"));
        Link hubLink17 = scenario.getNetwork().getLinks().get(Id.createLinkId("251698"));
        Link hubLink18 = scenario.getNetwork().getLinks().get(Id.createLinkId("276836-481277"));
        Link hubLink19 = scenario.getNetwork().getLinks().get(Id.createLinkId("329269"));
        Link hubLink20 = scenario.getNetwork().getLinks().get(Id.createLinkId("21838-317911-307952"));

        Link hubLink21 = scenario.getNetwork().getLinks().get(Id.createLinkId("83803"));
        Link hubLink22 = scenario.getNetwork().getLinks().get(Id.createLinkId("386314-284556"));
        Link hubLink23 = scenario.getNetwork().getLinks().get(Id.createLinkId("199394"));
        Link hubLink24 = scenario.getNetwork().getLinks().get(Id.createLinkId("412675-412683"));
        Link hubLink25 = scenario.getNetwork().getLinks().get(Id.createLinkId("145925"));
        Link hubLink26 = scenario.getNetwork().getLinks().get(Id.createLinkId("159057"));
        Link hubLink27 = scenario.getNetwork().getLinks().get(Id.createLinkId("27874"));
        Link hubLink28 = scenario.getNetwork().getLinks().get(Id.createLinkId("277056"));
        Link hubLink29 = scenario.getNetwork().getLinks().get(Id.createLinkId("206669"));
        Link hubLink30 = scenario.getNetwork().getLinks().get(Id.createLinkId("184556"));

        Link hubLink31 = scenario.getNetwork().getLinks().get(Id.createLinkId("108701"));
        Link hubLink32 = scenario.getNetwork().getLinks().get(Id.createLinkId("112834-58152-482595-482611-58153"));
        Link hubLink33 = scenario.getNetwork().getLinks().get(Id.createLinkId("62586"));
        Link hubLink34 = scenario.getNetwork().getLinks().get(Id.createLinkId("320461"));
        Link hubLink35 = scenario.getNetwork().getLinks().get(Id.createLinkId("404368"));

        Link hubLink36 = scenario.getNetwork().getLinks().get(Id.createLinkId("79678"));
        Link hubLink37 = scenario.getNetwork().getLinks().get(Id.createLinkId("319349"));
        Link hubLink38 = scenario.getNetwork().getLinks().get(Id.createLinkId("193560"));
        Link hubLink39 = scenario.getNetwork().getLinks().get(Id.createLinkId("421776"));
        Link hubLink40 = scenario.getNetwork().getLinks().get(Id.createLinkId("66468"));

        Link hubLink41 = scenario.getNetwork().getLinks().get(Id.createLinkId("374254"));
        Link hubLink42 = scenario.getNetwork().getLinks().get(Id.createLinkId("86055"));
        Link hubLink43 = scenario.getNetwork().getLinks().get(Id.createLinkId("435295"));
        Link hubLink44 = scenario.getNetwork().getLinks().get(Id.createLinkId("333457"));
        Link hubLink45 = scenario.getNetwork().getLinks().get(Id.createLinkId("63836"));




        linkList.add(hubLink1);
        linkList.add(hubLink2);
        linkList.add(hubLink3);
        linkList.add(hubLink4);
        linkList.add(hubLink5);
        linkList.add(hubLink6);
        linkList.add(hubLink7);
        linkList.add(hubLink8);
        linkList.add(hubLink9);
        linkList.add(hubLink10);
        linkList.add(hubLink11);
        linkList.add(hubLink12);
        linkList.add(hubLink13);
        linkList.add(hubLink14);
        linkList.add(hubLink15);
        linkList.add(hubLink16);
        linkList.add(hubLink17);
        linkList.add(hubLink18);
        linkList.add(hubLink19);
        linkList.add(hubLink20);
        linkList.add(hubLink21);
        linkList.add(hubLink22);
        linkList.add(hubLink23);
        linkList.add(hubLink24);
        linkList.add(hubLink25);
        linkList.add(hubLink26);
        linkList.add(hubLink27);
        linkList.add(hubLink28);
        linkList.add(hubLink29);
        linkList.add(hubLink30);
        linkList.add(hubLink31);
        linkList.add(hubLink32);
        linkList.add(hubLink33);
        linkList.add(hubLink34);
        linkList.add(hubLink35);
        linkList.add(hubLink36);
        linkList.add(hubLink37);
        linkList.add(hubLink38);
        linkList.add(hubLink39);
        linkList.add(hubLink40);
        linkList.add(hubLink41);
        linkList.add(hubLink42);
        linkList.add(hubLink43);
        linkList.add(hubLink44);
        linkList.add(hubLink45);



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
