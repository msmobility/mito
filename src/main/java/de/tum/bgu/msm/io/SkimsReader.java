package de.tum.bgu.msm.io;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import omx.OmxFile;
import omx.OmxMatrix;
import org.apache.log4j.Logger;

/**
 * Created by Nico on 17.07.2017.
 */
public class SkimsReader extends AbstractInputReader {

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
        OmxFile hSkim = new OmxFile(Properties.getString(Properties.AUTO_PEAK_SKIM));
        hSkim.openReadOnly();
        OmxMatrix timeOmxSkimAutos = hSkim.getMatrix("HOVTime");
        dataSet.setAutoTravelTimes(MitoUtil.convertOmxToMatrix(timeOmxSkimAutos));
    }

    private void readTransitSkims() {
        OmxFile tSkim = new OmxFile(Properties.getString(Properties.TRANSIT_PEAK_SKIM));
        tSkim.openReadOnly();
        OmxMatrix timeOmxSkimTransit = tSkim.getMatrix("CheapJrnyTime");
        dataSet.setTransitTravelTimes(MitoUtil.convertOmxToMatrix(timeOmxSkimTransit));
    }
}
