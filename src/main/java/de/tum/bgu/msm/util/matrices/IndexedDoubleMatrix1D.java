package de.tum.bgu.msm.util.matrices;

import cern.colt.map.tint.OpenIntIntHashMap;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.Id;

import java.util.Collection;

public class IndexedDoubleMatrix1D extends DenseDoubleMatrix1D {

    private final OpenIntIntHashMap index;

    /**
     * Creates a new id-indexed vector for double values. Each id will be associated with a subsequent
     * array index used for the vector. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix1D(Collection<? extends Id> entries) {
        super(entries.size());

        int counter = 0;
        index = new OpenIntIntHashMap(entries.size());
        for(Id row: entries) {
            index.put(row.getId(), counter++);
        }
    }

    /**
     * Sets the double value for the given indexed ids
     * @param i id of row entry
     * @param val the value associated in the underlying indexed matrix
     */
    public void setIndexed(int i, double val) {
        setQuick(index.get(i), val);
    }

    /**
     * Gets the double value for the given indexed ids
     * @param i id of row entry
     */
    public double getIndexed(int i) {
        return getQuick(index.get(i));
    }
}

