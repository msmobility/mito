package de.tum.bgu.msm.scenarios.drtNoise;

import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

public class RunMatsim {

    public static void main(String[] args) {

        Config  config = ConfigureMatsim.configureMatsim();
        MutableScenario scenario = (MutableScenario) ScenarioUtils.createScenario(config);

        final Population population = scenario.getPopulation();
        new PopulationReader(scenario).readFile("D:\\resultStorage\\moia-msm\\realisticModeChoice\\matsimPlans.xml.gz");

        Population replace = PopulationUtils.createPopulation(config);

        for(Person person: population.getPersons().values()) {
            final Leg l = TripStructureUtils.getLegs(person.getSelectedPlan()).get(0);
            if(TransportMode.car.equals(l.getMode())) {
                replace.addPerson(person);
            }
        }

        scenario.setPopulation(replace);

        new MatsimNetworkReader(scenario.getNetwork()).readFile("C:\\Users\\Nico\\tum\\fabilut\\gitproject\\muc\\input\\mito\\trafficAssignment\\studyNetworkDense.xml");

        config.controler().setOutputDirectory("./drtFull");
        config.strategy().setMaxAgentPlanMemorySize(3);
        Controler controler = new Controler(scenario);
        controler.run();
    }
}
