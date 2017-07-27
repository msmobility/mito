package de.tum.bgu.msm.io.input.readers;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.OMXReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class DistancesReader extends OMXReader {

    private static final Logger logger = Logger.getLogger(DistancesReader.class);

    private Matrix distanceMatrix;

    public DistancesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        String fileName = Resources.INSTANCE.getString(Properties.DISTANCE_SKIM);

        logger.info("   Starting to read distances OMX matrix");
        distanceMatrix = super.readAndConvertToMatrix(fileName, "HOVTime");
        logger.info("   Read distances OMX matrix");

        scaleValues();
        dataSet.setDistanceMatrix(distanceMatrix);
    }

    private void scaleValues() {
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
