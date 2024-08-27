package de.tum.bgu.msm.scenarios.tengos;

import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;


public class RunMatsim {

    private static final Logger logger = Logger.getLogger(RunMatsim.class);

    public static void main(String[] args) {
        // Assume args[0] is the config file path or base directory for resources
        String configFilePath = "C:\\models\\mito7\\muc\\scenOutput\\tengos_debug_2\\2011\\trafficAssignment\\mondayConfig.xml";
        String populationFilePath = "C:\\models\\mito7\\muc\\scenOutput\\tengos_debug_2\\2011\\matsimPlans.xml.gz";

        logger.info("Loading MATSim configuration from: " + configFilePath);
        Config config = ConfigUtils.loadConfig(configFilePath);

        // Set network and plans file paths
        config.transit().setVehiclesFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt\\tengos\\vehicles_scale_0.25_10_newcapacity.xml");
        config.network().setInputFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/network_pt_road.xml.gz");
        config.transit().setTransitScheduleFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/tengos/schedule.xml");
        //config.transit().setVehiclesFile("C:\\models\\MITO/mitoMunich/input/trafficAssignment/pt/tengos/vehicles_scale_0.05_10.xml");
        //config.network().setInputFile("trafficAssignment\\pt\\network_pt_road.xml.gz");
        //config.transit().setTransitScheduleFile("trafficAssignment\\pt\\tengos\\schedule.xml");

        config.plans().setInputFile(populationFilePath);

        config.controler().setOutputDirectory("C:\\models\\mito7\\muc\\scenOutput\\tengos_debug_2\\2011\\trafficAssignment\\test");

        // Set other configurations as needed
        double scaleFactor = 1;
        config.qsim().setFlowCapFactor(scaleFactor);
        config.qsim().setStorageCapFactor(scaleFactor);
        config.controler().setLastIteration(25);

        // Load the scenario with the configuration
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);

        // Filter population by mode "pt"
        Population filteredPopulation = PopulationUtils.createPopulation(config);
        for (Person person : scenario.getPopulation().getPersons().values()) {
            // Check if the person's "day" attribute is "monday"
            String day = (String) person.getAttributes().getAttribute("day");

            if ("monday".equals(day)) {
                // Check if the person's plan contains a leg with mode "pt"
                boolean hasPtLeg = person.getSelectedPlan().getPlanElements().stream()
                        .filter(planElement -> planElement instanceof Leg)
                        .map(planElement -> (Leg) planElement)
                        .anyMatch(leg -> leg.getMode().equals("pt"));

                // If both conditions are met, add the person to the filtered population
                if (hasPtLeg) {
                    filteredPopulation.addPerson(person);
                }
            }
        }

        // Set the filtered population in the scenario
        scenario.setPopulation(filteredPopulation);

        // Initialize the controler with the filtered scenario
        Controler controler = new Controler(scenario);

        controler.addOverridingModule(new SBBTransitModule());
        controler.addOverridingModule(new SwissRailRaptorModule());

        controler.configureQSimComponents(components -> {
            new SBBTransitEngineQSimModule().configure(components);});

        // Run the simulation
        logger.info("Running MATSim simulation with the filtered population...");
        controler.run();
        logger.info("MATSim simulation completed.");
    }
}
