package de.tum.bgu.msm.data.travelDistances;

import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;

public class MatrixTravelDistances implements TravelDistances {

    private final IndexedDoubleMatrix2D matrix;

    public MatrixTravelDistances(IndexedDoubleMatrix2D matrix) {
        this.matrix = matrix;
    }

    @Override
    public double getTravelDistance(int origin, int destination) {
        return matrix.getIndexed(origin, destination);
    }

    public void printOutDistanceSkim(String filePath, String matrixName) {
        OmxMatrixWriter.createOmxSkimMatrix(matrix,
                filePath,
                matrixName);

    }
}
