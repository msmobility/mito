package de.tum.bgu.msm;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Implements the Transport in Microsimulation Orchestrator (TIMO)
 * @author Rolf Moeckel
 * Created on Feb 12, 2017 in Munich, Germany
 *
 */

public class MitoMuc {

    private static Logger logger = Logger.getLogger(MitoMuc.class);

    public static void main(String[] args) {
        // main run method

        MitoMuc mito = new MitoMuc();
        ResourceBundle rb = MitoUtil.createResourceBundle(args[0]);
        MitoUtil.setBaseDirectory(rb.getString("base.directory"));
        mito.run(rb);
    }

    private void run (ResourceBundle rb) {
        // main run method
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        MitoModel model = new MitoModel(rb);
        model.initializeStandAlone();
        model.runModel();
    }
}
