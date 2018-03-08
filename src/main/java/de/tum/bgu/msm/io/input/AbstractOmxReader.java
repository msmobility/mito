package de.tum.bgu.msm.io.input;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class AbstractOmxReader extends AbstractInputReader{

    protected AbstractOmxReader(DataSet dataSet) {
        super(dataSet);
    }

    protected DoubleMatrix2D readAndConvertToDoubleMatrix(String fileName, String matrixName) {
        OmxFile omx = new OmxFile(fileName);
        omx.openReadOnly();
        DoubleMatrix2D matrix = Matrices.convertOmxToDoubleMatrix2D(omx.getMatrix(matrixName));
        omx.close();
        return matrix;
    }
}
