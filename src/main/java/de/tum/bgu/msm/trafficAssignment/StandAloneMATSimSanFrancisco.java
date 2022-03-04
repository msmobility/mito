package de.tum.bgu.msm.trafficAssignment;

import de.tum.bgu.msm.MitoModelSanFrancisco;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.sanFrancisco.SanFranciscoImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

public class StandAloneMATSimSanFrancisco {

    private static Logger logger = Logger.getLogger(StandAloneMATSimSanFrancisco.class);

    public static void main(String[] args) {

        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");

        Resources.initializeResources(args[0]);
        logger.info("Running traffic assignment in MATsim");

        Config config;
        if (args.length > 1 && args[1] != null) {
            config = ConfigUtils.loadConfig(args[1]);
            ConfigureMatsim.setDemandSpecificConfigSettings(config);
        } else {
            logger.warn("Using a fallback config with default values as no initial config has been provided.");
            config = ConfigureMatsim.configureMatsim();
        }

        String outputSubDirectory = "scenOutput/" + Resources.instance.getScenarioName() + "/" + Resources.instance.getString(Properties.SCENARIO_YEAR);
        config.controler().setOutputDirectory("F:/trampa_sf" + "/" + outputSubDirectory + "/trafficAssignment");

        MutableScenario matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        //population is read from an external file already created
        String populationFileName = outputSubDirectory + "/matsimPlans.xml.gz";
        new PopulationReader(matsimScenario).readFile(populationFileName);

        Controler controler = new Controler(matsimScenario);
        controler.run();

    }

}
