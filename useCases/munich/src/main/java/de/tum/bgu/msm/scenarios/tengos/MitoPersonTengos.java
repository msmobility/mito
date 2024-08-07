package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.*;

import java.util.ArrayList;


public class MitoPersonTengos extends MitoPerson7days {
    private ArrayList<Integer> alterLists;

    public MitoPersonTengos(int id, MitoHousehold household, MitoOccupationStatus mitoOccupationStatus, MitoOccupation occupation, int age, MitoGender mitoGender, boolean driversLicense) {
        super(id, household, mitoOccupationStatus, occupation, age, mitoGender, driversLicense);
    }

    public ArrayList<Integer> getAlterLists() {
        return alterLists;
    }

    public void setAlterLists(ArrayList<Integer> alterLists) {
        this.alterLists = alterLists;
    }


}
