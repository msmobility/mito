package de.tum.bgu.msm.io.input.readers;

import com.pb.common.matrix.Matrix;
import de.tum.bgu.msm.data.travelTimes.MatrixTravelTimes;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.OMXReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

public class SkimsReader extends OMXReader {

    private static final Logger logger = Logger.getLogger(SkimsReader.class);

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
        Matrix timeSkimAutos = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.AUTO_PEAK_SKIM), "mat1", "lookup1");
        dataSet.addTravelTimeForMode("car", new MatrixTravelTimes(timeSkimAutos));
    }

    private void readTransitSkims() {
        Matrix timeSkimTransit = super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.TRANSIT_PEAK_SKIM), "CheapJrnyTime", "lookup1");
        dataSet.addTravelTimeForMode("pt", new MatrixTravelTimes(timeSkimTransit));
    }
}
