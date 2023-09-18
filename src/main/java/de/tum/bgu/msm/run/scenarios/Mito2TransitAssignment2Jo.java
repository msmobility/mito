package de.tum.bgu.msm.run.scenarios;

import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import de.tum.bgu.msm.MitoModel2;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsimPt;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Mito2TransitAssignment2Jo {

    private static final Logger logger = Logger.getLogger(Mito2TransitAssignment2Jo.class);
    private static boolean runMito = false;
    private static boolean runPtAssignment = true;
    private static String popFile = "C:\\models\\MITO/mitoMunich/externalDemand/trips_total_5perc_2.xml.gz";
    private static double planScale = 0.05;
    private static int lastItration = 25;
    private static double reroute = 0.2;
    private static boolean useSBB = true;
    private static boolean deterministic = false;
    private static int maxPlan = 5;
    private static double maxSearchRadius = 2000;
    private static double betaTransfer = 300;




    public static void main(String[] args) {
        MitoModel2 model = null;
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        if(runMito){
            model = MitoModel2.standAloneModel(args[0], MunichImplementationConfig.get());
            System.exit(1);
            model.run();
        }else{
            Resources.initializeResources(args[0]);
        }

        Config config;
        MutableScenario matsimScenario;
        if (runPtAssignment) {
            logger.info("Running traffic assignment in MATsim");
            String outputSubDirectory = "scenOutput/" + Resources.instance.getString(Properties.SCENARIO_NAME) + "/" +Resources.instance.getString(Properties.SCENARIO_YEAR);

            config = ConfigureMatsimPt.configureMatsim(lastItration,reroute,planScale,10, maxPlan);
            config.controler().setOutputDirectory(Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + "/trafficAssignment");

            matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);

            PopulationReader popReader = new PopulationReader(matsimScenario);
            Population populationPt = PopulationUtils.createPopulation(ConfigUtils.createConfig());

            if (runMito){
                logger.warn("Total number of all agents: " + matsimScenario.getPopulation().getPersons().size());
                MainModeIdentifierImpl mainModeIdentifier = new MainModeIdentifierImpl();
                for (Person pp : model.getData().getPopulation().getPersons().values()) {
                    String mode = mainModeIdentifier.identifyMainMode(TripStructureUtils.getLegs(pp.getSelectedPlan()));
                    if (mode.equals("pt")) {
                        populationPt.addPerson(pp);
                    }
                }
                matsimScenario.setPopulation(populationPt);
                logger.warn("size of pt pop:" + matsimScenario.getPopulation().getPersons().size());
            }else {
                popReader.readFile(popFile);
                logger.warn("Total number of all agents: " + matsimScenario.getPopulation().getPersons().size());

                MainModeIdentifierImpl mainModeIdentifier = new MainModeIdentifierImpl();
                for (Person pp : matsimScenario.getPopulation().getPersons().values()) {
                    String mode = mainModeIdentifier.identifyMainMode(TripStructureUtils.getLegs(pp.getSelectedPlan()));
                    if (mode.equals("pt")) {
                        populationPt.addPerson(pp);
                    }
                }
                matsimScenario.setPopulation(populationPt);
                logger.warn("size of pt pop:" + matsimScenario.getPopulation().getPersons().size());

            }

            Controler controler = new Controler(matsimScenario);

            if(useSBB){

                // To use the deterministic pt simulation (Part 1 of 2):
                controler.addOverridingModule(new SBBTransitModule());

                // To use the fast pt router (Part 1 of 1)
                controler.addOverridingModule(new SwissRailRaptorModule());

                // To use the deterministic pt simulation (Part 2 of 2):
                controler.configureQSimComponents(components -> {
                    new SBBTransitEngineQSimModule().configure(components);

                    // if you have other extensions that provide QSim components, call their configure-method here
                });

                ConfigureMatsimPt.setSBBConfig(controler.getConfig(),deterministic, maxSearchRadius, betaTransfer);

            }

            long start = System.currentTimeMillis();
            controler.run();
            long runTime = System.currentTimeMillis()-start;
            logger.warn("Run time: " + runTime);
        }
    }
}

