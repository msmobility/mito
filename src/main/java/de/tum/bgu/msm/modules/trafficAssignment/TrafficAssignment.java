package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class TrafficAssignment extends Module {

    private final String scenarioName;
    private Config matsimConfig;
    private MutableScenario matsimScenario;
    private String outputSubDirectory;
    private final double SILO_SAMPLING_RATE = 20.;

    public TrafficAssignment(DataSet dataSet, String scenarioName) {
        super(dataSet);
        this.scenarioName = scenarioName;
    }

    @Override
    public void run() {
        outputSubDirectory = "scenOutput/" + scenarioName + "/" + dataSet.getYear();
        configMatsim();
        createPopulation();
        runMatsim();
    }

    private void configMatsim() {
        matsimConfig = ConfigUtils.createConfig();
        matsimConfig = ConfigureMatsim.configureMatsim(matsimConfig);

        String runId = "mito_assignment";
        matsimConfig.controler().setRunId(runId);
        matsimConfig.controler().setOutputDirectory(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + "/trafficAssignment");
        matsimConfig.network().setInputFile(Resources.INSTANCE.getString(Properties.MATSIM_NETWORK_FILE));

        matsimConfig.qsim().setNumberOfThreads(16);
        matsimConfig.global().setNumberOfThreads(16);
        matsimConfig.parallelEventHandling().setNumberOfThreads(16);
        matsimConfig.qsim().setUsingThreadpool(false);

        matsimConfig.controler().setLastIteration(Resources.INSTANCE.getInt(Properties.MATSIM_ITERATIONS));
        matsimConfig.controler().setWritePlansInterval(matsimConfig.controler().getLastIteration());
        matsimConfig.controler().setWriteEventsInterval(matsimConfig.controler().getLastIteration());

        matsimConfig.qsim().setStuckTime(10);
        matsimConfig.qsim().setFlowCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));
        matsimConfig.qsim().setStorageCapFactor(SILO_SAMPLING_RATE * Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));


        String[] networkModes = Resources.INSTANCE.getArray(Properties.MATSIM_NETWORK_MODES);
        Set<String> networkModesSet = new HashSet<>();

        for (String mode : networkModes){
            String matsimMode = Mode.getMatsimMode(Mode.valueOf(mode));
            if (!networkModesSet.contains(matsimMode)){
                networkModesSet.add(matsimMode);
            }
        }

        matsimConfig.plansCalcRoute().setNetworkModes(networkModesSet);



    }

    private void createPopulation() {
        MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator();
        Population population = matsimPopulationGenerator.generateMatsimPopulation(dataSet, matsimConfig);
        if (Resources.INSTANCE.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
            LongDistanceTraffic longDistanceTraffic = new LongDistanceTraffic(dataSet);
            population = longDistanceTraffic.addLongDistancePlans(Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)), population);
        }
        matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(matsimConfig);
        matsimScenario.setPopulation(population);
    }

    private void runMatsim() {
        final Controler controler = new Controler(matsimScenario);
        controler.run();

        CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
        skimUpdater.run();

        final TravelTime linkTravelTimes = controler.getLinkTravelTimes();
        dataSet.setMatsimTravelTime(linkTravelTimes);
        dataSet.setMatsimTravelDisutility( controler.getTravelDisutilityFactory().createTravelDisutility(linkTravelTimes));
    }
}
