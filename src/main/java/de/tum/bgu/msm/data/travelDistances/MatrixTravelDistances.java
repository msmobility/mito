package de.tum.bgu.msm.data.travelDistances;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;

public class MatrixTravelDistances implements TravelDistances{

    private final DoubleMatrix2D matrix;

    public MatrixTravelDistances(DoubleMatrix2D matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelDistance(int origin, int destination) {
        return matrix.getQuick(origin, destination);
    }

    public void printOutDistanceSkim(String filePath, String matrixName) {
        OmxMatrixWriter.createOmxSkimMatrix(matrix,
                filePath,
                matrixName);

    }
}
