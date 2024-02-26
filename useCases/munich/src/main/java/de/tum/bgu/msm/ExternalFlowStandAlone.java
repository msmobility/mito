package de.tum.bgu.msm;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.modules.plansConverter.externalFlows.LongDistanceTraffic;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Arrays;


/*
* This class creates the matsim population for external flows and is not to be used with MITO
 */

public class ExternalFlowStandAlone {

    private static Logger logger = Logger.getLogger(ExternalFlowStandAlone.class);


    public static void main (String args[]){

        Resources.initializeResources(args[0]);
        MitoUtil.initializeRandomNumber();

        float scalingFactor = 0.05f;

        DataSet dataSet = new DataSet();
        dataSet.setYear(Resources.instance.getInt(Properties.SCENARIO_YEAR, 2011));
        LongDistanceTraffic longDistanceTraffic = new LongDistanceTraffic(dataSet, scalingFactor, Arrays.asList(Purpose.values()));

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);
        Population matsimPopulation = scenario.getPopulation();
        dataSet.setPopulation(matsimPopulation);

        longDistanceTraffic.run();

        PopulationWriter populationWriter = new PopulationWriter(matsimPopulation);
        populationWriter.write("input/externalFlows/population.xml");

    }

}
