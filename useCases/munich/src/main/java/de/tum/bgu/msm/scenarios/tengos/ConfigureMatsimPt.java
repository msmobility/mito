package de.tum.bgu.msm.scenarios.tengos;

import ch.sbb.matsim.config.SBBTransitConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.RaptorParameters;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.*;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;

import java.util.HashSet;
import java.util.Set;

public class ConfigureMatsimPt {

    private final static double SILO_SAMPLING_RATE = 0.05;

    public static Config configureMatsim(int lastItration, double reroute,  double planScale,int ptCapactityFactor, int maxPlan) {
        Config config = ConfigUtils.createConfig();

        //general config
        config.controler().setLastIteration(lastItration);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setWriteTripsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );

        config.qsim().setEndTime(26*3600);
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        config.qsim().setFlowCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
        config.qsim().setStorageCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
        config.qsim().setStuckTime(10);
        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);

        //transit config
        config.transit().setUseTransit(true);
        config.network().setInputFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/network_pt_road.xml.gz");
        config.transit().setTransitScheduleFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/tengos/schedule.xml");
        config.transit().setVehiclesFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/tengos/vehicles_scale_0.05_10.xml");
        //config.transit().setVehiclesFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/tengos/vehicles_scale_" +
        //planScale + "_" + ptCapactityFactor + ".xml");
        config.transitRouter().setDirectWalkFactor(2);

        Set<String> transitModes = new HashSet<>();
        transitModes.add(TransportMode.pt);
        config.transit().setTransitModes(transitModes);

        //activity config
        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams nursingHomeActivity = new PlanCalcScoreConfigGroup.ActivityParams("nursing_home");
        nursingHomeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(nursingHomeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams nursingWorkActivity = new PlanCalcScoreConfigGroup.ActivityParams("nursing_work");
        nursingWorkActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(nursingWorkActivity);

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

        PlanCalcScoreConfigGroup.ModeParams carPassenger = new PlanCalcScoreConfigGroup.ModeParams("car_passenger");
        config.planCalcScore().addModeParams(carPassenger);


        //Route config
        PlansCalcRouteConfigGroup.ModeRoutingParams carPassengerParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("car_passenger");
        carPassengerParams.setTeleportedModeFreespeedFactor(1.0);
        config.plansCalcRoute().addModeRoutingParams(carPassengerParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams bike = new PlansCalcRouteConfigGroup.ModeRoutingParams("bike");
        bike.setBeelineDistanceFactor(2.0);
        bike.setTeleportedModeSpeed(12 / 3.6);
        config.plansCalcRoute().addModeRoutingParams(bike);

        PlansCalcRouteConfigGroup.ModeRoutingParams walk = new PlansCalcRouteConfigGroup.ModeRoutingParams("walk");
        walk.setBeelineDistanceFactor(2.0);
        walk.setTeleportedModeSpeed(4 / 3.6);
        config.plansCalcRoute().addModeRoutingParams(walk);


        //strategy config
        {
            StrategyConfigGroup.StrategySettings strat = new StrategyConfigGroup.StrategySettings();
            strat.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
            strat.setWeight(reroute);
            config.strategy().addStrategySettings(strat);
        }


        {
            StrategyConfigGroup.StrategySettings strat = new StrategyConfigGroup.StrategySettings();
            strat.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
            strat.setWeight(1-reroute);
            config.strategy().addStrategySettings(strat);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(maxPlan);

        String[] networkModes = Resources.instance.getArray(Properties.MATSIM_NETWORK_MODES, new String[]{"autoDriver"});
        Set<String> networkModesSet = new HashSet<>();

        for (String mode : networkModes) {
            String matsimMode = Mode.getMatsimMode(Mode.valueOf(mode));
            if (!networkModesSet.contains(matsimMode)) {
                networkModesSet.add(matsimMode);
            }
        }

        config.plansCalcRoute().setNetworkModes(networkModesSet);

       /* </module>

		<module name="transitRouter" >

		<!-- additional time the router allocates when a line switch happens, Can be interpreted as a 'savity' time that agents need to savely transfer from one line to another -->
		<param name="additionalTransferTime" value="0.0" />

		<!-- step size to increase searchRadius if no stops are found -->
		<param name="extensionRadius" value="1000.0" />

		<!-- maximum beeline distance between stops that agents could transfer to by walking -->
		<param name="maxBeelineWalkConnectionDistance" value="1500.0" />

		<!-- the radius in which stop locations are searched, given a start or target coordinate -->
		<param name="searchRadius" value="10000.0" />
	</module>

        double	additionalTransferTime
        The minimum time needed for a transfer is calculated based on the distance and the beeline walk speed between two stop facilities.
        double	beelineWalkConnectionDistance
        The distance in meters that agents can walk to get from one stop to another stop of a nearby transit line.
        double	extensionRadius
        If no stop facility is found around start or end coordinate (see searchRadius), the nearest stop location is searched for and the distance from start/end coordinate to this location is extended by the given amount.
                If only one stop facility is found within searchRadius, the radius is also extended in the hope to find more stop facilities (e.g.
        double	searchRadius
        The distance in meters in which stop facilities should be searched for around the start and end coordinate.
*/

        return config;

    }

    public static void setSBBConfig(Config config, boolean deterministic, double maxSearchRadius, double betaTransfer){
        SBBTransitConfigGroup sbbTransitConfigGroup = new SBBTransitConfigGroup();


        if (deterministic) {
            Set<String> deterministicMode = new HashSet<>();
            deterministicMode.add("subway");
            deterministicMode.add("rail");
            sbbTransitConfigGroup.setDeterministicServiceModes(deterministicMode);
            sbbTransitConfigGroup.setCreateLinkEventsInterval(config.controler().getLastIteration());
        }

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = new SwissRailRaptorConfigGroup();

        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(true);
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet walkAccessEgress =  new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        walkAccessEgress.setMode(TransportMode.walk);
        walkAccessEgress.setInitialSearchRadius(maxSearchRadius);
        walkAccessEgress.setMaxRadius(maxSearchRadius);
        walkAccessEgress.setSearchExtensionRadius(100);
        swissRailRaptorConfigGroup.addIntermodalAccessEgress(walkAccessEgress);

        swissRailRaptorConfigGroup.setUseRangeQuery(true);
        SwissRailRaptorConfigGroup.RangeQuerySettingsParameterSet rangeQuerySettings = new SwissRailRaptorConfigGroup.RangeQuerySettingsParameterSet();
        rangeQuerySettings.setMaxEarlierDeparture(600);
        rangeQuerySettings.setMaxLaterDeparture(900);
        swissRailRaptorConfigGroup.addRangeQuerySettings(rangeQuerySettings);
        //swissRailRaptorConfigGroup.setTransferPenaltyCostPerTravelTimeHour(betaTransfer/3600.);
        //swissRailRaptorConfigGroup.setTransferPenaltyBaseCost(betaTransfer/3600.);

        SwissRailRaptorConfigGroup.RouteSelectorParameterSet routeSelector = new SwissRailRaptorConfigGroup.RouteSelectorParameterSet();
        routeSelector.setBetaTravelTime(1);
        routeSelector.setBetaDepartureTime(1);
        routeSelector.setBetaTransfers(betaTransfer);
        swissRailRaptorConfigGroup.addRouteSelector(routeSelector);

        config.addModule(sbbTransitConfigGroup);
        config.addModule(swissRailRaptorConfigGroup);
    }

}