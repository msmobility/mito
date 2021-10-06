package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.modules.tripDistribution.ExplanatoryVariable;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class OpenAttractorDataReader extends AbstractCsvReader {
    private final static Logger logger = Logger.getLogger(OpenAttractorDataReader.class);
    private int zoneIndex;
    private int zoneNotFoundCounter = 0;
    private int twitterCount;
    private int twitterCountDensity;

    public OpenAttractorDataReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MitoUtil.findPositionInArray("zoneID", header);
        twitterCount = MitoUtil.findPositionInArray(ExplanatoryVariable.numberOfTweets, header);
        twitterCountDensity = MitoUtil.findPositionInArray(ExplanatoryVariable.numberOfTweetsPerArea, header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneID = Integer.parseInt(record[zoneIndex]);
        MitoZone zone = dataSet.getZones().get(zoneID);
        double numberOfTweets = Double.parseDouble(record[twitterCount]);
        double numberOfTweetsPerArea = Double.parseDouble(record[twitterCountDensity]);
        Map<String, Double> openDataExplanatoryVariables = new HashMap<>();
        openDataExplanatoryVariables.put(ExplanatoryVariable.numberOfTweets, numberOfTweets);
        openDataExplanatoryVariables.put(ExplanatoryVariable.numberOfTweetsPerArea, numberOfTweetsPerArea);

        if(zone != null) {
            zone.setOpenDataExplanatoryVariables(openDataExplanatoryVariables);
            logger.info(zone.getId() + " " + zone.getOpenDataExplanatoryVariables().get(ExplanatoryVariable.numberOfTweets));
            logger.info(zone.getId() + " " + zone.getOpenDataExplanatoryVariables().get(ExplanatoryVariable.numberOfTweetsPerArea));
        } else {
            zoneNotFoundCounter++;
        }
    }

    @Override
    public void read() {
        super.read(Resources.instance.getOpenDataTwitterFilePath(),",");
        logger.warn(zoneNotFoundCounter + " zones were not present");
    }
}
