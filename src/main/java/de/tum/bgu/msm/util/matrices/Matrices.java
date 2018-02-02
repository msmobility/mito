package de.tum.bgu.msm.util.matrices;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.data.Id;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;

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
        if(!type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            throw new IllegalArgumentException("Provided omx matrix is not a double matrix but is of type: " + type.name());
        }
        final int[] intLookup = (int[]) lookup.getLookup();
        final int[] dimensions = omxMatrix.getShape();
        final DoubleMatrix2D matrix = new DenseDoubleMatrix2D(dimensions[0]+1, dimensions[1]+1);

        double[][] dArray = (double[][]) omxMatrix.getData();
        for (int i = 0; i < dimensions[0]; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                matrix.set(intLookup[i], intLookup[j], dArray[i][j]);
            }
        }
        return matrix;
    }

    public static FloatMatrix2D convertOmxToFloatMatrix2D(OmxMatrix omxMatrix, OmxLookup lookup) {
        final OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        if(!type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            throw new IllegalArgumentException("Provided omx matrix is not a double matrix but is of type: " + type.name());
        }

        final int[] intLookup = (int[]) lookup.getLookup();
        final int[] dimensions = omxMatrix.getShape();
        final FloatMatrix2D matrix = new DenseFloatMatrix2D(dimensions[0]+1, dimensions[1]+1);

        float[][] fArray = (float[][]) omxMatrix.getData();
        for (int i = 0; i < dimensions[0]; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                matrix.set(intLookup[i], intLookup[j], fArray[i][j]);
            }
        }
        return matrix;
    }
}
