package de.tum.bgu.msm;

import de.tum.bgu.msm.modules.logsumAccessibility.Accessibility;
import de.tum.bgu.msm.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.util.munich.MunichImplementationConfig;
import org.apache.log4j.Logger;

/**
 * Implements the Transport in Microsimulation Orchestrator (MITO) to calculate logsum-based accessibilities
 * @author Ana Moreno
 * Created on Jun 24, 2019 in Munich, Germany
 *
 */
class MitoAccessibilityMuc {

    private static final Logger logger = Logger.getLogger(MitoAccessibilityMuc.class);

    public static void main(String[] args) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO) Accessibility Calculation");
        MitoModel model = MitoModel.standAloneModel(args[0], MunichImplementationConfig.get());
        model.calculateAccessibility();

    }
}
