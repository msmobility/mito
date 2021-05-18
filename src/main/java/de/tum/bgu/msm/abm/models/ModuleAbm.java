package de.tum.bgu.msm.abm.models;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.plans.Plan;

public abstract class ModuleAbm {

    public final DataSet dataSet;

    protected ModuleAbm(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void run(Plan plan, MitoPerson person, MitoHousehold household);
}
