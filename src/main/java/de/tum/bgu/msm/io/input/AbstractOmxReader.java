package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxLookup;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class AbstractOmxReader extends AbstractInputReader{

    private final static Logger logger = Logger.getLogger(AbstractOmxReader.class);

    protected AbstractOmxReader(DataSet dataSet) {
        super(dataSet);
    }

    protected IndexedDoubleMatrix2D readAndConvertToDoubleMatrix(String fileName, String matrixName, double factor) {
        OmxFile omx = new OmxFile(fileName);
        omx.openReadOnly();
        final Set<String> lookupNames = omx.getLookupNames();
        OmxLookup lookup = null;
        if(!lookupNames.isEmpty()) {
            final Iterator<String> iterator = omx.getLookupNames().iterator();
            final String next = iterator.next();
            lookup = omx.getLookup(next);
            if(!lookup.getOmxJavaType().equals(OmxHdf5Datatype.OmxJavaType.INT)) {
                throw new IllegalArgumentException("Provided omx matrix lookup " +
                        "is not of type int but of type: " + lookup.getOmxJavaType().name() +
                        " please use int.");
            }
            if(iterator.hasNext()) {
                logger.warn("More than one lookup was provided. Will use the first one (name: " + next + ")");
            }
        }
        IndexedDoubleMatrix2D matrix = Matrices.convertOmxToDoubleMatrix2D(omx.getMatrix(matrixName), lookup, factor);
        omx.close();
        return matrix;
    }
}
