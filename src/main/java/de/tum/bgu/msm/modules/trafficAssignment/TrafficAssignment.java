package de.tum.bgu.msm.modules.trafficAssignment;

import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;

public abstract class TrafficAssignment extends Module {

	protected final String scenarioName;
	protected Config matsimConfig;
	protected MutableScenario matsimScenario;
	protected String outputSubDirectory;
	protected final double SILO_SAMPLING_RATE = 1;
	protected final int iteration;

	protected TrafficAssignment(DataSet dataSet, String scenarioName, int iteration) {
		super(dataSet);
		this.scenarioName = scenarioName;
		this.iteration = iteration;
	}

	@Override
	public void run() {
		outputSubDirectory = "scenOutput/" + scenarioName + "/" + dataSet.getYear();
		configMatsim();
		createPopulation();
		runMatsim();
	}

	protected abstract void configMatsim();

	protected void createPopulation() {
		MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator();
		Population population = matsimPopulationGenerator.generateMatsimPopulation(dataSet, matsimConfig);
		if (Resources.INSTANCE.getBoolean(Properties.ADD_EXTERNAL_FLOWS, false)) {
			LongDistanceTraffic longDistanceTraffic = new LongDistanceTraffic(dataSet);
			population = longDistanceTraffic.addLongDistancePlans(
					Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)), population);
		}
		matsimScenario = (MutableScenario) ScenarioUtils.loadScenario(matsimConfig);
		matsimScenario.setPopulation(population);
	}

	protected void runMatsim() {
		final Controler controler = new Controler(matsimScenario);
		controler.run();

		CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
		skimUpdater.run();
		dataSet.setMatsimControler(controler);
	}
}
