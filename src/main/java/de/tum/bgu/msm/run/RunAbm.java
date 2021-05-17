package de.tum.bgu.msm.run;

import de.tum.bgu.msm.AbmModel;
import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.trafficAssignment.CarSkimUpdater;
import de.tum.bgu.msm.trafficAssignment.ConfigureMatsim;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * Implements the Transport in Microsimulation Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 */
class RunAbm {

    private static final Logger logger = Logger.getLogger(RunAbm.class);

    public static void main(String[] args) {
        logger.info("Started the ABM model");

        AbmModel model =AbmModel.createModel(args[0], MunichImplementationConfig.get());
        model.runModel();


    }
}
