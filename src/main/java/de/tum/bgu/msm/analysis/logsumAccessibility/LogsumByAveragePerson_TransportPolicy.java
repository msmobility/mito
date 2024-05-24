package de.tum.bgu.msm.analysis.logsumAccessibility;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.modules.modeChoice.ModeChoiceCalibrationData;
import de.tum.bgu.msm.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LogsumByAveragePerson_TransportPolicy {

    private final static Logger logger = LogManager.getLogger(LogsumByAveragePerson_TransportPolicy.class);

    private static final Map<Integer, Map<Integer,  Double>> logsumTable = new HashMap<>();
    private static final Map<Integer, Boolean> evForbidden = new HashMap<>();

    private final DataSet dataSet = new DataSet();
    private Purpose[] givenPurposes = {Purpose.HBW, Purpose.HBE, Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO, Purpose.HBR};
    private Map<Mode, Map<String, Double>> coef;
    private ModeChoiceCalibrationData calibrationData;

    public static void main(String[] args) throws FileNotFoundException {
        LogsumByAveragePerson_TransportPolicy logsumByAveragePerson = new LogsumByAveragePerson_TransportPolicy();

        logsumByAveragePerson.setup();
        logsumByAveragePerson.load();
        logsumByAveragePerson.run();
    }

    public void setup() throws FileNotFoundException {
        Resources.initializeResources("C:\\models\\MITO\\mitoMunich\\mito.properties");
    }

    public void load() {
        new ZonesReader(dataSet).read();
        dataSet.setTravelTimes(new SkimTravelTimes());
        new OmxSkimsReader(dataSet).read();
        new EconomicStatusReader(dataSet).read();
        new CalibrationDataReader(dataSet).read();
        new CalibrationRegionMapReader(dataSet).read();
        for (MitoZone zoneId : dataSet.getZones().values()) {
            evForbidden.put(zoneId.getId(), false);
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader("////nas.ads.mwn.de/tubv/mob/projects/2021/DatSim/abit/transportpolicypaper/lowEmissionZones.csv"));
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 0) {
                    int zoneId = Integer.parseInt(values[0].trim());
                    evForbidden.put(zoneId, true);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void run() {
        boolean[] hasEVOptions = {true,false};
        //boolean[] hasEVOptions = {false};
        for (Purpose purpose : givenPurposes) {
            for(boolean hasEV : hasEVOptions) {
                this.coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
                this.calibrationData = dataSet.getModeChoiceCalibrationData();
                logger.info("Running logsum-based accessibility calculation for purpose: " + purpose);

                logsumTable.clear();
                for (MitoZone origin : dataSet.getZones().values()) {
                    logsumTable.put(origin.getId(), new HashMap<>());
                    for (MitoZone destination : dataSet.getZones().values()) {
                        logsumTable.get(origin.getId()).put(destination.getId(), 0.0);
                    }
                }

                calculateLogsums(logsumTable, purpose, hasEV);

                logger.info("Finished synthetic logsum calculator");
                writeLogsumAccessibility(logsumTable, purpose, hasEV);
            }
        }
    }

    private void calculateLogsums(Map<Integer, Map<Integer, Double>> logsumTable, Purpose purpose, boolean hasEV) {
        for(MitoZone origin : dataSet.getZones().values()){
            for (MitoZone destination : dataSet.getZones().values()) {
                logsumTable.get(origin.getZoneId())
                        .put(destination.getZoneId(), calculateLogsumsByZone(origin.getZoneId(), destination.getZoneId(), hasEV, purpose));
            }
        }
    }

    private double calculateLogsumsByZone(int origin, int destination, Boolean hasEV, Purpose purpose) {

        LogsumCalculator2 calculator = new LogsumCalculator2(this.coef, this.calibrationData);

        MitoZone originZone = dataSet.getZones().get(origin);
        MitoZone destinationZone = dataSet.getZones().get(destination);

        TravelTimes travelTimes = dataSet.getTravelTimes();
        final double travelDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originZone.getId(), destinationZone.getId());
        final double travelDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originZone.getId(), destinationZone.getId());

        return calculator.calculateLogsumByZone(purpose, hasEV, originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, 0,
                evForbidden);
    }


    private void writeLogsumAccessibility(Map<Integer, Map<Integer, Double>> logsumTable, Purpose purpose, boolean hasEV){
        PrintWriter pw;
        try {
            String evStatus = hasEV ? "hasEV" : "noEV";
            String fileName = "C:/models/MITO/mitoMunich/skims/logsum/" + purpose + "_" + evStatus + ".csv";
            pw = new PrintWriter(fileName);
            pw.println("origin,destination,logsum");
            for(MitoZone origin : dataSet.getZones().values()){
                for (MitoZone destination : dataSet.getZones().values()) {
                    pw.println(origin.getId() + "," + destination.getId() + "," + logsumTable.get(origin.getId()).get(destination.getId()));
                }
            }
/*            String fileName = "C:/models/MITO/mitoMunich/skims/logsum/lowEmissionScenario/" + purpose + ".csv";
            pw = new PrintWriter(fileName);
            pw.println("origin,destination,logsum");
            for(MitoZone origin : dataSet.getZones().values()){
                for (MitoZone destination : dataSet.getZones().values()) {
                    pw.println(origin.getId() + "," + destination.getId() + "," + logsumTable.get(origin.getId()).get(destination.getId()));
                }
            }*/
            pw.close();
            logger.info("Output written to " + fileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
