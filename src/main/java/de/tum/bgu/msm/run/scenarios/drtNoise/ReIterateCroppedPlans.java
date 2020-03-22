package de.tum.bgu.msm.run.scenarios.drtNoise;

import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

public class ReIterateCroppedPlans {

    public static final double SCALE = 0.2;

    public static void main(String[] args) {

        final Config config = ConfigUtils.createConfig();
        ConfigureMatsim.setDemandSpecificConfigSettings(config);
        String outputSubDirectory = "C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\reiterate5";
        config.controler().setOutputDirectory(outputSubDirectory);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);


        config.qsim().setFlowCapFactor(SCALE);
        config.qsim().setStorageCapFactor(SCALE);
        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        config.controler().setFirstIteration(0);
        config.controler().setLastIteration(50);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.qsim().setEndTime(28 * 3600);
        //config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.6);
            config.strategy().addStrategySettings(strategySettings);
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.1);
            config.strategy().addStrategySettings(strategySettings);
        }


        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);


        MutableScenario matsimScenario = ScenarioUtils.createMutableScenario(config);
        new PopulationReader(matsimScenario).readFile("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedPopulation.xml.gz");

        final Population oldPop = matsimScenario.getPopulation();
        Population newPop = PopulationUtils.createPopulation(config);

        for (Person next : oldPop.getPersons().values()) {
            if (Math.random() < SCALE) {
               newPop.addPerson(next);
            }
        }

        matsimScenario.setPopulation(newPop);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("C:\\Users\\Nico\\tum\\moia-msm\\cleverShuttleOperationArea\\croppedDenseNetwork.xml.gz");
        matsimScenario.setNetwork(network);

        Controler controler = new Controler(matsimScenario);
        controler.run();


    }
}
