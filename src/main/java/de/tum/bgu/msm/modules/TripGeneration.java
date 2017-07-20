package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.TravelDemandGenerator;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.io.output.SummarizeData;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator;
import de.tum.bgu.msm.modules.tripGeneration.TripBalancer;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs trip generation for the Transport in Microsimulation Orchestrator (MITO)
 * @author Rolf Moeckel
 * Created on Sep 18, 2016 in Munich, Germany
 *
 */

public class TripGeneration extends Module{

    private static Logger logger = Logger.getLogger(TravelDemandGenerator.class);

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        generateTrips();
    }

    private void generateTrips () {
        // Run trip generation model

        logger.info("  Started microscopic trip generation model.");
        RawTripGenerator rawTripGenerator = new RawTripGenerator(dataSet);
        rawTripGenerator.run();

        AttractionCalculator calculator = new AttractionCalculator(dataSet);
        Map<Integer, Map<String, Float>> tripAttr =  calculator.run();

        TripBalancer tripBalancer = new TripBalancer(dataSet, tripAttr);
        tripBalancer.run();

        writeTripSummary(tripAttr);
        SummarizeData.writeOutSyntheticPopulationWithTrips(dataSet);
        logger.info("  Completed microscopic trip generation model.");
    }


    // Scaling trips is challenging, because individual households would have to add or give up trips. The trip frequency distribution of the survey would need to be scaled up or down.
//    private void scaleTripGeneration() {
//        // scale trip generation to account for underreporting in survey
//
//        logger.info("  Scaling trip production and attraction to account for underreporting in survey");
//        String[] token = ResourceUtil.getArray(rb, "trip.gen.scaler.purpose");
//        double[] scaler = ResourceUtil.getDoubleArray(rb, "trip.gen.scaler.factor");
//        HashMap<String, Double[]> scale = new HashMap<>();
//        for (tripPurposes purp: tripPurposes.values()) scale.put(purp.toString(), new Double[]{0d,0d,0d,0d,0d});
//        for (int i = 0; i < token.length; i++) {
//            String[] tokenParts = token[i].split(Pattern.quote("."));
//            if (tokenParts.length == 2) {
//                // purpose is split by income categories
//                Double[] values = scale.get(tokenParts[0]);
//                values[Integer.parseInt(tokenParts[1]) - 1] = scaler[i];
//            } else {
//                // purpose is not split by income categories
//                Double[] values = scale.get(token[i]);
//                for (int inc = 0; inc < values.length; inc++) values[inc] = scaler[i];
//            }
//        }
//        for (int purp = 0; purp < tripPurposes.values().length; purp++) {
//            Double[] scalingFactors = scale.get(tripPurposes.values()[purp].toString());
//            for (int mstmInc = 1; mstmInc <= 5; mstmInc++) {
//                if (scalingFactors[mstmInc-1] == 1) continue;
//                for (int zone: geoData.getZones()) {
//                    tripProd[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
//                    tripAttr[zone][purp][mstmInc] *= scalingFactors[mstmInc-1];
//                }
//            }
//        }
//    }


    private void writeTripSummary(Map<Integer, Map<String, Float>> tripAttractionByZoneAndPurp) {
        // write number of trips by purpose and zone to output file

        String fileNameProd = MitoUtil.generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_PRODUCTION_OUTPUT));
        PrintWriter pwProd = MitoUtil.openFileForSequentialWriting(fileNameProd, false);
        String fileNameAttr = MitoUtil.generateOutputFileName(Resources.INSTANCE.getString(Properties.TRIP_ATTRACTION_OUTPUT));
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
}


