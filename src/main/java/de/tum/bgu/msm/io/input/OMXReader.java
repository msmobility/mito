package de.tum.bgu.msm.io.input;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.util.MitoUtil;
import omx.OmxFile;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class OMXReader extends AbstractInputReader{

    protected OMXReader(DataSet dataSet) {
        super(dataSet);
    }

    protected Matrix readAndConvertToMatrix(String fileName, String matrixName, String lookupName) {
        OmxFile omx = new OmxFile(fileName);
        omx.openReadOnly();
        return MitoUtil.convertOmxToMatrix(omx.getMatrix(matrixName), omx.getLookup(lookupName));
    }
}
