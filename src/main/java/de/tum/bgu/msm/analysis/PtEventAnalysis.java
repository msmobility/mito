package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.pt.counts.OccupancyAnalyzer;

public class PtEventAnalysis {
    private static boolean subTrip = true;
    private static double planScale = 0.05;
    private static int lastItration = 1;
    private static double reroute = 1;
    private static boolean useSBB = true;
    private static boolean deterministic = false;
    private static int minCapacityFactor = 10;
    private static int maxCapacityFactor = 10;
    private static int maxPlan = 5;
    private static double maxSearchRadius = 1000;
    private static double betaTransfer = 1;
    private static int instance = 0;

    public static void main(String[] args) {
        Resources.initializeResources(args[0]);

        for(int factor = minCapacityFactor;factor<=maxCapacityFactor;factor++){
            String router = useSBB?"SBB":"woSBB";
            String dt = deterministic?"dt":"notDt";
            String scenario = subTrip?"subTrip_benedikt": Resources.instance.getString(Properties.SCENARIO_NAME);
            String outputSubDirectory = "scenOutput/" + scenario +
                    "_it" + lastItration + "_reroute" + reroute + "_capa" + factor + "_" + router +
                    "_" + dt + "_maxPlan" + maxPlan + "_maxRadius" + maxSearchRadius +"_betaTransfer" + betaTransfer + "_instance"+ instance+ "/" +Resources.instance.getString(Properties.SCENARIO_YEAR);

            String eventFileName = Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/output_events.xml.gz";
            EventsManager eventsManager = new EventsManagerImpl();
            OccupancyAnalyzer occupancyAnalyzer = new OccupancyAnalyzer(3600,24*3600);
            eventsManager.addHandler(occupancyAnalyzer);
            EventsUtils.readEvents(eventsManager,eventFileName);

            String output=Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/occupancyTransit.csv";
            occupancyAnalyzer.write(output);

        }
    }
}
