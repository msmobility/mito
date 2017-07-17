package de.tum.bgu.msm.io;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import omx.OmxFile;
import omx.OmxMatrix;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

import static de.tum.bgu.msm.io.InputManager.PROPERTIES_AUTO_PEAK_SKIM;
import static de.tum.bgu.msm.io.InputManager.PROPERTIES_TRANSIT_PEAK_SKIM;

/**
 * Created by Nico on 17.07.2017.
 */
public class SkimsReader {

    private static Logger logger = Logger.getLogger(SkimsReader.class);

    private final DataSet dataSet;
    private final String highwaySkimFileName;
    private final String transitSkimFileName;

    public SkimsReader(DataSet dataSet, ResourceBundle resources) {
        this.dataSet = dataSet;
        this.highwaySkimFileName = resources.getString(PROPERTIES_AUTO_PEAK_SKIM);
        this.transitSkimFileName = resources.getString(PROPERTIES_TRANSIT_PEAK_SKIM);
    }

    public void read() {
        logger.info("  Reading skims");
        readHighwaySkims();
        readTransitSkims();
    }

    private void readHighwaySkims() {
        OmxFile hSkim = new OmxFile(highwaySkimFileName);
        hSkim.openReadOnly();
        OmxMatrix timeOmxSkimAutos = hSkim.getMatrix("HOVTime");
        dataSet.setAutoTravelTimes(MitoUtil.convertOmxToMatrix(timeOmxSkimAutos));
    }

    private void readTransitSkims() {
        OmxFile tSkim = new OmxFile(transitSkimFileName);
        tSkim.openReadOnly();
        OmxMatrix timeOmxSkimTransit = tSkim.getMatrix("CheapJrnyTime");
        dataSet.setTransitTravelTimes(MitoUtil.convertOmxToMatrix(timeOmxSkimTransit));
    }
}
