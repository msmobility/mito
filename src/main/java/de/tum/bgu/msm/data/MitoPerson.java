package de.tum.bgu.msm.data;

import de.tum.bgu.msm.resources.Gender;
import de.tum.bgu.msm.resources.Occupation;

import java.io.Serializable;

/**
 * Holds person objects for the Microsimulation Transport Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on June 8, 2017 in Munich, Germany
 *
 */

public class MitoPerson implements Serializable {

    private final int id;
    private final int hhId;
    private final Gender gender;
    private Occupation occupation;
    private int workplace;
    private int workzone;
    private int age;
    private boolean driversLicense;

    public MitoPerson(int id, int hhId, Occupation occupation, int workplace, int age, Gender gender, boolean driversLicense) {
        this.id = id;
        this.hhId = hhId;
        this.occupation = occupation;
        this.workplace = workplace;
        this.age = age;
        this.gender = gender;
        this.driversLicense = driversLicense;
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

    public Occupation getOccupation() {
        return occupation;
    }

    public int getWorkzone() {
        return workzone;
    }

    public int getId() {
        return this.id;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean hasDriversLicense() {
        return driversLicense;
    }

    public void setDriversLicense(boolean driversLicense) {
        this.driversLicense = driversLicense;
    }
}
