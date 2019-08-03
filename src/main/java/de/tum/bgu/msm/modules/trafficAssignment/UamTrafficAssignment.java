package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import net.bhl.matsim.uam.router.strategy.UAMStrategy;
import net.bhl.matsim.uam.run.RunUAMScenario;
import net.bhl.matsim.uam.scenario.utils.ConfigAddUAMParameters;
import org.matsim.core.controler.Controler;

import java.util.HashSet;
import java.util.Set;

/**
 * MATSim assignment model using the uam_extension (developed by BHL and not available yet as open-source)
 */
public class UamTrafficAssignment extends TrafficAssignment {

	private int numberOfThreads = Runtime.getRuntime().availableProcessors();

	public UamTrafficAssignment(DataSet dataSet, String scenarioName, int iteration) {
		super(dataSet, scenarioName, iteration);
	}

	@Override
	protected void configMatsim() {
		String[] args = { "--city", "Munich" };
		RunUAMScenario.parseArguments(args);
		matsimConfig = RunUAMScenario.createConfig();

		// UAM parameters
		ConfigAddUAMParameters.addUAMParameters(
				matsimConfig,
				Resources.INSTANCE.getString(Properties.UAM_VEHICLES),
				"walk,car,bike,pt",
				numberOfThreads,
				50000,
				500,
				UAMStrategy.UAMStrategyType.PREDEFINED,
				false
		);
		
		matsimConfig = ConfigureMatsim.configureMatsim(matsimConfig);

		String runId = "mito_assignment";
		matsimConfig.controler().setRunId(runId);
		matsimConfig.controler().setOutputDirectory(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/"
				+ outputSubDirectory + "/trafficAssignment/" + iteration  + "/");
		matsimConfig.network().setInputFile(Resources.INSTANCE.getString(Properties.MATSIM_NETWORK_FILE));

		matsimConfig.qsim().setNumberOfThreads(numberOfThreads);
		matsimConfig.global().setNumberOfThreads(numberOfThreads);
		matsimConfig.parallelEventHandling().setNumberOfThreads(numberOfThreads);
		matsimConfig.qsim().setUsingThreadpool(false);

		matsimConfig.controler().setLastIteration(Resources.INSTANCE.getInt(Properties.MATSIM_ITERATIONS));
		matsimConfig.controler().setWritePlansInterval(matsimConfig.controler().getLastIteration());
		matsimConfig.controler().setWriteEventsInterval(matsimConfig.controler().getLastIteration());

		matsimConfig.qsim().setStuckTime(10);
		matsimConfig.qsim().setFlowCapFactor(
				SILO_SAMPLING_RATE * Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));
		matsimConfig.qsim().setStorageCapFactor(
				SILO_SAMPLING_RATE * Double.parseDouble(Resources.INSTANCE.getString(Properties.TRIP_SCALING_FACTOR)));

		String[] networkModes = Resources.INSTANCE.getArray(Properties.MATSIM_NETWORK_MODES,
				new String[] { "autoDriver" });
		Set<String> networkModesSet = new HashSet<>();

		for (String mode : networkModes) {
			String matsimMode = Mode.getMatsimMode(Mode.valueOf(mode));
			//do not add uam as network mode, even if it is assigned
			if (!networkModesSet.contains(matsimMode) && !matsimMode.equals("uam")) {
				networkModesSet.add(matsimMode);
			}
		}

		matsimConfig.plansCalcRoute().setNetworkModes(networkModesSet);
	}

	protected void runMatsim() {
		RunUAMScenario.setScenario(matsimScenario);
		Controler controler = RunUAMScenario.createControler();
		controler.run();

		//Do not update car times if car is not simulated!!
		CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
		skimUpdater.run();
		dataSet.setMatsimControler(controler);

		//update waiting times of UAM mode.
		HandlingTimesUpdater handlingTimesUpdater = new HandlingTimesUpdater(dataSet);
		int lastIteration = matsimConfig.controler().getLastIteration();
		String inputFileName = matsimConfig.controler().getOutputDirectory() + "/ITERS/it." + lastIteration + "/" +
		matsimConfig.controler().getRunId() + "." + lastIteration + ".uamdemand.csv";
		String outputFileName = matsimConfig.controler().getOutputDirectory() + "/vertiportWaitingTimes.csv";
		handlingTimesUpdater.run(inputFileName, outputFileName);


	}

}
