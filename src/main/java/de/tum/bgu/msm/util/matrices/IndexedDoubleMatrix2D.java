package de.tum.bgu.msm.util.matrices;

import cern.colt.map.tint.OpenIntIntHashMap;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import de.tum.bgu.msm.data.Id;

import java.util.Collection;

/**
 * @author nkuehnel
 */
public class IndexedDoubleMatrix2D extends DenseDoubleMatrix2D {

    private final OpenIntIntHashMap rowIndex;
    private final OpenIntIntHashMap colIndex;

    /**
     * Creates a new id-indexed matrix for double values. Each id will be associated with a subsequent
     * array index used for the matrix. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix2D(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        super(rows.size(), columns.size());

        int counter = 0;
        rowIndex = new OpenIntIntHashMap(rows.size());
        for (Id row : rows) {
            rowIndex.put(row.getId(), counter++);
        }

        counter = 0;
        colIndex = new OpenIntIntHashMap(columns.size());
        for (Id row : columns) {
            colIndex.put(row.getId(), counter++);
        }
    }

    /**
     * Creates a new indexed matrix for double values. Each id will be associated with a subsequent
     * array index used for the matrix. The index is given by the lookup array in which each subsequent
     * entry holds the actual id. This allows objects to start from high ids (e.g. zone ids in the
     * range of 500000-510000). While the id values can be in the complete positive integer range, the
     * total number of different ids is still limited.
     */
    public IndexedDoubleMatrix2D(int[] lookup) {
        super(lookup.length, lookup.length);

        rowIndex = new OpenIntIntHashMap(lookup.length);
        colIndex = new OpenIntIntHashMap(lookup.length);
        for (int i = 0; i < lookup.length; i++) {
            rowIndex.put(lookup[i], i);
            colIndex.put(lookup[i], i);
        }
    }

    /**
     * Sets the double value for the given indexed ids
     *
     * @param i   id of row entry
     * @param j   id of column entry
     * @param val the value associated in the underlying indexed matrix
     */
    public void setIndexed(int i, int j, double val) {
        setQuick(rowIndex.get(i), colIndex.get(j), val);
    }

    /**
     * Gets the double value for the given indexed ids
     *
     * @param i id of row entry
     * @param j id of column entry
     */
    public double getIndexed(int i, int j) {
        return getQuick(rowIndex.get(i), colIndex.get(j));
    }
}