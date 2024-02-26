package de.tum.bgu.msm.util.matrices;

import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.map.tint.AbstractIntIntMap;
import cern.colt.map.tint.OpenIntIntHashMap;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import de.tum.bgu.msm.data.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author nkuehnel
 */
public class IndexedDoubleMatrix2D {

    private final AbstractIntIntMap externalRowId2InternalIndex;
    private final AbstractIntIntMap externalColId2InternalIndex;

    private final AbstractIntIntMap internalRowIndex2ExternalId;
    private final AbstractIntIntMap internalColIndex2ExternalId;

    private final DoubleMatrix2D delegate;

    /**
     * Creates a new id-indexed matrix for double values. Each id will be associated with a subsequent
     * array index used for the matrix. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix2D(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        delegate = new DenseDoubleMatrix2D(rows.size(), columns.size());

        List<? extends Id> sortedRows = new ArrayList<>(rows);
        List<? extends Id> sortedColumns = new ArrayList<>(columns);
        sortedRows.sort(Comparator.comparingInt(Id::getId));
        sortedColumns.sort(Comparator.comparingInt(Id::getId));


        int counter = 0;
        externalRowId2InternalIndex = new OpenIntIntHashMap(rows.size());
        internalRowIndex2ExternalId = new OpenIntIntHashMap(rows.size());
        for (Id row : sortedRows) {
            externalRowId2InternalIndex.put(row.getId(), counter);
            internalRowIndex2ExternalId.put(counter, row.getId());
            counter++;
        }

        counter = 0;
        externalColId2InternalIndex = new OpenIntIntHashMap(columns.size());
        internalColIndex2ExternalId = new OpenIntIntHashMap(columns.size());
        for (Id col : sortedColumns) {
            externalColId2InternalIndex.put(col.getId(), counter);
            internalColIndex2ExternalId.put(counter, col.getId());
            counter++;
        }
    }

    /**
     * Creates a new squared indexed matrix for double values. Each id will be associated with a subsequent
     * array index used for the matrix. The index is given by the lookup array in which each subsequent
     * entry holds the actual id. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix2D(int[] lookup) {
        delegate = new DenseDoubleMatrix2D(lookup.length, lookup.length);
        externalRowId2InternalIndex = new OpenIntIntHashMap(lookup.length);
        externalColId2InternalIndex = new OpenIntIntHashMap(lookup.length);
        internalRowIndex2ExternalId = new OpenIntIntHashMap(lookup.length);
        internalColIndex2ExternalId = new OpenIntIntHashMap(lookup.length);

        for (int i = 0; i < lookup.length; i++) {
            externalRowId2InternalIndex.put(lookup[i], i);
            externalColId2InternalIndex.put(lookup[i], i);
            internalRowIndex2ExternalId.put(i, lookup[i]);
            internalColIndex2ExternalId.put(i, lookup[i]);
        }
    }

    private IndexedDoubleMatrix2D(DoubleMatrix2D delegate,
                                  AbstractIntIntMap externalRowId2InternalIndex,
                                  AbstractIntIntMap internalRowIndex2ExternalId,
                                  AbstractIntIntMap externalColId2InternalIndex,
                                  AbstractIntIntMap internalColIndex2ExternalId) {
        this.delegate = delegate;
        this.externalRowId2InternalIndex = externalRowId2InternalIndex;
        this.internalRowIndex2ExternalId = internalRowIndex2ExternalId;
        this.externalColId2InternalIndex = externalColId2InternalIndex;
        this.internalColIndex2ExternalId = internalColIndex2ExternalId;
    }

    /**
     * Sets the double value for the given indexed ids
     *
     * @param i   id of row entry
     * @param j   id of column entry
     * @param val the value associated in the underlying indexed matrix
     */
    public void setIndexed(int i, int j, double val) {
        delegate.setQuick(externalRowId2InternalIndex.get(i), externalColId2InternalIndex.get(j), val);
    }

    /**
     * Gets the double value for the given indexed ids
     *
     * @param i id of row entry
     * @param j id of column entry
     */
    public double getIndexed(int i, int j) {
        return delegate.getQuick(externalRowId2InternalIndex.get(i), externalColId2InternalIndex.get(j));
    }


    /**
     * Constructs and returns a new <i>slice view</i> representing the columns
     * of the given row. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa.
     * @param row
     *            the row to fix.
     * @return a new slice view.
     */
    public IndexedDoubleMatrix1D viewRow(int row) {
        return new IndexedDoubleMatrix1D(delegate.viewRow(externalRowId2InternalIndex.get(row)), externalColId2InternalIndex, internalColIndex2ExternalId);
    }

    /**
     * Constructs and returns a new <i>slice view</i> representing the rows
     * of the given column. The returned view is backed by this matrix, so changes
     * in the returned view are reflected in this matrix, and vice-versa.
     * @param col
     *            the row to fix.
     * @return a new slice view.
     */
    public IndexedDoubleMatrix1D viewColumn(int col) {
        return new IndexedDoubleMatrix1D(delegate.viewColumn(externalRowId2InternalIndex.get(col)), externalRowId2InternalIndex, internalRowIndex2ExternalId);
    }

