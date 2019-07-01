package de.tum.bgu.msm.modules.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import net.bhl.matsim.uam.run.RunUAMScenario;

import java.util.HashSet;
import java.util.Set;

import org.matsim.core.controler.Controler;

public class UamTrafficAssignment extends TrafficAssignment {

	private static Controler controler;

	public UamTrafficAssignment(DataSet dataSet, String scenarioName) {
		super(dataSet, scenarioName);
	}

	@Override
	protected void configMatsim() {
		String[] args = { "--config-path \"\"", "--city Munich" };
		RunUAMScenario.parseArguments(args);
		matsimConfig = RunUAMScenario.initialiseConfig();
		
		// UAM parameters
		matsimConfig.getModules().get("uam").addParam("inputFile", "uam.xml");
		matsimConfig.getModules().get("uam").addParam("availableAccessModes", "walk");
		matsimConfig.getModules().get("uam").addParam("parallelRouters", "16");
		matsimConfig.getModules().get("uam").addParam("searchRadius", "5050");
		matsimConfig.getModules().get("uam").addParam("walkDistance", "500");
		matsimConfig.getModules().get("uam").addParam("routingStrategy", "maxutility");
		matsimConfig.getModules().get("uam").addParam("ptSimulation", "true");

		matsimConfig = ConfigureMatsim.configureMatsim(matsimConfig);

		String runId = "mito_assignment";
		matsimConfig.controler().setRunId(runId);
		matsimConfig.controler().setOutputDirectory(Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/"
				+ outputSubDirectory + "/trafficAssignment");
		matsimConfig.network().setInputFile(Resources.INSTANCE.getString(Properties.MATSIM_NETWORK_FILE));

		matsimConfig.qsim().setNumberOfThreads(16);
		matsimConfig.global().setNumberOfThreads(16);
		matsimConfig.parallelEventHandling().setNumberOfThreads(16);
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
				new String[] { "autoDriver", "uam" });
		Set<String> networkModesSet = new HashSet<>();

		for (String mode : networkModes) {
			String matsimMode = Mode.getMatsimMode(Mode.valueOf(mode));
			if (!networkModesSet.contains(matsimMode)) {
				networkModesSet.add(matsimMode);
			}
		}

		matsimConfig.plansCalcRoute().setNetworkModes(networkModesSet);
	}

	protected void runMatsim() {
		RunUAMScenario.initialiseControler().run();

		CarSkimUpdater skimUpdater = new CarSkimUpdater(controler, matsimScenario.getNetwork(), dataSet);
		skimUpdater.run();
		dataSet.setMatsimControler(controler);
	}

}
