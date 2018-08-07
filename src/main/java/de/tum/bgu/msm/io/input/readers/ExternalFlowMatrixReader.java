package de.tum.bgu.msm.io.input.readers;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.modules.externalFlows.ExternalFlowType;
import de.tum.bgu.msm.modules.externalFlows.ExternalFlowZone;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExternalFlowMatrixReader {

    private static Logger logger = Logger.getLogger(ExternalFlowMatrixReader.class);
    private final Map<Integer, ExternalFlowZone> zones;

    //hard coded for the specific format of matrices
    private int startOrigId = 3;
    private int endOrigId = 11  ;
    private int startDestId = 14 ;
    private int endDestId = 21;
    private int startFlow = 23;

    private Map<ExternalFlowType, String> matrixFileNames;



    public ExternalFlowMatrixReader(DataSet dataSet, Map<Integer, ExternalFlowZone> zones) {
        String fileNamePkw = Resources.INSTANCE.getString(Properties.EXTERNAL_MATRIX_PKW);
        String fileNameGV = Resources.INSTANCE.getString(Properties.EXTERNAL_MATRIX_GV);
        String fileNamePkwPWV = Resources.INSTANCE.getString(Properties.EXTERNAL_MATRIX_PKW_PWV);
        String fileNameSZM = Resources.INSTANCE.getString(Properties.EXTERNAL_MATRIX_SZM);

        matrixFileNames = new HashMap<>();
        matrixFileNames.put(ExternalFlowType.GV_andere, fileNameGV);
        matrixFileNames.put(ExternalFlowType.Pkw, fileNamePkw);
        matrixFileNames.put(ExternalFlowType.Pkw_PWV, fileNamePkwPWV);
        matrixFileNames.put(ExternalFlowType.SZM, fileNameSZM);

        this.zones = zones;
    }

    public HashBasedTable<Integer, Integer, Float> read(ExternalFlowType type){
        HashBasedTable<Integer, Integer, Float> matrix = HashBasedTable.create();
        String fileName = matrixFileNames.get(type);

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String line;
            boolean hasOdPairs = false;
            while ((line = br.readLine()) != null) {
                if (hasOdPairs) {
                    if (line.contains("Netzobjektnamen")) {
                        hasOdPairs = false;
                    } else {
                        //readOdPairs()
                        int originId = Integer.parseInt(line.substring(startOrigId, endOrigId).replaceAll(" ", ""));
                        int destId = Integer.parseInt(line.substring(startDestId, endDestId).replaceAll(" ", ""));
                        float flow = Float.parseFloat(line.substring(startFlow, line.length()).replaceAll(" ", ""));

                        validate(originId, destId);
                        matrix.put(originId, destId, flow);

                    }
                } else if (line.contains("18.01.18")) {
                    hasOdPairs = true;
                }
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Created a matrix of external flows for vehicle type " + type.toString());
        return matrix;

    }

    private void validate(int originId, int destId) {
        if (!zones.keySet().contains(originId) || !zones.keySet().contains(destId)){
            logger.error("Od pair could not found in the zone list");
            throw new RuntimeException();
        }


    }


}
