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
    private ResourceBundle rb;


    public static void main(String[] args) {
        // main run method

        MitoMuc mito = new MitoMuc();
        ResourceBundle rb = MitoUtil.createResourceBundle(args[0]);
        mito.run(rb);
    }


    private void run (ResourceBundle rb) {
        // main run method
        long startTime = System.currentTimeMillis();
        logger.info("Started the Microsimulation Transport Orchestrator (MITO)");
        this.rb = rb;
        MitoModel model = new MitoModel(rb);
        model.readData();
        model.runModel();

        logger.info("Finished the Microsimulation Transport Orchestrator (MITO)");
        float endTime = MitoUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");

    }

}
