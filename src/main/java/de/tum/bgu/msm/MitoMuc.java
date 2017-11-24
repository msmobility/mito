package de.tum.bgu.msm;

import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 *
 */

class MitoMuc {

    private static final Logger logger = Logger.getLogger(MitoMuc.class);

    public static void main(String[] args) {
        MitoMuc mito = new MitoMuc();
        ResourceBundle rb = MitoUtil.createResourceBundle(args[0]);
        MitoUtil.setBaseDirectory(rb.getString("base.directory"));
        mito.run(rb);
    }

    private void run (ResourceBundle resources) {
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        MitoModel model = new MitoModel(resources);
        model.initializeStandAlone();
        model.runModel();
    }
}
