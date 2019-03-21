package de.tum.bgu.msm.util.matrices;

import cern.colt.map.tint.OpenIntIntHashMap;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.Id;

import java.util.Collection;

public class IndexedDoubleMatrix1D  {

    private final OpenIntIntHashMap externalId2InternalIndex;
    private final OpenIntIntHashMap internalIndex2ExternalId;

    private final DoubleMatrix1D delegate;

    /**
     * Creates a new id-indexed vector for double values. Each id will be associated with a subsequent
     * array index used for the vector. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix1D(Collection<? extends Id> entries) {
        delegate = new DenseDoubleMatrix1D(entries.size());
        int counter = 0;
        externalId2InternalIndex = new OpenIntIntHashMap(entries.size());
        internalIndex2ExternalId = new OpenIntIntHashMap(entries.size());
        for(Id row: entries) {
            externalId2InternalIndex.put(row.getId(), counter);
            internalIndex2ExternalId.put(counter, row.getId());
            counter++;
        }
    }

    public IndexedDoubleMatrix1D(DoubleMatrix1D delegate, OpenIntIntHashMap external2InternalLookup, OpenIntIntHashMap internal2ExternalLookup) {
        this.externalId2InternalIndex = external2InternalLookup;
        this.internalIndex2ExternalId = internal2ExternalLookup;
        this.delegate = delegate;
    }

    /**
     * Sets the double value for the given indexed ids
     * @param i id of row entry
     * @param val the value associated in the underlying indexed matrix
     */
    public void setIndexed(int i, double val) {
        delegate.setQuick(externalId2InternalIndex.get(i), val);
    }

    /**
     * Gets the double value for the given indexed ids
     * @param i id of row entry
     */
    public double getIndexed(int i) {
        return delegate.getQuick(externalId2InternalIndex.get(i));
    }

    /**
     * Returns the non-indexed double matrix with indices ranging from 0....n-1, n being the number of rows.
     * @return
     */
    public double[] toNonIndexedArray() {
        return delegate.toArray();
    }

    /**
     * Returns the associated id for the given internal index.
     * @param index
     * @return
     */
    public int getIdForInternalIndex(int index) {
        return this.internalIndex2ExternalId.get(index);
    }

    /**
     * Returns the sum of all cells; Sum( x[i] ).
     * @return
     */
    public double zSum() {
        return delegate.zSum();
    }
}

