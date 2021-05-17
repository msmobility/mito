package de.tum.bgu.msm.abm.models;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.plans.Plan;

public abstract class ModuleAbm {

    public final DataSet dataSetAbm;

    protected ModuleAbm(DataSet dataSetAbm) {
        this.dataSetAbm = dataSetAbm;
    }

    public abstract void run(Plan Plan, MitoPerson person, MitoHousehold household);
}
