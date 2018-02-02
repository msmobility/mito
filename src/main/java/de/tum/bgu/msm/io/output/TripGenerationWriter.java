package de.tum.bgu.msm.io.output;

import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.Purpose;
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

    public static void writeTripsByPurposeAndZone(DataSet dataSet) {
        // write number of trips by purpose and zone to output file

        String fileNameProd = generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_PRODUCTION_OUTPUT), dataSet);
        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(fileNameProd, false);
        String fileNameAttr = generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_ATTRACTION_OUTPUT), dataSet);
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
                int number = tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).get(purpose);
                tripProdByZoneAndPurp.get(trip.getTripOrigin().getId()).replace(purpose, (number + 1));
            }
        }

        int totalTrips = 0;
        pwProd.println();
        pwAttr.println();
        for (int zoneId: dataSet.getZones().keySet()) {
            pwProd.print(zoneId);
            pwAttr.print(zoneId);
            for (Purpose purpose: Purpose.values()) {
                int tripProdTmp = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                totalTrips += tripProdTmp;
                pwProd.print("," + tripProdTmp);
                pwAttr.print("," + tripProdTmp);
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
        if (MitoModel.getScenarioName() != null) {
            File dir = new File("scenOutput/" + MitoModel.getScenarioName() + "/tripGeneration");
            if(!dir.exists()){
                boolean directoryCreated = dir.mkdir();
                if (!directoryCreated) {
                    logger.warn("Could not create directory for trip gen output: " + dir.toString());
                }
            }
            fileName = "scenOutput/" + MitoModel.getScenarioName() + "/tripGeneration/" + fileName;
        }
        return fileName;
    }
}
