package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;

import java.util.List;

/**
 * Created by Nico on 14.07.2017.
 */
public abstract class Module {

    protected final DataSet dataSet;
    protected final List<Purpose> purposes;

    protected Module(DataSet dataSet, List<Purpose> purposes) {
        this.dataSet = dataSet;
        this.purposes = purposes;
    }

    public abstract void run();

}
