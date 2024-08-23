package de.tum.bgu.msm.scenarios.tengos;

import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import de.tum.bgu.msm.MitoModel2;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
//import de.tum.bgu.msm.trafficAssignment.ConfigureMatsimPt;
//import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
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

public class MatsimRun {
    private static final Logger logger = Logger.getLogger(MatsimRun.class);
    private static boolean runMito = false;
    private static boolean runPtAssignment = true;
    private static String popFile = "C:\\models/MITO/mitoMunich/input/trafficAssignment/pt/matsimPlans.xml.gz";
    private static String subTripFile = "C:\\models/MITO/mitoMunich/input/trafficAssignment/pt/plan_busOnly/run-0.xml.gz";
    private static boolean subTrip = true;
    private static int instance = 0;
    private static double planScale = 0.65;
    private static int lastItration = 1;
    private static double reroute = 1;
    private static boolean useSBB = true;
    private static boolean deterministic = false;
    private static int minCapacityFactor = 10;
    private static int maxCapacityFactor = 10;
    private static int maxPlan = 5;
    private static double maxSearchRadius = 1000;
    private static double betaTransfer = 300;
    public static void main(String[] args) {
        MitoModel2 model = null;
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) based on 2017 models");
        if(runMito){
            //model = MitoModel2.standAloneModel(args[0], MunichImplementationConfig.get());
            model.run();
        }else{
            Resources.initializeResources(args[0]);
        }
        for(int factor = minCapacityFactor;factor<=maxCapacityFactor;factor++){
            logger.info("Started pt assignment with capacity factor: " + factor);
            Config config;
            MutableScenario matsimScenario;
            if (runPtAssignment) {
                logger.info("Running traffic assignment in MATsim");
                String router = useSBB?"SBB":"woSBB";
                String dt = deterministic?"dt":"notDt";
                String scenario = subTrip?"subTrip_benedikt":Resources.instance.getString(Properties.SCENARIO_NAME);
                String outputSubDirectory = "scenOutput/" + scenario +
                        "_it" + lastItration + "_reroute" + reroute + "capa" + factor + "" + router +
                        "_" + dt + "_maxPlan" + maxPlan + "_maxRadius" + maxSearchRadius + "_betaTransfer" + betaTransfer + "_instance"+ instance +"/" +Resources.instance.getString(Properties.SCENARIO_YEAR);
                //old config
                //config = ConfigureMatsimPt.configureMatsim(lastItration,reroute,planScale,factor,outputSubDirectory, maxPlan);
                config = ConfigureMatsimPt.configureMatsim(lastItration,reroute,planScale,factor, maxPlan);
                matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(config);
                if (runMito){
                    matsimScenario.setPopulation(model.getData().getPopulation());
                    logger.warn("size of pt pop:" + matsimScenario.getPopulation().getPersons().size());
                }else {
                    PopulationReader popReader = new PopulationReader(matsimScenario);
                    Population populationPt = PopulationUtils.createPopulation(ConfigUtils.createConfig());
                    if(subTrip){
                        popReader.readFile(subTripFile);
                        /*//debugging
                        for(Person pp : matsimScenario.getPopulation().getPersons().values()){
                            if (pp.getId().toString().equals("379637_2900")){
                                populationPt.addPerson(pp);
                            }
                        }
                        matsimScenario.setPopulation(populationPt);
                        //debugging end*/
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
                    }
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
    private static List<Trip> readTrips(String path) {
        List<Trip> trips = new ArrayList<>();
        File file = new File(path);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            final String header = reader.readLine();
            String record = reader.readLine();
            while (record!= null) {
                final String[] split = record.split(",");
                int id = Integer.parseInt(split[1]);
                trips.add(new Trip(id));
                record = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trips;
    }
    public static class Trip {
        private final int id;
        public Trip(int id) {
            this.id = id;
        }
    }
}
