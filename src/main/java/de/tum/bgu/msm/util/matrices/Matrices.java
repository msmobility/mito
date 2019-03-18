package de.tum.bgu.msm.util.matrices;

import cern.colt.function.tdouble.IntDoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import de.tum.bgu.msm.data.Id;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class Matrices {

    private Matrices() {}

    public static DoubleMatrix2D doubleMatrix2D(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        return new IndexedDoubleMatrix2D(rows, columns);
    }

    public static IndexedDoubleMatrix2D convertOmxToDoubleMatrix2D(OmxMatrix omxMatrix, OmxLookup lookup, double factor) {
        final OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        if(!type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE) && !type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            throw new IllegalArgumentException("Provided omx matrix is not a double or float matrix but is of type: " + type.name());
        }
        final int[] dimensions = omxMatrix.getShape();

        int[] array;
        if(lookup == null) {
            array = IntStream.rangeClosed(0, dimensions[0]).toArray();
        } else {
            array = (int[]) lookup.getLookup();
        }
        final IndexedDoubleMatrix2D matrix = new IndexedDoubleMatrix2D(array);

        if(type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            for (int i = 0; i < dimensions[0]; i++) {
                int rowId = array[i];
                for (int j = 0; j < dimensions[1]; j++) {
                    int colId = array[j];
                    matrix.setIndexed(rowId, colId, dArray[i][j] * factor);
                }
            }
        } else if(type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            for (int i = 0; i < dimensions[0]; i++) {
                int rowId = array[i];
                for (int j = 0; j < dimensions[1]; j++) {
                    int colId = array[j];
                    matrix.setIndexed(rowId, colId, fArray[i][j] * factor);
                }
            }
        }
        return matrix;
    }


    public static DoubleMatrix1D doubleMatrix1D(Collection<? extends Id> identifiables) {
        int max = identifiables.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new DenseDoubleMatrix1D(max+1);
    }

    public static DoubleMatrix1D doubleMatrix1DSparse(Collection<? extends Id> identifiables) {
        int max = identifiables.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new SparseDoubleMatrix1D(max+1);
    }

    public static FloatMatrix1D floatMatrix1D(Collection<? extends Id> identifiables) {
        int max = identifiables.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new DenseFloatMatrix1D(max+1);
    }

    public static FloatMatrix1D floatMatrix1DSparse(Collection<? extends Id> identifiables) {
        int max = identifiables.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new SparseFloatMatrix1D(max+1);
    }

    public static DoubleMatrix2D applyToEachCellInMatrix(DoubleMatrix2D matrix, final IntIntDoubleFunction function) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        int c;
        if (nthreads > 1 && matrix.size() >= (long)ConcurrencyUtils.getThreadsBeginN_2D()) {
            nthreads = Math.min(nthreads, matrix.rows());
            Future<?>[] futures = new Future[nthreads];
            c = matrix.rows() / nthreads;

            for(int j = 0; j < nthreads; ++j) {
                final int firstRow = j * c;
                final int lastRow = j == nthreads - 1 ? matrix.rows() : firstRow + c;
                futures[j] = ConcurrencyUtils.submit(() -> {
                    for(int r = firstRow; r < lastRow; ++r) {
                        for(int c1 = 0; c1 < matrix.columns(); ++c1) {
                            double value = matrix.getQuick(r, c1);
                            double a = function.apply(r, c1, value);
                            if (a != value) {
                                matrix.setQuick(r, c1, a);
                            }
                        }
                    }

                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for(int r = 0; r < matrix.rows(); ++r) {
                for(c = 0; c < matrix.columns(); ++c) {
                    double value = matrix.getQuick(r, c);
                    double a = function.apply(r, c, value);
                    if (a != value) {
                        matrix.setQuick(r, c, a);
                    }
                }
            }
        }
        return matrix;
    }


    public DoubleMatrix1D applyToEachCellInVector(DoubleMatrix1D vector, IntDoubleFunction function) {
        for(int v = 0; v < vector.size(); ++v) {
            double value = vector.getQuick(v);
            double a = function.apply(v, value);
            if( a!= value) {
                vector.setQuick(v, a);
            }
        }
        return vector;
    }
}
