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
public class SkimsReader extends OMXReader {

    private static Logger logger = Logger.getLogger(SkimsReader.class);

    public SkimsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading skims");
        readHighwaySkims();
        readTransitSkims();
    }

    private void readHighwaySkims() {
        Matrix timeSkimAutos = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "HOVTime");
        dataSet.setAutoTravelTimes(timeSkimAutos);
    }

    private void readTransitSkims() {
        Matrix timeSkimTransit = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRANSIT_PEAK_SKIM), "CheapJrnyTime");
        dataSet.setTransitTravelTimes(timeSkimTransit);
    }
}
