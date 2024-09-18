package de.tum.bgu.msm;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VolumeEventAnalysis {


    public static void main(String[] args) {

        String networkPath = "";
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkPath);

        String eventFileName = "";
        EventsManager eventsManager = new EventsManagerImpl();
        DailyVolumeEventHandler volumeEventHandler = new DailyVolumeEventHandler();
        eventsManager.addHandler(volumeEventHandler);
        EventsUtils.readEvents(eventsManager,eventFileName);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder header = new StringBuilder();
        header.append("linkId,edgeId,osmId,bike,ped");
        pw.println(header);


        for (Link link :  network.getLinks().values()) {
            String linkId = link.getId().toString();
            int edgeId = (int) link.getAttributes().getAttribute("edgeID");
            int osmId = (int) link.getAttributes().getAttribute("osmID");
            int bikeVolumes = volumeEventHandler.getBikeVolumes().getOrDefault(link.getId(),0);
            int pedVolumes = volumeEventHandler.getPedVolumes().getOrDefault(link.getId(),0);

            pw.println(linkId + "," + edgeId + "," + osmId + "," + bikeVolumes + "," + pedVolumes);
        }

        pw.close();

    }
}
