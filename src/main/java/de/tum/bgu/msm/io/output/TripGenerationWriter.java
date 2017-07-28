package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 26/07/2017.
 */
public class TripGenerationWriter {

    private static final Logger logger = Logger.getLogger(TripGenerationWriter.class);

    public static void writeTripsByPurposeAndZone(DataSet dataSet, Map<Integer, Map<String, Float>> tripAttractionByZoneAndPurp) {
        // write number of trips by purpose and zone to output file

        String fileNameProd = generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_PRODUCTION_OUTPUT), dataSet);
        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(fileNameProd, false);
        String fileNameAttr = generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_ATTRACTION_OUTPUT), dataSet);
        PrintWriter pwAttr = MitoUtil.openFileForSequentialWriting(fileNameAttr, false);
        pwProd.print("Zone");
        pwAttr.print("Zone");
        for (String tripPurpose: dataSet.getPurposes()) {
            pwProd.print("," + tripPurpose + "P");
            pwAttr.print("," + tripPurpose + "A");
        }

        Map<Integer, Map<String, Integer>> tripProdByZoneAndPurp = new HashMap<>();

        for(Integer zoneId: dataSet.getZones().keySet()) {
            Map<String, Integer> initialValues = new HashMap<>();
            for(String purp: dataSet.getPurposes()) {
                initialValues.put(purp, 0);
            }
            tripProdByZoneAndPurp.put(zoneId, initialValues);
        }

        for (MitoTrip trip: dataSet.getTrips().values()) {
            String purp = dataSet.getPurposes()[trip.getTripPurpose()];
            int number = tripProdByZoneAndPurp.get(trip.getTripOrigin()).get(purp);
            tripProdByZoneAndPurp.get(trip.getTripOrigin()).replace(purp, (number + 1));
        }

        int totalTrips = 0;
        pwProd.println();
        pwAttr.println();
        for (int zoneId: dataSet.getZones().keySet()) {
            pwProd.print(zoneId);
            pwAttr.print(zoneId);
            for (String purp: dataSet.getPurposes()) {
                int tripProdTmp = tripProdByZoneAndPurp.get(zoneId).get(purp);
                totalTrips += tripProdTmp;
                pwProd.print("," + tripProdTmp);
                pwAttr.print("," + tripAttractionByZoneAndPurp.get(zoneId).get(purp));
            }
            pwProd.println();
            pwAttr.println();
        }
        pwProd.close();
        pwAttr.close();
        logger.info("  Wrote out " + MitoUtil.customFormat("###,###", totalTrips)
                + " aggregate trips balanced against attractions.");
    }

    private static String generateOutputFileName (String fileName, DataSet dataSet) {
        if (dataSet.getScenarioName() != null) {
            File dir = new File("scenOutput/" + dataSet.getScenarioName() + "/tripGeneration");
            if(!dir.exists()){
                boolean directoryCreated = dir.mkdir();
                if (!directoryCreated) logger.warn("Could not create directory for trip gen output: " + dir.toString());
            }
            fileName = "scenOutput/" + dataSet.getScenarioName() + "/tripGeneration/" + fileName;
        }
        return fileName;
    }
}
