package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoData;
import de.tum.bgu.msm.data.DataSet;

import java.util.ResourceBundle;

/**
 * Created by Nico on 14.07.2017.
 */
public abstract class Module {

    protected DataSet dataSet;

    public Module(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void run();

}
