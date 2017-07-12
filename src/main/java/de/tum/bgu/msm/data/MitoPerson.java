package de.tum.bgu.msm.data;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
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
    private static final Map<Integer,MitoPerson> personMap = new HashMap<>();

    private int id;
    private MitoHousehold hh;
    private int occupation;
    private int workplace;
    private int workzone;


    public MitoPerson(int id, int hhId, int occupation, int workzone) {
        this.id = id;
        this.hh = MitoHousehold.getHouseholdFromId(hhId);
        this.occupation = occupation;
        this.workzone = workzone;
        personMap.put(id,this);
    }

    public int getId() {
        return id;
    }


    public MitoHousehold getHh() {
        return hh;
    }


    public void setHh(MitoHousehold hh) {
        this.hh = hh;
    }

    public static MitoPerson getMitoPersonFromId(int id) {
        return personMap.get(id);
    }

    public void setWorkplace(int workplace) {
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


    public static MitoPerson[] getMitoPersons(){
        return personMap.values().toArray(new MitoPerson[personMap.size()]);
    }
}
