package de.tum.bgu.msm;

import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 *
 */
class MitoMuc {

    private static final Logger logger = Logger.getLogger(MitoMuc.class);

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        MitoModel model = MitoModel.standAloneModel(args[0], MunichImplementationConfig.get());
        model.runModel();
    }
}
