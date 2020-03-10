package de.tum.bgu.msm.run.scenarios.drtNoise;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.analysis.DrtModeAnalysisModule;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.MinCostFlowRebalancingParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

public class RunDrtNoise {

    public static void main(String[] args) {
        String configPath = "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/baseMatsimInput/mito_assignment.output_config.xml";
        String demandPath = "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/plans_drt_inside_SA_2.xml";
        String networkPath = "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/baseMatsimInput/mito_assignment.output_network.xml";
        String fleetPath = "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/baseMatsimInput/fleet_drt.xml.gz";
        String outputDir = "/Volumes/GoogleDrive/Meine Ablage/PhD/MITO_DRT_Project/output";

        DrtConfigGroup drtCfg = new DrtConfigGroup();
        MultiModeDrtConfigGroup multiModeDrtConfigGroup = new MultiModeDrtConfigGroup();
        multiModeDrtConfigGroup.addParameterSet(drtCfg);
        final Config config = ConfigUtils.loadConfig(configPath, multiModeDrtConfigGroup,
                new DvrpConfigGroup());

        config.controler().setLastIteration(2); // Number of simulation iterations
        config.controler().setWriteEventsInterval(2); // Write Events file every x-Iterations
        config.controler().setWritePlansInterval(2); // Write Plan file every x-Iterations


        config.plans().setInputFile(demandPath);
        config.network().setInputFile(networkPath);
        DrtConfigGroup drt = DrtConfigGroup.getSingleModeDrtConfig(config);
        drt.setMaxTravelTimeBeta(600.0);
        drt.setMaxTravelTimeAlpha(1.5);
        drt.setMaxWaitTime(600.0);
        drt.setStopDuration(30.0);
        drt.setRejectRequestIfMaxWaitOrTravelTimeViolated(true);
        drt.setTransitStopFile(null); //door-to-door approach for now?
        drt.setMaxWalkDistance(300.0);
        config.controler().setOutputDirectory(outputDir);
        drt.setVehiclesFile(fleetPath);
        drt.setIdleVehiclesReturnToDepots(false);
        drt.setOperationalScheme(DrtConfigGroup.OperationalScheme.door2door);
        drt.setPlotDetailedCustomerStats(false);

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
