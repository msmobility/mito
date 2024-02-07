package de.tum.bgu.msm.trafficAssignment;

import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigureMatsimBikeWalk {

    public static Config configureMatsim() {

        Config config = ConfigUtils.createConfig();
        config.controler().setFirstIteration(0);
        config.controler().setLastIteration(1);
        config.qsim().setFlowCapFactor(1.);
        config.qsim().setStorageCapFactor(1.);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        String runId = "mito_assignment";
        config.controler().setRunId(runId);
        config.network().setInputFile(Resources.instance.getBaseDirectory().toString() + "/input/trafficAssignment/studyNetworkDenseBikeWalkHealth.xml.gz");

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        //config.qsim().setUsingThreadpool(false); removed for compatibility with 14.0

        config.qsim().setStuckTime(10);

        config.qsim().setEndTime(26 * 3600);
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.8);
            config.strategy().addStrategySettings(strategySettings);
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.2);
            config.strategy().addStrategySettings(strategySettings);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams educationActivity = new PlanCalcScoreConfigGroup.ActivityParams("education");
        educationActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(educationActivity);

        PlanCalcScoreConfigGroup.ActivityParams shoppingActivity = new PlanCalcScoreConfigGroup.ActivityParams("shopping");
        shoppingActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(shoppingActivity);

        PlanCalcScoreConfigGroup.ActivityParams recreationActivity = new PlanCalcScoreConfigGroup.ActivityParams("recreation");
        recreationActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(recreationActivity);

        PlanCalcScoreConfigGroup.ActivityParams otherActivity = new PlanCalcScoreConfigGroup.ActivityParams("other");
        otherActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(otherActivity);

        PlanCalcScoreConfigGroup.ActivityParams airportActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport");
        airportActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(airportActivity);

        PlansCalcRouteConfigGroup.ModeRoutingParams carPassengerParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("car_passenger");
        carPassengerParams.setTeleportedModeFreespeedFactor(1.0);
        config.plansCalcRoute().addModeRoutingParams(carPassengerParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams ptParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("pt");
        ptParams.setBeelineDistanceFactor(1.5);
        ptParams.setTeleportedModeSpeed(50 / 3.6);
        config.plansCalcRoute().addModeRoutingParams(ptParams);

        List<String> mainModeList = new ArrayList<>();
        mainModeList.add("bike");
        mainModeList.add("walk");
        config.qsim().setMainModes(mainModeList);
        config.plansCalcRoute().setNetworkModes(mainModeList);

        config.plansCalcRoute().removeModeRoutingParams("bike");
        config.plansCalcRoute().removeModeRoutingParams("walk");

        PlanCalcScoreConfigGroup.ModeParams bicycleParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.bike);
        bicycleParams.setConstant(0. );
        bicycleParams.setMarginalUtilityOfDistance(-0.0004 );
        bicycleParams.setMarginalUtilityOfTraveling(-6.0 );
        bicycleParams.setMonetaryDistanceRate(0. );
        config.planCalcScore().addModeParams(bicycleParams);

        PlanCalcScoreConfigGroup.ModeParams walkParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.walk);
        walkParams.setConstant(0. );
        walkParams.setMarginalUtilityOfDistance(-0.0004 );
        walkParams.setMarginalUtilityOfTraveling(-6.0 );
        walkParams.setMonetaryDistanceRate(0. );
        config.planCalcScore().addModeParams(walkParams);

        return config;
    }
}
