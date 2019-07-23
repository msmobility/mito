package de.tum.bgu.msm.modules.logsumAccessibility;


import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.*;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Accessibility extends Module {

    private final static Logger logger = Logger.getLogger(Accessibility.class);
    private final double beta = 0.5;
    private final ConcurrentMap<Purpose, Map<Integer, Double>> accessibilitiesByPurpose = new ConcurrentHashMap<>();

    public Accessibility(DataSet dataSet){super(dataSet);}
    private final ConcurrentMap<Purpose, IndexedDoubleMatrix2D> matricesByPurpose = new ConcurrentHashMap<>();



    @Override
    public void run() {
        logger.info(" Calculating logsumAccessibility");
        initializeMatrices();
        accessibilityByOrigin();
        printAccessibilityValues();
    }

    private void initializeMatrices() {
        int[] array = convertArrayListToIntArray(dataSet.getZones().values());
        final IndexedDoubleMatrix2D matrix1 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix2= new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix3 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix4 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix5 = new IndexedDoubleMatrix2D(array);
        final IndexedDoubleMatrix2D matrix6 = new IndexedDoubleMatrix2D(array);
        matricesByPurpose.put(Purpose.HBW, matrix1);
        matricesByPurpose.put(Purpose.HBE, matrix2);
        matricesByPurpose.put(Purpose.HBS, matrix3);
        matricesByPurpose.put(Purpose.HBO, matrix4);
        matricesByPurpose.put(Purpose.NHBW, matrix5);
        matricesByPurpose.put(Purpose.NHBO, matrix6);
    }


    private void accessibilityByOrigin(){
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new AccessibilityByOrigin(purpose, dataSet));
        }
        executor.execute();
    }

    private void printAccessibilityValues(){
        String outputSubDirectory = "skims/";
        logger.info("  Writing logsumAccessibility file");
        String fileaa = "";
        if (Resources.INSTANCE.getBoolean(Properties.RUN_UAM)){
            if (Resources.INSTANCE.getBoolean(Properties.RUN_AVS)) {
                fileaa = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + "accessibilitiesUAM_V2.csv";
            } else {
                fileaa = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + "accessibilitiesUAMnoAVS_V2.csv";
            }
        } else {
            fileaa = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory +  "accessibilitiesBase_v2.csv";
        }
        PrintWriter pwha = MitoUtil.openFileForSequentialWriting(fileaa, false);
        pwha.println("origin");
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

    class AccessibilityByOrigin extends RandomizableConcurrentFunction<Void>{

        private final Purpose purpose;
        private final DataSet dataSet;
        private final TravelTimes travelTimes;
        private final AccessibilityJSCalculator calculator;

        AccessibilityByOrigin(Purpose purpose, DataSet dataSet){
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            if (Resources.INSTANCE.getBoolean(Properties.RUN_UAM)) {
                if (Resources.INSTANCE.getBoolean(Properties.RUN_AVS)) {
                    this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoiceLogsumsUAM")), purpose);
                } else {
                    this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                            .getResourceAsStream("ModeChoiceLogsumsUAMnoAV")), purpose);
                }
            } else {
                this.calculator = new AccessibilityJSCalculator(new InputStreamReader(this.getClass()
                        .getResourceAsStream("ModeChoiceLogsumsUAMnoAVnoUAM")), purpose);
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
            if (Resources.INSTANCE.getBoolean(Properties.RUN_UAM)) {
                final double travelDistanceUAM = dataSet.getTravelDistancesUAM().getTravelDistance(originId,
                        destinationId);
                return calculator.calculateProbabilities(origin, destination, travelTimes, travelDistanceAuto, travelDistanceNMT,
                        travelDistanceUAM, dataSet.getPeakHour());
            } else {
                return calculator.calculateProbabilities(origin, destination, travelTimes, travelDistanceAuto, travelDistanceNMT, dataSet.getPeakHour());
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
