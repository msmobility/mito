package de.tum.bgu.msm.io.input;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class OMXReader extends AbstractInputReader{

    protected OMXReader(DataSet dataSet) {
        super(dataSet);
    }

    protected FloatMatrix2D readAndConvertToFloatMatrix(String fileName, String matrixName, String lookupName) {
        OmxFile omx = new OmxFile(fileName);
        omx.openReadOnly();
        return Matrices.convertOmxToFloatMatrix2D(omx.getMatrix(matrixName), omx.getLookup(lookupName));
    }

    protected DoubleMatrix2D readAndConvertToDoubleMatrix(String fileName, String matrixName, String lookupName) {
        OmxFile omx = new OmxFile(fileName);
        omx.openReadOnly();
        return Matrices.convertOmxToDoubleMatrix2D(omx.getMatrix(matrixName), omx.getLookup(lookupName));
    }
}
