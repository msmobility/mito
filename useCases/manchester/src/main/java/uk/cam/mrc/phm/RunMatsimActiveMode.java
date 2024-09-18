package uk.cam.mrc.phm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import routing.*;
import uk.cam.mrc.phm.util.ManchesterImplementationConfig;

import java.util.*;

public class RunMatsimActiveMode {

    private static final Logger logger = Logger.getLogger(RunMatsimActiveMode.class);
    private static final double MAX_WALKSPEED = 5.0;
    private static final double MAX_CYCLESPEED = 15.0;
    private static final String MATSIM_NETWORK = "F:\\models\\silo_manchester\\input/mito/trafficAssignment/network.xml";
    private static final String MATSIM_PLAN = "F:\\models\\silo_manchester\\scenOutput\\mito_1_0_baseStress_basePOI_fullModeset_matsim\\2021/matsimPlans_thursday.xml.gz";
    private static final List<Day> MATSIM_DAY =new ArrayList<>(Collections.singleton(Day.thursday));

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        MitoModelMCR model = MitoModelMCR.standAloneModel(args[0], ManchesterImplementationConfig.get());
        //model.run();
        final DataSet dataSet = model.getData();


        boolean runAssignment = Resources.instance.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);
        runAssignment = Boolean.TRUE;
        if (runAssignment) {

            Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
            new PopulationReader(scenario).readFile(MATSIM_PLAN);

            //Load population by mode by day
            Map<Day, Population> populationBikePedByDay = new HashMap<>();
            Map<Day, Population> populationCarByDay = new HashMap<>();

            MainModeIdentifierImpl mainModeIdentifier = new MainModeIdentifierImpl();
            for (Person person : scenario.getPopulation().getPersons().values()){
                Day day = Day.valueOf((String)person.getAttributes().getAttribute("day"));
                String mode = mainModeIdentifier.identifyMainMode(TripStructureUtils.getLegs(person.getSelectedPlan()));
                switch (mode) {
                    case "car":
                        populationCarByDay.computeIfAbsent(day, p -> PopulationUtils.createPopulation(ConfigUtils.createConfig())).addPerson(person);
                        break;
                    case "bike":
                    case "walk":
                        populationBikePedByDay.computeIfAbsent(day, p -> PopulationUtils.createPopulation(ConfigUtils.createConfig())).addPerson(person);
                        break;
                    default:
                        continue;
                }
            }


            //initial bike, ped simulation config
            Config bikePedConfig = ConfigUtils.createConfig();
            bikePedConfig.addModule(new BicycleConfigGroup());
            bikePedConfig.addModule(new WalkConfigGroup());
            fillBikePedConfig(bikePedConfig);


            //simulate bikePed by day
            for (Day day : MATSIM_DAY) {
                logger.info("Starting " + day.toString().toUpperCase() + " MATSim simulation");
                String outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
                bikePedConfig.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/" + day + "/bikePed/");
                bikePedConfig.controler().setRunId(String.valueOf(dataSet.getYear()));


                //initialize scenario
                MutableScenario matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(bikePedConfig);
                matsimScenario.setPopulation(populationBikePedByDay.get(day));
                logger.info("total population " + day + " | Bike Walk: " + populationBikePedByDay.get(day).getPersons().size());

                //set vehicle types
                VehicleType walk = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.walk, VehicleType.class));
                walk.setMaximumVelocity(MAX_WALKSPEED / 3.6);
                walk.setPcuEquivalents(0.);
                matsimScenario.getVehicles().addVehicleType(walk);

                VehicleType bicycle = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.bike, VehicleType.class));
                bicycle.setMaximumVelocity(MAX_CYCLESPEED / 3.6);
                bicycle.setPcuEquivalents(0.);
                matsimScenario.getVehicles().addVehicleType(bicycle);

                matsimScenario.getConfig().qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

                // Create active mode networks
                Network activeNetwork = extractModeSpecificNetwork(MATSIM_NETWORK,new HashSet<>(Arrays.asList(TransportMode.bike, TransportMode.walk)));

                matsimScenario.setNetwork(activeNetwork);
                NetworkUtils.writeNetwork(activeNetwork, "F:\\models\\silo_manchester\\input/mito/trafficAssignment/network_active_cleaned.xml");
                //ConfigUtils.writeMinimalConfig(matsimScenario.getConfig(),"F:\\models\\silo_manchester\\input/mito/trafficAssignment/config_min.xml");

                //set up controler
                final Controler controlerBikePed = new Controler(matsimScenario);
                controlerBikePed.addOverridingModule(new WalkModule());
                controlerBikePed.addOverridingModule(new BicycleModule());


                controlerBikePed.run();
            }
        }
    }

    private static void fillBikePedConfig(Config bikePedConfig) {
        // set input file and basic controler settings
        bikePedConfig.controler().setLastIteration(1);
        bikePedConfig.controler().setWritePlansInterval(Math.max(bikePedConfig.controler().getLastIteration(), 1));
        bikePedConfig.controler().setWriteEventsInterval(Math.max(bikePedConfig.controler().getLastIteration(), 1));
        bikePedConfig.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        // set qsim - passingQ
        bikePedConfig.qsim().setFlowCapFactor(1.);
        bikePedConfig.qsim().setStorageCapFactor(1.);
        bikePedConfig.qsim().setEndTime(24*60*60);
        bikePedConfig.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ);

        // set routing modes
        List<String> mainModeList = new ArrayList<>();
        mainModeList.add(TransportMode.bike);
        mainModeList.add(TransportMode.walk);
        bikePedConfig.qsim().setMainModes(mainModeList);
        bikePedConfig.plansCalcRoute().setNetworkModes(mainModeList);
        bikePedConfig.plansCalcRoute().removeModeRoutingParams("bike");
        bikePedConfig.plansCalcRoute().removeModeRoutingParams("walk");
        bikePedConfig.plansCalcRoute().removeModeRoutingParams("pt");

        // set walk/bike routing parameters
        BicycleConfigGroup bicycleConfigGroup = (BicycleConfigGroup) bikePedConfig.getModules().get(BicycleConfigGroup.GROUP_NAME);
        bicycleConfigGroup.getMarginalCostGradient().put("commute",66.8);
        bicycleConfigGroup.getMarginalCostVgvi().put("commute",0.);
        bicycleConfigGroup.getMarginalCostLinkStress().put("commute",6.3);
        bicycleConfigGroup.getMarginalCostJctStress().put("commute",0.);
        bicycleConfigGroup.getMarginalCostGradient().put("nonCommute",63.45);
        bicycleConfigGroup.getMarginalCostVgvi().put("nonCommute",0.);
        bicycleConfigGroup.getMarginalCostLinkStress().put("nonCommute",1.59);
        bicycleConfigGroup.getMarginalCostJctStress().put("nonCommute",0.);


        WalkConfigGroup walkConfigGroup = (WalkConfigGroup) bikePedConfig.getModules().get(WalkConfigGroup.GROUP_NAME);
        walkConfigGroup.getMarginalCostGradient().put("commute",0.);
        walkConfigGroup.getMarginalCostVgvi().put("commute",0.);
        walkConfigGroup.getMarginalCostLinkStress().put("commute",0.);
        walkConfigGroup.getMarginalCostJctStress().put("commute",4.27);
        walkConfigGroup.getMarginalCostGradient().put("nonCommute",0.);
        walkConfigGroup.getMarginalCostVgvi().put("nonCommute",0.62);
        walkConfigGroup.getMarginalCostLinkStress().put("nonCommute",0.);
        walkConfigGroup.getMarginalCostJctStress().put("nonCommute",14.34);

        // set scoring parameters
        ModeParams bicycleParams = new ModeParams(TransportMode.bike);
        bicycleParams.setConstant(0. );
        bicycleParams.setMarginalUtilityOfDistance(-0.0004 );
        bicycleParams.setMarginalUtilityOfTraveling(-6.0 );
        bicycleParams.setMonetaryDistanceRate(0. );
        bikePedConfig.planCalcScore().addModeParams(bicycleParams);

        ModeParams walkParams = new ModeParams(TransportMode.walk);
        walkParams.setConstant(0. );
        walkParams.setMarginalUtilityOfDistance(-0.0004 );
        walkParams.setMarginalUtilityOfTraveling(-6.0 );
        walkParams.setMonetaryDistanceRate(0. );
        bikePedConfig.planCalcScore().addModeParams(walkParams);

        ActivityParams homeActivity = new ActivityParams("home").setTypicalDuration(12 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(homeActivity);

        ActivityParams workActivity = new ActivityParams("work").setTypicalDuration(8 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(workActivity);

        ActivityParams educationActivity = new ActivityParams("education").setTypicalDuration(8 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(educationActivity);

        ActivityParams shoppingActivity = new ActivityParams("shopping").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(shoppingActivity);

        ActivityParams recreationActivity = new ActivityParams("recreation").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(recreationActivity);

        ActivityParams otherActivity = new ActivityParams("other").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(otherActivity);

        ActivityParams airportActivity = new ActivityParams("airport").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(airportActivity);

        //Set strategy
        bikePedConfig.strategy().setMaxAgentPlanMemorySize(5);
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.8);
            bikePedConfig.strategy().addStrategySettings(strategySettings);
        }

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.2);
            bikePedConfig.strategy().addStrategySettings(strategySettings);
        }


        bikePedConfig.transit().setUsingTransitInMobsim(false);

    }

    public static Network extractModeSpecificNetwork(String networkFile, Set<String> transportModes) {

        Network network = NetworkUtils.readNetwork(networkFile);
        Network modeSpecificNetwork = NetworkUtils.createNetwork();

        new TransportModeNetworkFilter(network).filter(modeSpecificNetwork, transportModes);
        NetworkUtils.runNetworkCleaner(modeSpecificNetwork);
        return modeSpecificNetwork;
    }
}
