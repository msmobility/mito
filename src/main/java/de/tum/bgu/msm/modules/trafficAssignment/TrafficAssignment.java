package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;

public class TrafficAssignment extends Module {

    private Config matsimConfig;
    private MutableScenario matsimScenario;
    private String outputDirectory = "output/trafficAssignment/";

    public TrafficAssignment(DataSet dataSet) {
        super(dataSet);

    }

    @Override
    public void run() {
        configMatsim();
        createPopulation();
        runMatsim();

    }

    private void configMatsim() {
        matsimConfig = ConfigUtils.createConfig();
        matsimConfig = ConfigureMatsim.configureMatsim(matsimConfig);

        String runId = "mito_assignment";
        matsimConfig.controler().setRunId(runId);
        matsimConfig.controler().setOutputDirectory(outputDirectory + "output/");
        matsimConfig.network().setInputFile(Resources.INSTANCE.getString(Properties.MATSIM_NETWORK_FILE));

        matsimConfig.qsim().setNumberOfThreads(16);
        matsimConfig.global().setNumberOfThreads(16);
        matsimConfig.parallelEventHandling().setNumberOfThreads(16);
        matsimConfig.qsim().setUsingThreadpool(false);

        matsimConfig.controler().setLastIteration(Resources.INSTANCE.getInt(Properties.MATSIM_ITERATIONS));
        matsimConfig.controler().setWritePlansInterval(matsimConfig.controler().getLastIteration());
        matsimConfig.controler().setWriteEventsInterval(matsimConfig.controler().getLastIteration());

        matsimConfig.qsim().setStuckTime(10);
        matsimConfig.qsim().setFlowCapFactor(Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));
        matsimConfig.qsim().setStorageCapFactor(Math.pow(Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)),0.75));
    }

    private void createPopulation() {
        MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator();
        matsimPopulationGenerator.loadZoneShapeFile();
        Population population = matsimPopulationGenerator.generateMatsimPopulation(dataSet, matsimConfig);
        matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(matsimConfig);
        matsimScenario.setPopulation(population);

        PopulationWriter populationWriter = new PopulationWriter(population);
        populationWriter.write(outputDirectory + "population.xml");
    }

    private void runMatsim() {
        final Controler controler = new Controler(matsimScenario);
        controler.run();
    }

}
