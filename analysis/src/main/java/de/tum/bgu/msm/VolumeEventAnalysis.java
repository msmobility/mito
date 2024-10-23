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

    private static final String MATSIM_NETWORK = "/home/qin/models/manchester/input/mito/trafficAssignment/network.xml";
    private static final String MATSIM_EVENT = "/home/qin/models/manchester/scenOutput/mito_1_0_baseStress_basePOI_fullModeset_matsim/2021/trafficAssignment/thursday/bikePed/2021.output_events.xml.gz";
    private static final String OUTPUT_PATH = "/home/qin/models/manchester/scenOutput/mito_1_0_baseStress_basePOI_fullModeset_matsim/2021/trafficAssignment/thursday/bikePed/dailyVolume_bikePed.csv";

    public static void main(String[] args) {

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(MATSIM_NETWORK);

        EventsManager eventsManager = new EventsManagerImpl();
        DailyVolumeEventHandler volumeEventHandler = new DailyVolumeEventHandler();
        eventsManager.addHandler(volumeEventHandler);
        EventsUtils.readEvents(eventsManager,MATSIM_EVENT);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(OUTPUT_PATH));
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
