package de.tum.bgu.msm.run.scenarios.drtNoise;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.MinCostFlowRebalancingParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunDrtNoise {

    private static String configPath  = "mito_assignment.output_config.xml";
    private static String demandPath;
    private static String networkPath = "croppedDenseNetwork.xml.gz";
    private static String stopPath = "stops_pt.xml";
    private static String fleetPath;
    private static String outputDir;
    private static Boolean stopbased;
    private static Boolean rejections;
    private static Integer numberOfIterations;

    public static void main(String[] args) {
        if (args.length > 0){ //ONLY FOR CMD CASES

            demandPath = args[0];
            fleetPath = args[1];
            stopbased = Boolean.parseBoolean(args[2]);
            outputDir = args[3];
            rejections = Boolean.parseBoolean(args[4]);
            numberOfIterations = Integer.parseInt(args[5]);





        } else {

            String base = "/Users/felixzwick/input/";
            configPath = base + "mito_assignment.output_config.xml";
            networkPath = base + "croppedDenseNetwork.xml.gz";
            fleetPath = base + "fleet_drt_10000.xml.gz";
            outputDir = base + "outputTest";
            demandPath = base + "croppedPopulation_drt_00001pct.xml.gz";
            stopPath = base + "stops_pt.xml";
            outputDir = "outputTest/";
            stopbased = false;

        }



        DrtConfigGroup drtCfg = new DrtConfigGroup();
        MultiModeDrtConfigGroup multiModeDrtConfigGroup = new MultiModeDrtConfigGroup();
        multiModeDrtConfigGroup.addParameterSet(drtCfg);
        final Config config = ConfigUtils.loadConfig(configPath, multiModeDrtConfigGroup,
                new DvrpConfigGroup());

        config.controler().setLastIteration(numberOfIterations); // Number of simulation iterations
        config.controler().setWriteEventsInterval(numberOfIterations); // Write Events file every x-Iterations
        config.controler().setWritePlansInterval(numberOfIterations); // Write Plan file every x-Iterations


        config.plans().setInputFile(demandPath);
        config.network().setInputFile(networkPath);
        DrtConfigGroup drt = DrtConfigGroup.getSingleModeDrtConfig(config);
        drt.setMaxTravelTimeBeta(600.0);
        drt.setMaxTravelTimeAlpha(1.5);
        drt.setMaxWaitTime(600.0);
        drt.setStopDuration(30.0);
        drt.setRejectRequestIfMaxWaitOrTravelTimeViolated(rejections);
        drt.setTransitStopFile(null); //door-to-door approach for now?
        drt.setMaxWalkDistance(1000.0);
        config.controler().setOutputDirectory(outputDir);
        drt.setVehiclesFile(fleetPath);
        drt.setIdleVehiclesReturnToDepots(false);
        drt.setPlotDetailedCustomerStats(true);

        drt.setOperationalScheme(DrtConfigGroup.OperationalScheme.door2door);


        if (stopbased) {
            drt.setOperationalScheme(DrtConfigGroup.OperationalScheme.stopbased);
        }
        drt.setTransitStopFile(stopPath);

        Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
        ScenarioUtils.loadScenario(scenario);
        Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt(config, false);
//        controler.addOverridingModule(new DrtModeAnalysisModule(drt));

        MinCostFlowRebalancingParams rebalancingParams = new MinCostFlowRebalancingParams();

        rebalancingParams.setInterval(300);
        rebalancingParams.setCellSize(1000);
        rebalancingParams.setTargetAlpha(0.3);
        rebalancingParams.setTargetBeta(0.3);
        rebalancingParams.setMaxTimeBeforeIdle(500);
        rebalancingParams.setMinServiceTime(3600);
        drt.addParameterSet(rebalancingParams);

        controler.run();
    }

}
