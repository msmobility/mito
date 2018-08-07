package de.tum.bgu.msm.modules.externalFlows;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;


/*
* This class creates the matsim population for external flows and is not to be used with MITO
 */

public class ExternalFlowStandAlone {

    private static Logger logger = Logger.getLogger(ExternalFlowStandAlone.class);


    public static void main (String args[]){

        Resources.initializeResources(args[0]);
        MitoUtil.initializeRandomNumber();

        float scalingFactor = 0.05f;

        LongDistanceTraffic longDistanceTraffic = new LongDistanceTraffic(new DataSet());

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);
        Population matsimPopulation = scenario.getPopulation();

        matsimPopulation = longDistanceTraffic.addLongDistancePlans(scalingFactor, matsimPopulation);

        PopulationWriter populationWriter = new PopulationWriter(matsimPopulation);
        populationWriter.write("input/externalFlows/population.xml");

    }

}
