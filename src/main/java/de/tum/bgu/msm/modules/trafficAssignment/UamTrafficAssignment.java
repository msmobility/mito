package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import net.bhl.matsim.uam.router.strategy.UAMStrategy;
import net.bhl.matsim.uam.run.RunUAMScenario;
import net.bhl.matsim.uam.run.UAMModule;
import net.bhl.matsim.uam.scenario.utils.ConfigAddUAMParameters;
import org.matsim.core.config.Config;
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
		String[] args = {};
		RunUAMScenario.parseArguments(args);
		matsimConfig = RunUAMScenario.createConfig();

		// Selected routing strategy
		UAMStrategy.UAMStrategyType strategy = Resources.INSTANCE.getBoolean(Properties.UAM_MATSIM_ROUTING, false) ?
				UAMStrategy.UAMStrategyType.MINTRAVELTIME : UAMStrategy.UAMStrategyType.PREDEFINED;

		// If UAM MATSim routing is used: reduce searchRadius to 10km to speed up computation; if MATSim does not have
		// to do routing, set search radius to cover whole study area, to avoid MATSim not finding the UAM stations
		// as defined by MITO (and handed to MATSim via agents' plans)
		int searchRadius = Resources.INSTANCE.getBoolean(Properties.UAM_MATSIM_ROUTING, false) ?
				10000 : 300000;

		// UAM parameters
		ConfigAddUAMParameters.addUAMParameters(
				matsimConfig,
				Resources.INSTANCE.getString(Properties.UAM_VEHICLES),
				"walk,car,bike,pt",
				numberOfThreads,
				searchRadius,
				500,
				strategy,
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
		matsimConfig.qsim().setUsingThreadpool(true);

		matsimConfig.controler().setLastIteration(Resources.INSTANCE.getInt(Properties.MATSIM_ITERATIONS));
		matsimConfig.controler().setWritePlansInterval(matsimConfig.controler().getLastIteration());
		matsimConfig.controler().setWriteEventsInterval(matsimConfig.controler().getLastIteration());

		matsimConfig.linkStats().setAverageLinkStatsOverIterations(Math.min(matsimConfig.controler().getLastIteration(), 5));
		matsimConfig.linkStats().setWriteLinkStatsInterval(matsimConfig.controler().getLastIteration());

		matsimConfig.qsim().setStuckTime(10);
		//TODO temporary set capacity factors to the UAM scale factor
		matsimConfig.qsim().setFlowCapFactor(Resources.INSTANCE.getDouble(Properties.TRIP_SCALING_FACTOR + ".uam", 1));
		matsimConfig.qsim().setStorageCapFactor(Resources.INSTANCE.getDouble(Properties.TRIP_SCALING_FACTOR + ".uam", 1));

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

		//TODO temporary do not update car times if car is not simulated or subsample is too low to be representative!!
		//CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
		//skimUpdater.run();
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
