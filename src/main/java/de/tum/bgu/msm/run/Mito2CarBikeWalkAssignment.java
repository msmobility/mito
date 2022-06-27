package de.tum.bgu.msm.run;

import de.tum.bgu.msm.MitoModel2;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.ArrayList;
import java.util.List;

public class Mito2CarBikeWalkAssignment {

    private static final Logger logger = Logger.getLogger(Mito2CarBikeWalkAssignment.class);
    private static final double MAX_WALKSPEED = 5.0;
    private static final double MAX_CYCLESPEED = 15.0;
    private static final double SILO_SAMPLING_RATE = 1.;

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        MitoModel2 model = MitoModel2.standAloneModel(args[0], MunichImplementationConfig.get());
        model.run();
        final DataSet dataSet = model.getData();

        //System.exit(1);
        Config config;
        if (args.length > 1 && args[1] != null) {
            config = ConfigUtils.loadConfig(args[1]);
            ConfigureMatsim.setDemandSpecificConfigSettings(config);
        } else {
            logger.warn("Using a fallback config with default values as no initial config has been provided.");
            config = ConfigureMatsim.configureMatsim();
        }

        boolean runAssignment = Resources.instance.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);
        boolean runBikePedAssignment = true;

        MainModeIdentifierImpl mainModeIdentifier = new MainModeIdentifierImpl();

        Population populationCar = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        Population populationBikePed = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        for (Person pp : dataSet.getPopulation().getPersons().values()) {
            String mode = mainModeIdentifier.identifyMainMode(TripStructureUtils.getLegs(pp.getSelectedPlan()));
            switch (mode) {
                case "car":
                    populationCar.addPerson(pp);
                    break;
                case "bike":
                case "walk":
                    populationBikePed.addPerson(pp);
                    break;
                default:
                    continue;
            }
        }

        logger.warn("size of car pop:" + populationCar.getPersons().size());
        logger.warn("size of bike ped pop:" + populationBikePed.getPersons().size());

        logger.warn("Running MATSim transport model for car scenario ");
        Config carConfig = config;
        String outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
        carConfig.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/Car");

        MutableScenario scenarioCar = (MutableScenario) ScenarioUtils.loadScenario(carConfig);
        scenarioCar.setPopulation(populationCar);
        finalizeCarConfig(scenarioCar.getConfig());
        final Controler controlerCar = new Controler(scenarioCar);
        controlerCar.run();
        logger.warn("Running MATSim transport model for car scenario finished.");

        logger.warn("Running MATSim transport model for Bike&Ped scenario.");
        Config bikePedConfig = config;
        outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
        bikePedConfig.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/BikePed");
        bikePedConfig.network().setInputFile("input/trafficAssignment/studyNetworkDenseBikeWalkHealth.xml.gz");
        MutableScenario scenarioBikePed = (MutableScenario) ScenarioUtils.loadScenario(bikePedConfig);
        scenarioBikePed.setPopulation(populationBikePed);

        VehicleType walk = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.walk, VehicleType.class));
        walk.setMaximumVelocity(MAX_WALKSPEED / 3.6);
        scenarioBikePed.getVehicles().addVehicleType(walk);

        VehicleType bicycle = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.bike, VehicleType.class));
        bicycle.setMaximumVelocity(MAX_CYCLESPEED / 3.6);
        scenarioBikePed.getVehicles().addVehicleType(bicycle);

        scenarioBikePed.getConfig().qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

        finalizeConfigForBikePedScenario(scenarioBikePed.getConfig());
        final Controler controlerBikePed = new Controler(scenarioBikePed);

        controlerBikePed.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                this.addTravelTimeBinding(TransportMode.bike).toInstance((link, time, person, vehicle) -> link.getLength() / MAX_CYCLESPEED);
                this.addTravelTimeBinding(TransportMode.walk).toInstance((link, time, person, vehicle) -> link.getLength() / MAX_WALKSPEED);
            }
        });

        controlerBikePed.run();
        logger.warn("Running MATSim transport model for Bike&Ped scenario finished.");


        /*boolean runAssignment = Resources.instance.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);

        if (runAssignment) {
            logger.info("Running traffic assignment in MATsim");

            Config config;
            if (args.length > 1 && args[1] != null) {
                config = ConfigUtils.loadConfig(args[1]);
                ConfigureMatsim.setDemandSpecificConfigSettings(config);
            } else {
                logger.warn("Using a fallback config with default values as no initial config has been provided.");
                config = ConfigureMatsim.configureMatsim();
            }

            String outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
            config.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment");

            MutableScenario matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);
            matsimScenario.setPopulation(dataSet.getPopulation());

            Controler controler = new Controler(matsimScenario);
            controler.run();

            if (Resources.instance.getBoolean(Properties.PRINT_OUT_SKIM, false)) {
                CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, model.getData(), model.getScenarioName());
                skimUpdater.run();
            }
        }*/
    }

    private static void finalizeConfigForBikePedScenario(Config bikePedConfig) {

        bikePedConfig.controler().setLastIteration(1);
        bikePedConfig.qsim().setFlowCapFactor(1.);
        bikePedConfig.qsim().setStorageCapFactor(1.);
        bikePedConfig.qsim().setEndTime(24*60*60);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home").setTypicalDuration(12 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work").setTypicalDuration(8 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams educationActivity = new PlanCalcScoreConfigGroup.ActivityParams("education").setTypicalDuration(8 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(educationActivity);

        PlanCalcScoreConfigGroup.ActivityParams shoppingActivity = new PlanCalcScoreConfigGroup.ActivityParams("shopping").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(shoppingActivity);

        PlanCalcScoreConfigGroup.ActivityParams recreationActivity = new PlanCalcScoreConfigGroup.ActivityParams("recreation").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(recreationActivity);

        PlanCalcScoreConfigGroup.ActivityParams otherActivity = new PlanCalcScoreConfigGroup.ActivityParams("other").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(otherActivity);

        PlanCalcScoreConfigGroup.ActivityParams airportActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport").setTypicalDuration(1 * 60 * 60);
        bikePedConfig.planCalcScore().addActivityParams(airportActivity);

        bikePedConfig.controler().setRunId(Properties.SCENARIO_YEAR);
        bikePedConfig.controler().setWritePlansInterval(Math.max(bikePedConfig.controler().getLastIteration(), 1));
        bikePedConfig.controler().setWriteEventsInterval(Math.max(bikePedConfig.controler().getLastIteration(), 1));
        bikePedConfig.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        bikePedConfig.transit().setUsingTransitInMobsim(false);

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

        List<String> mainModeList = new ArrayList<>();
        mainModeList.add("bike");
        mainModeList.add("walk");
        bikePedConfig.qsim().setMainModes(mainModeList);
        bikePedConfig.plansCalcRoute().setNetworkModes(mainModeList);

        bikePedConfig.plansCalcRoute().removeModeRoutingParams("bike");
        bikePedConfig.plansCalcRoute().removeModeRoutingParams("walk");

        PlanCalcScoreConfigGroup.ModeParams bicycleParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.bike);
        bicycleParams.setConstant(0. );
        bicycleParams.setMarginalUtilityOfDistance(-0.0004 );
        bicycleParams.setMarginalUtilityOfTraveling(-6.0 );
        bicycleParams.setMonetaryDistanceRate(0. );
        bikePedConfig.planCalcScore().addModeParams(bicycleParams);

        PlanCalcScoreConfigGroup.ModeParams walkParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.walk);
        walkParams.setConstant(0. );
        walkParams.setMarginalUtilityOfDistance(-0.0004 );
        walkParams.setMarginalUtilityOfTraveling(-6.0 );
        walkParams.setMonetaryDistanceRate(0. );
        bikePedConfig.planCalcScore().addModeParams(walkParams);
    }

    private static void finalizeCarConfig(Config config) {
        config.qsim().setFlowCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));
        config.qsim().setStorageCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.instance.getString(Properties.TRIP_SCALING_FACTOR)));

        logger.info("Flow Cap Factor: " + config.qsim().getFlowCapFactor());
        logger.info("Storage Cap Factor: " + config.qsim().getStorageCapFactor());

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home").setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work").setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams educationActivity = new PlanCalcScoreConfigGroup.ActivityParams("education").setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(educationActivity);

        PlanCalcScoreConfigGroup.ActivityParams shoppingActivity = new PlanCalcScoreConfigGroup.ActivityParams("shopping").setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(shoppingActivity);

        PlanCalcScoreConfigGroup.ActivityParams recreationActivity = new PlanCalcScoreConfigGroup.ActivityParams("recreation").setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(recreationActivity);

        PlanCalcScoreConfigGroup.ActivityParams otherActivity = new PlanCalcScoreConfigGroup.ActivityParams("other").setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(otherActivity);

        PlanCalcScoreConfigGroup.ActivityParams airportActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport").setTypicalDuration(1 * 60 * 60);
        config.planCalcScore().addActivityParams(airportActivity);


        config.controler().setRunId(Properties.SCENARIO_YEAR);
        config.controler().setWritePlansInterval(Math.max(config.controler().getLastIteration(), 1));
        config.controler().setWriteEventsInterval(Math.max(config.controler().getLastIteration(), 1));
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.transit().setUsingTransitInMobsim(false);
        config.qsim().setEndTime(24*60*60);
    }
}

