package de.tum.bgu.msm.run.scenarios.drtNoise;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.gis.ShapeFileReader;

public class RunMitoDrt {

    private static final Logger logger = Logger.getLogger(RunMitoDrt.class);

    private static final String serviceAreaShapeFile = "D:\\resultStorage\\moia-msm\\abmtrans\\shapesServiceAreas\\HolzkirchenServiceArea.shp";

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        final Geometry geometry = (Geometry) ShapeFileReader
                .getAllFeatures(serviceAreaShapeFile)
                .iterator().next().getDefaultGeometry();

        MitoModelDrt model = MitoModelDrt.standAloneModel(args[0], MunichImplementationConfig.get(), geometry);
        model.run();
        final DataSet dataSet = model.getData();

        ServiceAreaModeChoiceResults.printServiceAreaModeChoiceResults(dataSet, geometry, Resources.instance.getScenarioName());

//        boolean runAssignment = Resources.instance.getBoolean(Properties.RUN_TRAFFIC_ASSIGNMENT, false);
        boolean runAssignment = false;

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

            ConfigureMatsim.setDemandSpecificConfigSettings(config);
            config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);


            config.qsim().setNumberOfThreads(16);
            config.global().setNumberOfThreads(16);
            config.parallelEventHandling().setNumberOfThreads(16);
//            config.qsim().setUsingThreadpool(true);

            config.controler().setFirstIteration(0);
            config.controler().setLastIteration(150);
            config.controler().setMobsim("qsim");
            config.controler().setWritePlansInterval(25);
            config.controler().setWriteEventsInterval(25);
            config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

            config.qsim().setEndTime(28 * 3600);
            //config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
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
                strategySettings.setWeight(0.15);
                config.strategy().addStrategySettings(strategySettings);
            }

            {
                StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
                strategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator_ReRoute);
                strategySettings.setWeight(0.05);
                config.strategy().addStrategySettings(strategySettings);
            }

            config.timeAllocationMutator().setMutationRange(1200);


            config.strategy().setFractionOfIterationsToDisableInnovation(0.85);
            config.strategy().setMaxAgentPlanMemorySize(5);

            Network network = NetworkUtils.createNetwork();

            new MatsimNetworkReader(network).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\studyNetworkDense.xml");
            matsimScenario.setNetwork(network);

            Controler controler = new Controler(matsimScenario);
            controler.run();

        }
    }
}
