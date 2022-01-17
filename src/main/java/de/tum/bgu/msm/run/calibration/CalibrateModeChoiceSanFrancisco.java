package de.tum.bgu.msm.run.calibration;

import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import de.tum.bgu.msm.util.sanFrancisco.SanFranciscoImplementationConfig;
import org.apache.log4j.Logger;

/**
 * Implements the Transport in Microsimulation Orchestrator (MITO)
 *
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 */
class CalibrateModeChoiceSanFrancisco {

    private static final Logger logger = Logger.getLogger(CalibrateModeChoiceSanFrancisco.class);

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        MitoModelSanFranciscoForModeChoiceCalibration model = MitoModelSanFranciscoForModeChoiceCalibration.standAloneModel(args[0],
                SanFranciscoImplementationConfig.get());
        model.run();


    }
}
