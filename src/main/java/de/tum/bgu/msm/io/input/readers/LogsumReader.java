package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelDistances.TravelDistances;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import de.tum.bgu.msm.data.Purpose;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LogsumReader extends AbstractCsvReader {
    private static final Logger logger = Logger.getLogger(LogsumReader.class);

    private int posOrigin = -1;
    private int posDestination = -1;
    private int posLogsum = -1;

    private Map<Purpose, IndexedDoubleMatrix2D> logsumMatrices = new HashMap<>();
    private final EnumMap<Purpose, TravelDistances> logsumMatricesByPurpose_EV = new EnumMap<Purpose, TravelDistances>(Purpose.class);
    private final EnumMap<Purpose, TravelDistances> logsumMatricesByPurpose_NoEV = new EnumMap<Purpose, TravelDistances>(Purpose.class);

    private List<Purpose> purposes = Arrays.asList(Purpose.HBW, Purpose.HBE, Purpose.HBS, Purpose.HBO, Purpose.NHBW, Purpose.NHBO, Purpose.HBR);


    public LogsumReader(DataSet dataSet) {
        super(dataSet);
        int[] zoneIds = convertArrayListToIntArray(dataSet.getZones().values());
        for (Purpose purpose : purposes) {
            logsumMatrices.put(purpose, new IndexedDoubleMatrix2D(zoneIds));
        }

    }

    public void read() {
        for (Purpose purpose : purposes) {
            String fileNameEV = "F:/models/mitoAggregate/mitoMunich/skims/logsum/" + purpose + "_hasEV" + ".csv";
            Path filePath = Paths.get(fileNameEV);
            super.read(filePath, ",");
            logger.info("Reading logsum for EV hh from csv file" + fileNameEV);

            logsumMatricesByPurpose_EV.put(purpose, new MatrixTravelDistances(logsumMatrices.get(purpose)));
        }
        dataSet.setLogsumByPurpose_EV(logsumMatricesByPurpose_EV);

        logsumMatrices.clear();
        for (Purpose purpose : purposes) {
            logsumMatrices.put(purpose, new IndexedDoubleMatrix2D(dataSet.getZones().values().stream().mapToInt(MitoZone::getId).toArray()));
        }

        for (Purpose purpose : purposes) {

            // For low emission scenario run, uncomment following to determine file path based on the purpose

/*
            String fileName = "";
            if (Arrays.asList(Purpose.HBE, Purpose.HBW, Purpose.HBS, Purpose.HBR, Purpose.HBO).contains(purpose)) {
                fileName = "F:/models/mitoAggregate/mitoMunich/skims/logsum/lowEmissionScenario/" + purpose + ".csv";
            } else if (Arrays.asList(Purpose.NHBW, Purpose.NHBO).contains(purpose)) {
                fileName = "F:/models/mitoAggregate/mitoMunich/skims/logsum/" + purpose + "_noEV" + ".csv";
            } else {
                continue; // Skip if purpose is not handled (if any other purposes are present)
            }*/

            //For base scenario, uncomment followign and comment above code segment to read base logsums
            //fileName = "F:/models/mitoAggregate/mitoMunich/skims/logsum/" + purpose + "_noEV" + ".csv";

            String fileName = "F:/models/mitoAggregate/mitoMunich/skims/logsum/" + purpose + "_noEV" + ".csv";
            Path filePath = Paths.get(fileName);
            super.read(filePath, ",");
            logger.info("Reading logsum for non EV hh from csv file" + fileName);

            logsumMatricesByPurpose_NoEV.put(purpose, new MatrixTravelDistances(logsumMatrices.get(purpose)));
        }
        dataSet.setLogsumByPurpose_NoEV(logsumMatricesByPurpose_NoEV);

    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posOrigin = headerList.indexOf("origin");
        posDestination = headerList.indexOf("destination");
        posLogsum = headerList.indexOf("logsum");
    }

    @Override
    public void processRecord(String[] record) {
        final int origin = Integer.parseInt(record[posOrigin]);
        final int destination = Integer.parseInt(record[posDestination]);
        final double logsum = Double.parseDouble(record[posLogsum]);

        for (Purpose purpose : purposes) {
            IndexedDoubleMatrix2D matrix = logsumMatrices.get(purpose);
            matrix.setIndexed(origin, destination, logsum);
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
