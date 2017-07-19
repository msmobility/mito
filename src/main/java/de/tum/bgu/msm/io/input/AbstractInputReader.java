package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.data.DataSet;

/**
 * Created by Nico on 17.07.2017.
 */
abstract class AbstractInputReader {

    protected final DataSet dataSet;

    public AbstractInputReader(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void read();
}
