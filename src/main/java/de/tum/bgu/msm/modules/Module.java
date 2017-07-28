package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.DataSet;

/**
 * Created by Nico on 14.07.2017.
 */
abstract class Module {

    final DataSet dataSet;

    Module(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void run();

}
