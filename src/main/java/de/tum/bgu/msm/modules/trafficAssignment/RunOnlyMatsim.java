package de.tum.bgu.msm.modules.trafficAssignment;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

public class RunOnlyMatsim {

    public static void main(String[] args) {

        String configFileName = args[0];
        Config config = ConfigUtils.loadConfig(configFileName);

        //modify configuration parameters
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        //load the scenario from the configuration settings
        Scenario scenario = ScenarioUtils.loadScenario(config);

        //create the controller
        Controler controler = new Controler(scenario);

        // This runs iterations:
        controler.run();


    }
}
