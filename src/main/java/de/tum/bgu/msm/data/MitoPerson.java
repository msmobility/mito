package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */

public class MitoPerson implements Serializable {
    static Logger logger = Logger.getLogger(MitoPerson.class);

    private int id;
    private MitoHousehold hh;
    private int occupation;
    private int workplace;
    private int workzone;


    public MitoPerson(int id, MitoHousehold hh, int occupation, int workplace) {
        this.id = id;
        this.hh = hh;
        this.occupation = occupation;
        this.workplace = workplace;
    }


    public int getWorkplace() {
        return workplace;
    }


    public void setWorkzone(int workzone) {
        this.workzone = workzone;
    }


    public int getOccupation() {
        return occupation;
    }


    public int getWorkzone() {
        return workzone;
    }

    public int getId() {
        return this.id;
    }
}
