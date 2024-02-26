package de.tum.bgu.msm.util.matrices;

import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;

import java.util.stream.IntStream;

public class Matrices {

    private Matrices() {}

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
}
