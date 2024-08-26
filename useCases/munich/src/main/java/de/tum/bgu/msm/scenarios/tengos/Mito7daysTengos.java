package de.tum.bgu.msm.scenarios.tengos;

import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.CarSkimUpdater;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
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

public class Mito7daysTengos {

    private static final Logger logger = Logger.getLogger(Mito7daysTengos.class);

    private static boolean runPtAssignment = false;

    private static double planScale = 0.25;
    private static int lastIteration = 25;
    private static double reroute = 0.2;
    private static boolean useSBB = true;
    private static boolean deterministic = false;
    private static int maxPlan = 5;
    private static double maxSearchRadius = 1000;
    private static double betaTransfer = 300;

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        MitoModel7daysTengos model = MitoModel7daysTengos.standAloneModel(args[0], MunichImplementationConfigTengos.get());
        model.run();
        final DataSet dataSet = model.getData();

        if (runPtAssignment) {
            logger.info("Running traffic assignment in MATsim for car and PT");

            Config config = ConfigureMatsimPt.configureMatsim(lastIteration, reroute, planScale, 10, maxPlan);
            if(useSBB) {
                ConfigureMatsimPt.setSBBConfig(config, deterministic, maxSearchRadius, betaTransfer);
            }
            String outputSubDirectory = "scenOutput/" + model.getScenarioName() + "/" + dataSet.getYear();

            // Handle day-specific simulation
            final EnumMap<Day, Controler> controlers = new EnumMap<>(Day.class);
            Map<Day, Population> populationByDay = new HashMap<>();

            for (Person person : dataSet.getPopulation().getPersons().values()){
                Day day = Day.valueOf((String)person.getAttributes().getAttribute("day"));
                populationByDay.computeIfAbsent(day, k -> PopulationUtils.createPopulation(ConfigUtils.createConfig())).addPerson(person);
            }

           /* for (Person person : dataSet.getPopulation().getPersons().values()) {
                Day day = Day.valueOf((String) person.getAttributes().getAttribute("day"));

                // Check if the person's plan contains a leg with mode "pt"
                boolean hasPtLeg = person.getSelectedPlan().getPlanElements().stream()
                        .filter(planElement -> planElement instanceof Leg)
                        .map(planElement -> (Leg) planElement)
                        .anyMatch(leg -> leg.getMode().equals("pt"));

                // If the plan contains a "pt" leg, add the person to the corresponding day population
                if (hasPtLeg) {
                    populationByDay.computeIfAbsent(day, k -> PopulationUtils.createPopulation(ConfigUtils.createConfig())).addPerson(person);
                }
            }*/

            for (Day day : Day.values()) {
                logger.info("Starting " + day.toString().toUpperCase() + " MATSim simulation");

                config.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/" + day.toString());
                MutableScenario matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);
                matsimScenario.setPopulation(populationByDay.get(day));


                for (Person person : populationByDay.get(day).getPersons().values()) {
                    Plan plan = person.getSelectedPlan();
                    if (plan.getPlanElements().size() < 3) {
                        logger.error("Person " + person.getId() + " has an invalid plan with only " + plan.getPlanElements().size() + " elements.");
                    }
                }

                controlers.put(day, new Controler(matsimScenario));
                if(useSBB){

                    // To use the deterministic pt simulation (Part 1 of 2):
                    controlers.get(day).addOverridingModule(new SBBTransitModule());

                    // To use the fast pt router (Part 1 of 1)
                    controlers.get(day).addOverridingModule(new SwissRailRaptorModule());

                    // To use the deterministic pt simulation (Part 2 of 2):
                    controlers.get(day).configureQSimComponents(components -> {
                        new SBBTransitEngineQSimModule().configure(components);

                        // if you have other extensions that provide QSim components, call their configure-method here
                    });

                }
                ConfigUtils.writeConfig(config,Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment/" + day.toString() + "Config.xml");
                long start = System.currentTimeMillis();
                controlers.get(day).run();
                long runTime = System.currentTimeMillis() - start;
                logger.warn("Run time for " + day.toString() + ": " + runTime);


            }

            // Optional: Generate skims if necessary
            if (Resources.instance.getBoolean(Properties.PRINT_OUT_SKIM, false)) {
                for (Day day : controlers.keySet()) {
                    CarSkimUpdater skimUpdater = new CarSkimUpdater(controlers.get(day), model.getData(), model.getScenarioName());
                    skimUpdater.run();
                }
            }
        } else {
            logger.info("runPtAssignment is set to false; skipping PT assignment.");
        }
    }
}