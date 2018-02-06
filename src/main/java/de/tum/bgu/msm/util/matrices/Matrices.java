package de.tum.bgu.msm.util.matrices;

import cern.colt.function.tdouble.IntDoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.data.Id;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Future;

public class Matrices {

    private Matrices() {}

    public static FloatMatrix2D floatMatrix2D(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        int rowMax = rows.stream().max(Comparator.comparing(Id::getId)).get().getId();
        int colMax = columns.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new DenseFloatMatrix2D(rowMax+1, colMax+1);
    }

    public static DoubleMatrix2D doubleMatrix2D(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        int rowMax = rows.stream().max(Comparator.comparing(Id::getId)).get().getId();
        int colMax = columns.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new DenseDoubleMatrix2D(rowMax+1, colMax+1);
    }

    public static FloatMatrix2D floatMatrix2DSparse(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        int rowMax = rows.stream().max(Comparator.comparing(Id::getId)).get().getId();
        int colMax = columns.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new SparseFloatMatrix2D(rowMax+1, colMax+1);
    }

    public static DoubleMatrix2D doubleMatrix2DSparse(Collection<? extends Id> rows, Collection<? extends Id> columns) {
        int rowMax = rows.stream().max(Comparator.comparing(Id::getId)).get().getId();
        int colMax = columns.stream().max(Comparator.comparing(Id::getId)).get().getId();
        return new SparseDoubleMatrix2D(rowMax+1, colMax+1);
    }

    public static DoubleMatrix2D convertOmxToDoubleMatrix2D(OmxMatrix omxMatrix, OmxLookup lookup) {
        final OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        if(!type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE) && !type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            throw new IllegalArgumentException("Provided omx matrix is not a double or float matrix but is of type: " + type.name());
        }
        final int[] intLookup = (int[]) lookup.getLookup();
        final int[] dimensions = omxMatrix.getShape();
        final DoubleMatrix2D matrix = new DenseDoubleMatrix2D(dimensions[0]+1, dimensions[1]+1);

        if(type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    matrix.set(intLookup[i], intLookup[j], dArray[i][j]);
                }
            }
        } else if(type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    matrix.set(intLookup[i], intLookup[j], fArray[i][j]);
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
