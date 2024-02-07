package de.tum.bgu.msm.run;

import de.tum.bgu.msm.MitoModel2;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsimBikeWalk;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

public class Mito2CarBikeWalkAssignment {

    private static final Logger logger = Logger.getLogger(Mito2CarBikeWalkAssignment.class);

    /*public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        MitoModel2 model = MitoModel2.standAloneModel(args[0], MunichImplementationConfig.get());
        model.run();
        final DataSet dataSet = model.getData();

        boolean runAssignment = Resources.instance.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);

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
        }
    }*/

    private static final double MAX_WALKSPEED = 5.0;
    private static final double MAX_CYCLESPEED = 15.0;

    public static double carHHShare;
    public static double bikeHHShare;



    public static void main(String[] args) {
        int i = 0;
        for (double factor = 1; factor <= 9; ) {

            carHHShare = 0.1 * factor;
            bikeHHShare = 0.1 * factor;

            MitoModel2 model = null;
            logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
            model = MitoModel2.standAloneModel(args[i], MunichImplementationConfig.get());
            model.run();
            DataSet dataSet = model.getData();


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
            Config carConfig = ConfigureMatsim.configureMatsim();
            String outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
            carConfig.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/car");

            MutableScenario scenarioCar = (MutableScenario) ScenarioUtils.loadScenario(carConfig);
            scenarioCar.setPopulation(populationCar);
            final Controler controlerCar = new Controler(scenarioCar);
            controlerCar.run();
            logger.warn("Running MATSim transport model for car scenario finished.");

            logger.warn("Running MATSim transport model for Bike&Ped scenario.");
            Config bikePedConfig = ConfigureMatsimBikeWalk.configureMatsim();
            outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();
            bikePedConfig.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/bikePed");
            MutableScenario scenarioBikePed = (MutableScenario) ScenarioUtils.loadScenario(bikePedConfig);
            scenarioBikePed.setPopulation(populationBikePed);

            VehicleType walk = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.walk, VehicleType.class));
            walk.setMaximumVelocity(MAX_WALKSPEED / 3.6);
            scenarioBikePed.getVehicles().addVehicleType(walk);

            VehicleType bicycle = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.bike, VehicleType.class));
            bicycle.setMaximumVelocity(MAX_CYCLESPEED / 3.6);
            scenarioBikePed.getVehicles().addVehicleType(bicycle);

            scenarioBikePed.getConfig().qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

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

            factor = factor + 2;
            i++;

        }
    }
}


