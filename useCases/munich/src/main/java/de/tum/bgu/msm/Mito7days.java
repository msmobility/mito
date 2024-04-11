package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.scenarios.mito7days.MitoModel7days;
import de.tum.bgu.msm.trafficAssignment.CarSkimUpdater;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.util.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Mito7days {

    private static final Logger logger = Logger.getLogger(Mito7days.class);

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        MitoModel7days model = MitoModel7days.standAloneModel(args[0], MunichImplementationConfig.get());
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

            final EnumMap<Day, Controler> controlers = new EnumMap<>(Day.class);

            Map<Day, Population> populationByDay = new HashMap<>();

            for (Person person : dataSet.getPopulation().getPersons().values()){
                Day day = Day.valueOf((String)person.getAttributes().getAttribute("day"));
                populationByDay.getOrDefault(day, PopulationUtils.createPopulation(ConfigUtils.createConfig())).addPerson(person);
            }

            for (Day day : Day.values()) {
                logger.info("Starting " + day.toString().toUpperCase() + " MATSim simulation");
                config.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/" + day.toString());
                MutableScenario matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);
                matsimScenario.setPopulation(populationByDay.get(day));
                controlers.put(day, new Controler(matsimScenario));
                controlers.get(day).run();
            }

            //TODO: print seperate skim for each day of week?
            if (Resources.instance.getBoolean(Properties.PRINT_OUT_SKIM, false)) {
                CarSkimUpdater skimUpdater = new CarSkimUpdater(controlers.get(Day.monday), model.getData(), model.getScenarioName());
                skimUpdater.run();
            }
        }
    }
}

