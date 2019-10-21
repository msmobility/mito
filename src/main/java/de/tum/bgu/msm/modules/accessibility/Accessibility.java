package de.tum.bgu.msm.modules.accessibility;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.tum.bgu.msm.resources.Properties.AUTONOMOUS_VEHICLE_CHOICE;
import static de.tum.bgu.msm.resources.Properties.UAM_CHOICE;

public class Accessibility extends Module {

    private final static Logger logger = Logger.getLogger(Accessibility.class);
    private final double beta = 0.5;
    private final ConcurrentMap<Purpose, Map<Integer, Double>> accessibilitiesByPurpose = new ConcurrentHashMap<>();

    public Accessibility(DataSet dataSet){super(dataSet);}
    private final ConcurrentMap<Purpose, IndexedDoubleMatrix2D> matricesByPurpose = new ConcurrentHashMap<>();

    private final boolean includeAV = Resources.INSTANCE.getBoolean(AUTONOMOUS_VEHICLE_CHOICE, false);
    private final boolean includeUAM = Resources.INSTANCE.getBoolean(UAM_CHOICE, true);


    @Override
    public void run() {
        logger.info(" Calculating logsumAccessibility");
        initializeMatrices();
        accessibilityByOrigin();
    }


    public void print(String scenarioName){
        logger.info("  Writing logsumAccessibility file");
        String fileName = "scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/accessibility/accessibility";
        String fileNameScenario = "";

        if (includeUAM){
            if (includeAV) {
                fileNameScenario = fileName + "UAMyesAVS.csv";
            } else {
                fileNameScenario = fileName + "noUAMyesAVS.csv";
            }
        } else {
            fileNameScenario = fileName +  "Base.csv";
        }
        PrintWriter pwha = MitoUtil.openFileForSequentialWriting(fileNameScenario, false);
        pwha.print("origin");
        for (Purpose purpose : Purpose.values()) {
            pwha.print(",");
            pwha.print(purpose);
        }
        pwha.println("");

        for (MitoZone origin : dataSet.getZones().values()) {
            pwha.print(origin.getId());
            for (Purpose purpose : Purpose.values()) {
                pwha.print(",");
                pwha.print(accessibilitiesByPurpose.get(purpose).get(origin.getId()));
            }
            pwha.println("");
        }
        pwha.close();
    }

    private void initializeMatrices() {
        int[] array = convertArrayListToIntArray(dataSet.getZones().values());
        final IndexedDoubleMatrix2D matrix1 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix2= new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix3 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix4 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix5 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix6 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix7 = new IndexedDoubleMatrix2D(array);
        matricesByPurpose.put(Purpose.HBW, matrix1);
        matricesByPurpose.put(Purpose.HBE, matrix2);
        matricesByPurpose.put(Purpose.HBS, matrix3);
        matricesByPurpose.put(Purpose.HBO, matrix4);
        matricesByPurpose.put(Purpose.NHBW, matrix5);
        matricesByPurpose.put(Purpose.NHBO, matrix6);
        matricesByPurpose.put(Purpose.AIRPORT, matrix7);
    }


    private void accessibilityByOrigin(){
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new AccessibilityByOrigin(purpose, dataSet, includeAV, includeUAM));
        }
        executor.execute();
    }


    class AccessibilityByOrigin extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final TravelTimes travelTimes;
        private final AccessAndEgressVariables accessAndEgressVariables;
        private final AccessibilityJSCalculator calculator;

        AccessibilityByOrigin(Purpose purpose, DataSet dataSet, boolean includeAV, boolean includeUAM){
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.accessAndEgressVariables = dataSet.getAccessAndEgressVariables();
            if (includeAV) {
                this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("AccessibilityAV")), purpose);
            } else if (includeUAM) {
                this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("AccessibilityUAMIncremental")), purpose);
            } else{
                this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("Accessibility")), purpose);
            }
        }

        public Void call(){
            try {
                int counter = 0;
                Map<Integer, Double> accessibilityForPurpose = new LinkedHashMap<>();
                for (MitoZone origin : dataSet.getZones().values()){
                    double accessibility = 0;
                    for (MitoZone destination : dataSet.getZones().values()){
                        double logsum = Math.log(calculateSumExpUtilities(origin, destination));
                        accessibility += destination.getTripAttraction(purpose) * Math.exp(logsum * beta);
                        IndexedDoubleMatrix2D matrix = matricesByPurpose.get(purpose);
                        matrix.setIndexed(origin.getId(), destination.getId(), logsum);
                        matricesByPurpose.put(purpose, matrix);
                    }
                    counter++;
                    accessibilityForPurpose.put(origin.getId(), accessibility);
                    if (LongMath.isPowerOfTwo(counter)) {
                        logger.info("Logsum calculation of purpose "  + purpose + " completed at " +  counter + " zones.");
                    }
                }
                accessibilitiesByPurpose.put(purpose, accessibilityForPurpose);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }


        double calculateSumExpUtilities(MitoZone origin, MitoZone destination){
            final int originId = origin.getZoneId();
            final int destinationId = destination.getZoneId();
            final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originId,
                    destinationId);
            final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originId,
                    destinationId);
            if (Resources.INSTANCE.getBoolean(UAM_CHOICE, true)){
                final double flyingDistanceUAM_km = dataSet.getFlyingDistanceUAM().getTravelDistance(originId,
                        destinationId);
                final double uamFare_eurkm = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_COST_KM));
                final double uamFare_eurbase = Double.parseDouble(Resources.INSTANCE.getString(Properties.UAM_COST_BASE));

                //todo car costs hard coded to 0.07!!!!!
                final double uamCost_eur = uamFare_eurbase + flyingDistanceUAM_km * uamFare_eurkm +
                        dataSet.getAccessAndEgressVariables().
                                getAccessVariable(origin, destination, "uam", AccessAndEgressVariables.AccessVariable.ACCESS_DIST_KM) * 0.07 +
                        dataSet.getAccessAndEgressVariables().
                                getAccessVariable(origin, destination, "uam", AccessAndEgressVariables.AccessVariable.EGRESS_DIST_KM) * 0.07;

                MitoTrip dummyTrip = new MitoTrip(-1, purpose);
                dummyTrip.setTripOrigin(origin);
                dummyTrip.setTripDestination(destination);
                dummyTrip.setDepartureInMinutes(8 * 60);
                //we calculate accessibilities for the peak hour of UAM, as done for car with the peak hour skim matrix

                double processingTime_min = dataSet.getTotalHandlingTimes().
                        getWaitingTime(dummyTrip, origin, destination, Mode.uam.toString());


                return calculator.calculateProbabilitiesUAM(origin, destination, travelTimes, accessAndEgressVariables, travelDistanceAuto,
                        travelDistanceNMT, uamCost_eur, dataSet.getPeakHour(),processingTime_min,uamFare_eurkm);
            }else {
                return calculator.calculateProbabilities(origin, destination, travelTimes, travelDistanceAuto,
                        travelDistanceNMT, dataSet.getPeakHour());
            }
        }

    }

    public static int[] convertArrayListToIntArray (Collection<MitoZone> zones) {
        int[] list = new int[zones.size()];
        int i = 0;
        for (MitoZone zone : zones){
            list[i] = zone.getId();
            i++;
        }
        return list;
    }
}
