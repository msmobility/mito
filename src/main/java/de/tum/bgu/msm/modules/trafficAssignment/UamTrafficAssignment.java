package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import net.bhl.matsim.uam.run.RunUAMScenario;

import java.util.HashSet;
import java.util.Set;

import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.Controler;

public class UamTrafficAssignment extends TrafficAssignment {

	private int numberOfThreads = 4;

	public UamTrafficAssignment(DataSet dataSet, String scenarioName) {
		super(dataSet, scenarioName);
	}

	@Override
	protected void configMatsim() {
		String[] args = { "--city", "Munich" };
		RunUAMScenario.parseArguments(args);
		matsimConfig = RunUAMScenario.createConfig();

		// UAM parameters
		matsimConfig.getModules().get("uam").addParam("inputUAMFile",
				Resources.INSTANCE.getString(Properties.UAM_VEHICLES));
		matsimConfig.getModules().get("uam").addParam("availableAccessModes", "walk,car");
		matsimConfig.getModules().get("uam").addParam("parallelRouters", "" + numberOfThreads);
		matsimConfig.getModules().get("uam").addParam("searchRadius", "15000");
		matsimConfig.getModules().get("uam").addParam("walkDistance", "500");
		matsimConfig.getModules().get("uam").addParam("routingStrategy", "MINDISTANCE");
		matsimConfig.getModules().get("uam").addParam("ptSimulation", "false");
		
		// UAM planCalcScore activities
		ConfigGroup uamInteractionParam = matsimConfig.getModules().get("planCalcScore").createParameterSet("activityParams");
		uamInteractionParam.addParam("activityType", "uam_interaction");
		uamInteractionParam.addParam("scoringThisActivityAtAll", "false");
		matsimConfig.getModules().get("planCalcScore").addParameterSet(uamInteractionParam);
		
		// UAM planCalcScore modes
		String[] modeScores = { "uam",
				"access_uam_walk", "egress_uam_walk",
				"access_uam_car", "egress_uam_car",
				"access_uam_bike", "egress_uam_bike" };
		for (String modeScore : modeScores) {
			ConfigGroup modeParam = matsimConfig.getModules().get("planCalcScore").createParameterSet("modeParams");
			modeParam.addParam("mode", modeScore);
			modeParam.addParam("constant", "0.0");
			modeParam.addParam("marginalUtilityOfDistance_util_m", "0.0");
			modeParam.addParam("marginalUtilityOfTraveling_util_hr", "0.0");
			modeParam.addParam("monetaryDistanceRate", "0.0");
			matsimConfig.getModules().get("planCalcScore").addParameterSet(modeParam);
		}
		
		matsimConfig = ConfigureMatsim.configureMatsim(matsimConfig);

		String runId = "mito_assignment";
		matsimConfig.controler().setRunId(runId);
		matsimConfig.controler().setOutputDirectory(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/"
				+ outputSubDirectory + "/trafficAssignment");
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

		CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
		skimUpdater.run();
		dataSet.setMatsimControler(controler);

		WaitingTimesUpdater waitingTimesUpdater = new WaitingTimesUpdater(dataSet);

		int lastIteration = matsimConfig.controler().getLastIteration();
		String inputFileName = matsimConfig.controler().getOutputDirectory() + "/ITERS/it." + lastIteration + "/" +
		matsimConfig.controler().getRunId() + "." + lastIteration + ".uamdemand.csv";

		String outputFileName = matsimConfig.controler().getOutputDirectory() + "/vertiportWaitingTimes.csv";

		waitingTimesUpdater.run(inputFileName, outputFileName);


	}

}
