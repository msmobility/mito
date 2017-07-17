package de.tum.bgu.msm.io;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import omx.OmxFile;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class DistancesReader extends AbstractInputReader {

    private static Logger logger = Logger.getLogger(DistancesReader.class);

    private Matrix distanceMatrix;

    public DistancesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
            readMatrix();
            scaleValues();
            dataSet.setDistanceMatrix(distanceMatrix);
    }

    private void readMatrix() {
        logger.info("   Starting to read distances OMX matrix");
        String fileName = Properties.getString(Properties.DISTANCE_SKIM);
        OmxFile travelTimeOmx = new OmxFile(fileName);
        travelTimeOmx.openReadOnly();
        distanceMatrix = MitoUtil.convertOmxToMatrix(travelTimeOmx.getMatrix("HOVTime"));
        logger.info("   Read distances OMX matrix");
    }

    private void scaleValues (){
        for (int i = 1; i <= distanceMatrix.getRowCount(); i++) {
            for (int j = 1; j <= distanceMatrix.getColumnCount(); j++) {
                if (i == j) {
                    distanceMatrix.setValueAt(i, j, 50 / 1000);
                } else {
                    distanceMatrix.setValueAt(i, j, distanceMatrix.getValueAt(i, j) / 1000);
                }
            }
        }
    }
}
