package de.tum.bgu.msm.io.output;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import de.tum.bgu.msm.MitoModel;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoTrip;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by matthewokrah on 12/03/2018.
 */
public class SummarizeDataToVisualize {
    private static final Logger logger = Logger.getLogger(SummarizeDataToVisualize.class);

    private static PrintWriter spatialResultWriter;
    private static PrintWriter spatialResultWriterFinal;

    public static void resultFileSpatial(String action) {
        resultFileSpatial(action,true);
    }

    public static void resultFileSpatial(String action, Boolean writeFinal) {
        // handle summary file
        switch (action) {
            case "open":
                String directory = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/scenOutput";
                MitoUtil.createDirectoryIfNotExistingYet(directory);
                String resultFileName = Resources.INSTANCE.getString(Properties.SPATIAL_RESULT_FILE_NAME);
                spatialResultWriter = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +
                        ".csv", true);
                spatialResultWriterFinal = MitoUtil.openFileForSequentialWriting(directory + "/" + resultFileName +"_final.csv", false);
                break;
            case "close":
                spatialResultWriter.close();
                spatialResultWriterFinal.close();
                break;
            default:
                spatialResultWriter.println(action);
                if(writeFinal)spatialResultWriterFinal.println(action);
                break;
        }
    }

    public static void summarizeSpatially (DataSet dataSet) {
        // write out results by zone
        resultFileSpatial("open");

        String hd = "Zone";
        for (Purpose purpose: Purpose.values()){
            hd = hd.concat("," + purpose + "P");
        }

        resultFileSpatial(hd);

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

        for (int zoneId: dataSet.getZones().keySet()){
            String txt = String.valueOf(zoneId);
            for (Purpose purpose: Purpose.values()){
                int tripsProduced = tripProdByZoneAndPurp.get(zoneId).get(purpose);
                txt = txt.concat("," + tripsProduced);
            }
            resultFileSpatial(txt);
        }
        resultFileSpatial("close");
    }

/*
    public static void summarizeSpatially (int year, SiloModelContainer modelContainer, SiloDataContainer dataContainer) {
        // write out results by zone

        String hd = "Year" + year + ",autoAccessibility,transitAccessibility,population,households,hhInc_<" + Properties.get().main.incomeBrackets[0];
        for (int inc = 0; inc < Properties.get().main.incomeBrackets.length; inc++) hd = hd.concat(",hhInc_>" + Properties.get().main.incomeBrackets[inc]);
        resultFileSpatial(hd + ",dd_SFD,dd_SFA,dd_MF234,dd_MF5plus,dd_MH,availLand,avePrice,jobs,shWhite,shBlack,shHispanic,shOther");


        int[][] dds = new int[DwellingType.values().length + 1][dataContainer.getGeoData().getHighestZonalId() + 1];
        int[] prices = new int[dataContainer.getGeoData().getHighestZonalId() + 1];
        int[] jobs = new int[dataContainer.getGeoData().getHighestZonalId() + 1];
        int[] hhs = new int[dataContainer.getGeoData().getHighestZonalId() + 1];
        int[][] hhInc = new int[Properties.get().main.incomeBrackets.length + 1][dataContainer.getGeoData().getHighestZonalId() + 1];
        DoubleMatrix1D pop = getPopulationByZone(dataContainer.getGeoData());
        for (Household hh: Household.getHouseholds()) {
            int zone = Dwelling.getDwellingFromId(hh.getDwellingId()).getZone();
            int incGroup = HouseholdDataManager.getIncomeCategoryForIncome(hh.getHhIncome());
            hhInc[incGroup - 1][zone]++;
            hhs[zone] ++;
        }
        for (Dwelling dd: Dwelling.getDwellings()) {
            dds[dd.getType().ordinal()][dd.getZone()]++;
            prices[dd.getZone()] += dd.getPrice();
        }
        for (Job jj: Job.getJobs()) {
            jobs[jj.getZone()]++;
        }


        for (int taz: dataContainer.getGeoData().getZones().keySet()) {
            float avePrice = -1;
            int ddThisZone = 0;
            for (DwellingType dt: DwellingType.values()) ddThisZone += dds[dt.ordinal()][taz];
            if (ddThisZone > 0) avePrice = prices[taz] / ddThisZone;
            double autoAcc = modelContainer.getAcc().getAutoAccessibilityForZone(taz);
            double transitAcc = modelContainer.getAcc().getTransitAccessibilityForZone(taz);
            double availLand = dataContainer.getRealEstateData().getAvailableLandForConstruction(taz);
//            Formatter f = new Formatter();
//            f.format("%d,%f,%f,%d,%d,%d,%f,%f,%d", taz, autoAcc, transitAcc, pop[taz], hhs[taz], dds[taz], availLand, avePrice, jobs[taz]);
            String txt = taz + "," + autoAcc + "," + transitAcc + "," + pop.getQuick(taz) + "," + hhs[taz];
            for (int inc = 0; inc <= Properties.get().main.incomeBrackets.length; inc++) txt = txt.concat("," + hhInc[inc][taz]);
            for (DwellingType dt: DwellingType.values()) txt = txt.concat("," + dds[dt.ordinal()][taz]);
            txt = txt.concat("," + availLand + "," + avePrice + "," + jobs[taz] + "," +
                    // todo: make the summary application specific, Munich does not work with these race categories
                    "0,0,0,0");
//                    modelContainer.getMove().getZonalRacialShare(taz, Race.white) + "," +
//                    modelContainer.getMove().getZonalRacialShare(taz, Race.black) + "," +
//                    modelContainer.getMove().getZonalRacialShare(taz, Race.hispanic) + "," +
//                    modelContainer.getMove().getZonalRacialShare(taz, Race.other));
//            String txt = f.toString();
            resultFileSpatial(txt);
        }
    }*/
}
