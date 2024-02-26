package de.tum.bgu.msm.util.matrices;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.map.tint.AbstractIntIntMap;
import cern.colt.map.tint.OpenIntIntHashMap;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class IndexedDoubleMatrix1D  {

    private final AbstractIntIntMap externalId2InternalIndex;
    private final AbstractIntIntMap internalIndex2ExternalId;

    private final DoubleMatrix1D delegate;

    /**
     * Creates a new id-indexed vector for double values. Each id will be associated with a subsequent
     * array index used for the vector. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix1D(Collection<? extends Id> entries) {
        delegate = new DenseDoubleMatrix1D(entries.size());
        List<? extends Id> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Comparator.comparingInt(Id::getId));
        externalId2InternalIndex = new OpenIntIntHashMap(entries.size());
        internalIndex2ExternalId = new OpenIntIntHashMap(entries.size());
        int counter = 0;
        for(Id row: entries) {
            externalId2InternalIndex.put(row.getId(), counter);
            internalIndex2ExternalId.put(counter, row.getId());
            counter++;
        }
    }

    public IndexedDoubleMatrix1D(DoubleMatrix1D delegate, AbstractIntIntMap external2InternalLookup, AbstractIntIntMap internal2ExternalLookup) {
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

    /**
     * Sets all cells to the state specified by value.
     */
    public IndexedDoubleMatrix1D assign(double val) {
        delegate.assign(val);
        return this;
    }

    /**
     * Sets all cells to the state specified by the evaluation of the double function.
     */
    public IndexedDoubleMatrix1D assign(DoubleFunction doubleFunction) {
        delegate.assign(doubleFunction);
        return this;
    }

    public IndexedDoubleMatrix1D assign(IndexedDoubleMatrix1D matrix) {
        delegate.assign(matrix.delegate);
        return this;
    }

    /**
     * Assigns the result of a function to each cell; x[i] = function(x[i],y[i]).
     * Example:
     *        	 // assign x[i] = x[i]<sup>y[i]</sup>
     *        	 m1 = 0 1 2 3;
     *        	 m2 = 0 2 4 6;
     *        	 m1.assign(m2, cern.jet.math.Functions.pow);
     *        	 -->
     *        	 m1 == 1 1 16 729
     *
     */
    public IndexedDoubleMatrix1D assign(IndexedDoubleMatrix1D matrix1D, DoubleDoubleFunction function) {
        delegate.assign(matrix1D.delegate, function);
        return this;
    }

    /**
     * Return the maximum value of this matrix together with its location
     * @return { maximum_value, location };
     */
    public double[] getMaxValAndInternalIndex() {
        return delegate.getMaxLocation();
    }

    /**
     * Return the minimum value of this matrix together with its location
     * @return { minimum_value, location };
     */
    public double[] getMinValAndInternalIndex() {
        return delegate.getMinLocation();
    }

    /**
     * Returns the number of cells.
     * @return
     */
    public long size() {
        return delegate.size();
    }

    /**
     * Constructs and returns a deep copy of the receiver.
     * Note that the returned matrix is an independent deep copy.
     * The returned matrix is not backed by this matrix,
     * so changes in the returned matrix are not reflected in this matrix, and vice-versa.
     */
    public IndexedDoubleMatrix1D copy() {
        return new IndexedDoubleMatrix1D(
                delegate.copy(),
                externalId2InternalIndex.copy(),
                internalIndex2ExternalId.copy());
    }
}

