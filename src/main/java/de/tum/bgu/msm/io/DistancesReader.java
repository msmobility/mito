package de.tum.bgu.msm.io;

import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import omx.OmxFile;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

import static de.tum.bgu.msm.io.InputManager.PROPERTIES_DISTANCE_SKIM;

/**
 * Created by Nico on 17.07.2017.
 */
public class DistancesReader {


    private static Logger logger = Logger.getLogger(DistancesReader.class);

    private final DataSet dataSet;
    private final String fileName;
    private Matrix distanceMatrix;

    public DistancesReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.fileName =  ResourceUtil.getProperty(resources, PROPERTIES_DISTANCE_SKIM);
    }

    public void read() {
            readMatrix();
            scaleValues();
            dataSet.setDistanceMatrix(distanceMatrix);
    }

    private void readMatrix() {
        logger.info("   Starting to read distances OMX matrix");
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
