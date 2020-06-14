package de.tum.bgu.msm.run.scenarios.drtNoise;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.gis.ShapeFileReader;

public class RunMitoDrt {

    private static final Logger logger = Logger.getLogger(RunMitoDrt.class);

    private static final String serviceAreaShapeFile = "D:\\resultStorage\\moia-msm\\cleverShuttleOperationArea\\cleverShuttle.shp";

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");

        final Geometry geometry = (Geometry) ShapeFileReader
                .getAllFeatures(serviceAreaShapeFile)
                .iterator().next().getDefaultGeometry();

        MitoModelDrt model = MitoModelDrt.standAloneModel(args[0], MunichImplementationConfig.get(), geometry);
        model.run();
        final DataSet dataSet = model.getData();

        ServiceAreaModeChoiceResults.printServiceAreaModeChoiceResults(dataSet, geometry, Resources.instance.getString(Properties.SCENARIO_NAME));

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

            Controler controler = new Controler(matsimScenario);
            controler.run();

        }
    }
}
