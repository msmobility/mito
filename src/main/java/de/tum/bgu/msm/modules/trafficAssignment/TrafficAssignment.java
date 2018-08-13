package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.NetworkCleaner;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

public class TrafficAssignment extends Module {

    private Config matsimConfig;
    private MutableScenario matsimScenario;
    private String outputSubDirectory;
    private final double SILO_SMAPLING_RATE = 1.2;

    public TrafficAssignment(DataSet dataSet) {
        super(dataSet);

    }

    @Override
    public void run() {
        outputSubDirectory = "scenOutput/" + MitoModel.getScenarioName() + "/" + dataSet.getYear();
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
        matsimConfig.qsim().setFlowCapFactor(SILO_SMAPLING_RATE * Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));
        matsimConfig.qsim().setStorageCapFactor(SILO_SMAPLING_RATE * Math.pow(Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)), 0.75));
    }

    private void createPopulation() {
        Population population = MatsimPopulationGenerator.generateMatsimPopulation(dataSet, matsimConfig);
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

    }

}
