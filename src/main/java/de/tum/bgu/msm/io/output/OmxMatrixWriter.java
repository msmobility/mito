package de.tum.bgu.msm.io.output;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import omx.OmxFile;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class OmxMatrixWriter {


    public static void createOmxFile(String omxFilePath, int numberOfZones) {

        try (OmxFile omxFile = new OmxFile(omxFilePath)) {
            int dim0 = numberOfZones -1;
            int dim1 = dim0;
            int[] shape = {dim0, dim1};
            int lookup1NA = -1;
            int[] lookup1Data = new int[dim0];
            Set<Integer> lookup1Used = new HashSet<>();
            for (int i = 0; i < lookup1Data.length; i++) {
                int lookup = i + 1;
                lookup1Data[i] = lookup1Used.add(lookup) ? lookup : lookup1NA;
            }
            OmxLookup.OmxIntLookup lookup1 = new OmxLookup.OmxIntLookup("lookup", lookup1Data, lookup1NA);
            omxFile.openNew(shape);
            omxFile.addLookup(lookup1);
            omxFile.save();

        }
    }


    public static void createOmxSkimMatrix(DoubleMatrix2D matrix, String omxFilePath, String omxMatrixName) {
        try (OmxFile omxFile = new OmxFile(omxFilePath)) {
            omxFile.openReadWrite();
            double mat1NA = -1;
            double[][] matrixWithZeros = matrix.toArray();
            double[][] matrixWithoutZeros = new double[matrix.rows()-1][matrix.columns()-1];
            for (int i = 1; i < matrix.columns(); i++){
                for (int j = 1; j < matrix.columns(); j++) {
                    matrixWithoutZeros[i-1][j-1] = matrixWithZeros[i][j];
                }

            }
            OmxMatrix.OmxDoubleMatrix mat1 = new OmxMatrix.OmxDoubleMatrix(omxMatrixName, matrixWithoutZeros, mat1NA);
            mat1.setAttribute(OmxConstants.OmxNames.OMX_DATASET_TITLE_KEY.getKey(), "skim_matrix");
            omxFile.addMatrix(mat1);
            omxFile.save();
            System.out.println(omxFile.summary());
            omxFile.close();
            System.out.println(omxMatrixName + "matrix written");
        }
    }

}
