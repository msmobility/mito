package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
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

    public static void writeTripsByPurposeAndZone(DataSet dataSet, String scenarioName) {
        // write number of trips by purpose and zone to output file

        String fileNameProd = generateOutputFileName(Resources.instance.getString(Properties.TRIP_PRODUCTION_OUTPUT), dataSet.getYear(), scenarioName);
        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(fileNameProd, false);
        String fileNameAttr = generateOutputFileName(Resources.instance.getString(Properties.TRIP_ATTRACTION_OUTPUT), dataSet.getYear(), scenarioName);
        PrintWriter pwAttr = MitoUtil.openFileForSequentialWriting(fileNameAttr, false);
        pwProd.print("MitoZone");
        pwAttr.print("MitoZone");
        for (Purpose purpose: Purpose.values()) {
            pwProd.print("," + purpose + "P");
            pwAttr.print("," + purpose + "A");
        }

        Map<Integer, Map<Purpose, Integer>> tripProdByZoneAndPurp = new HashMap<>(dataSet.getZones().size());

        for(Integer zoneId: dataSet.getZones().keySet()) {
            Map<Purpose, Integer> initialValues = new HashMap<>(Purpose.values().length);
            for(Purpose purpose: Purpose.values()) {
                initialValues.put(purpose, 0);
            }
            tripProdByZoneAndPurp.put(zoneId, initialValues);
        }

        for (MitoTrip trip: dataSet.getTrips().values()) {
            if(trip.getTripOrigin() != null && trip.getTripDestination() != null) {
                Purpose purpose = trip.getTripPurpose();
                int number = tripProdByZoneAndPurp.get(trip.getTripOrigin().getZoneId()).get(purpose);
                tripProdByZoneAndPurp.get(trip.getTripOrigin().getZoneId()).replace(purpose, (number + 1));
            }
        }

        int totalTrips = 0;
        pwProd.println();
        pwAttr.println();
        for (MitoZone zone: dataSet.getZones().values()) {
            final int zoneId = zone.getId();
            pwProd.print(zoneId);
            pwAttr.print(zoneId);
            for (Purpose purpose: Purpose.values()) {
                int tripProdTmp = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                totalTrips += tripProdTmp;
                pwProd.print("," + tripProdTmp);
                pwAttr.print("," + zone.getTripAttraction(purpose));
            }
            pwProd.println();
            pwAttr.println();
        }
        pwProd.close();
        pwAttr.close();
        logger.info("  Wrote out " + MitoUtil.customFormat("###,###", totalTrips)
                + " aggregate trips balanced against attractions.");
    }

    private static String generateOutputFileName (String fileName, int year, String scenarioName) {
        if (scenarioName != null) {
            File dir = new File("./scenOutput/" + scenarioName + "/" +  year +  "/tripGeneration");
            if(!dir.exists()){
                boolean directoryCreated = dir.mkdir();
                if (!directoryCreated) {
                    logger.warn("Could not create directory for trip gen output: " + dir.toString());
                }
            }
            fileName = "./scenOutput/" + scenarioName + "/" +  year  +  "/tripGeneration/" + fileName;
        }
        return fileName;
    }
}