    /**
     * Returns the non-indexed double matrix with indices ranging from 0....n-1, n being the number of rows/columns.
     */
    public double[][] toNonIndexedArray() {
        return delegate.toArray();
    }

    /**
     * Creates and returns a row-based lookup array that contains the external ids at the position
     * of their internal index.
     */
    public int[] getRowLookupArray() {
        int[] lookup = new int[externalRowId2InternalIndex.size()];
        externalRowId2InternalIndex.forEachPair((externalId, internalIndex) -> {
            lookup[internalIndex] = externalId;
            return true;
        });
        return lookup;
    }

    /**
     * Creates and returns a column-based lookup array that contains the external ids at the position
     * of their internal index.
     */
    public int[] getColumnLookupArray() {
        int[] lookup = new int[externalColId2InternalIndex.size()];
        externalColId2InternalIndex.forEachPair((externalId, internalIndex) -> {
            lookup[internalIndex] = externalId;
            return true;
        });
        return lookup;
    }

    /**
     * Returns the associated id for the given internal row index.
     * @param rowIndex
     */
    public int getIdForInternalRowIndex(int rowIndex) {
        return this.internalRowIndex2ExternalId.get(rowIndex);
    }
    /**
     * Returns the associated id for the given internal column index.
     * @param colIndex
     */
    public int getIdForInternalColumnIndex(int colIndex) {
        return this.internalColIndex2ExternalId.get(colIndex);
    }

    /**
     * Returns the number of columns.
     */
    public int columns() {
        return delegate.columns();
    }

    /**
     * Returns the number of rows.
     */
    public int rows() {
        return delegate.rows();
    }

    /**
     * Sets all cells to the state specified by value.
     */
    public IndexedDoubleMatrix2D assign(double val) {
        delegate.assign(val);
        return this;
    }

    /**
     * Sets all cells to the state specified by value.
     */
    public IndexedDoubleMatrix2D assign(DoubleFunction doubleFunction) {
        delegate.assign(doubleFunction);
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
     * Returns the number of cells which is rows()*columns().
     * @return
     */
    public long size() {
        return delegate.size();
    }

    /**
     * Constructs and returns a new sub-range view that is a height x width sub matrix starting at <b>internal</b>
     * ids [row,column].
     * Operations on the returned view can only be applied to the restricted range.
     * Any attempt to access coordinates not contained in the view will throw an IndexOutOfBoundsException.
     */
    public IndexedDoubleMatrix2D viewPart(int row, int column, int height, int width) {
        final OpenIntIntHashMap newExternalRow2Internal = new OpenIntIntHashMap();
        final OpenIntIntHashMap newInternalRow2External = new OpenIntIntHashMap();

        final OpenIntIntHashMap newExternalCol2Internal = new OpenIntIntHashMap();
        final OpenIntIntHashMap newInternalCol2External = new OpenIntIntHashMap();


        for(int i = 0; i < height; i++) {
            newExternalRow2Internal.put(externalRowId2InternalIndex.get(row+i),i);
            newInternalRow2External.put(i, externalRowId2InternalIndex.get(row+i));
        }
        for(int j = 0; j < width; j++) {
            newExternalCol2Internal.put(externalRowId2InternalIndex.get(column+j), j);
            newInternalCol2External.put(j, externalRowId2InternalIndex.get(column+j));
        }

        return new IndexedDoubleMatrix2D(
                delegate.viewPart(row, column, height, width),
                newExternalRow2Internal,
                newInternalRow2External,
                newExternalCol2Internal,
                newInternalCol2External);
    }

    /**
     * Replaces all cell values of the receiver with the values of another matrix.
     * Both matrices must have the same number of rows and columns.
     */
    public IndexedDoubleMatrix2D assign(IndexedDoubleMatrix2D matrix2D) {
        delegate.assign(matrix2D.delegate);
        return this;
    }

    /**
     * Constructs and returns a deep copy of the receiver.
     * Note that the returned matrix is an independent deep copy.
     * The returned matrix is not backed by this matrix,
     * so changes in the returned matrix are not reflected in this matrix, and vice-versa.
     */
    public IndexedDoubleMatrix2D copy() {
        return new IndexedDoubleMatrix2D(
                delegate.copy(),
                externalRowId2InternalIndex.copy(),
                internalRowIndex2ExternalId.copy(),
                externalColId2InternalIndex.copy(),
                internalColIndex2ExternalId.copy());
    }

    /**
     * Assigns the result of a function to each non-zero cell; x[row,col] = function(x[row,col]).
     * Use this method for fast special-purpose iteration.
     * Parameters to function are as follows: first==row <b>internal index</b>,
     * second==column <b>internal index</b>, third==nonZeroValue
     * @param function
     */
    public IndexedDoubleMatrix2D forEachNonZero(IntIntDoubleFunction function) {
        delegate.forEachNonZero(function);
        return this;
    }
}