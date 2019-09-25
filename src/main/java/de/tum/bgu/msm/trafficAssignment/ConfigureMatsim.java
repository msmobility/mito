package de.tum.bgu.msm.trafficAssignment;

import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.util.HashSet;
import java.util.Set;

public class ConfigureMatsim {

    private final static double SILO_SAMPLING_RATE = 1.;

    public static Config configureMatsim() {

        //String outputDirectory = outputDirectoryRoot + "/" + runId + "/";
        //matsimConfig.controler().setRunId(runId);
        //matsimConfig.controler().setOutputDirectory(outputDirectory);
        Config config = ConfigUtils.createConfig();
        config.controler().setFirstIteration(0);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.qsim().setEndTime(24 * 3600);
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.1);
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

        PlansCalcRouteConfigGroup.ModeRoutingParams bicycleParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("bike");
        bicycleParams.setBeelineDistanceFactor(1.3);
        bicycleParams.setTeleportedModeSpeed(15 / 3.6);
        config.plansCalcRoute().addModeRoutingParams(bicycleParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams walkParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("walk");
        walkParams.setBeelineDistanceFactor(1.3);
        walkParams.setTeleportedModeSpeed(5 / 3.6);
        config.plansCalcRoute().addModeRoutingParams(walkParams);

        String runId = "mito_assignment";
        config.controler().setRunId(runId);
        config.network().setInputFile(Resources.instance.getBaseDirectory().toString() + "/" + Resources.instance.getString(Properties.MATSIM_NETWORK_FILE));

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        config.controler().setLastIteration(Resources.instance.getInt(Properties.MATSIM_ITERATIONS));
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());

        config.qsim().setStuckTime(10);
        config.qsim().setFlowCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
        config.qsim().setStorageCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));


        String[] networkModes = Resources.instance.getArray(Properties.MATSIM_NETWORK_MODES, new String[]{"autoDriver"});
        Set<String> networkModesSet = new HashSet<>();

        for (String mode : networkModes) {
            String matsimMode = Mode.getMatsimMode(Mode.valueOf(mode));
            if (!networkModesSet.contains(matsimMode)) {
                networkModesSet.add(matsimMode);
            }
        }

        config.plansCalcRoute().setNetworkModes(networkModesSet);

        return config;
    }

    public static void setDemandSpecificConfigSettings(Config config) {
        config.qsim().setFlowCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
        config.qsim().setStorageCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));

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

        PlanCalcScoreConfigGroup.ActivityParams otherActivity = new PlanCalcScoreConfigGroup.ActivityParams("other");
        otherActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(otherActivity);

        PlanCalcScoreConfigGroup.ActivityParams airportActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport");
        airportActivity.setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(airportActivity);
    }
}
