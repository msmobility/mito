package de.tum.bgu.msm;

import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 *
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 */
class MitoMuc {

    private static final Logger logger = Logger.getLogger(MitoMuc.class);

    public static void main(String[] args) {

        for (String arg : args) {

            try {
                logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
                logger.info(arg);
                MitoModel model = MitoModel.standAloneModel(arg, MunichImplementationConfig.get());
                model.runModel();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }
}
