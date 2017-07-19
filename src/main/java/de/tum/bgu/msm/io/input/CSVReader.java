package de.tum.bgu.msm.io.input;

import com.pb.common.datafile.TableDataFileReader;
import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Nico on 17.07.2017.
 */
public abstract class CSVReader extends AbstractInputReader{

    private static Logger logger = Logger.getLogger(CSVReader.class);

    private BufferedReader reader;

    private int numberOfRecords = 0;

    public CSVReader(DataSet dataSet) {
        super(dataSet);
    }

    protected abstract void processHeader(String[] header);

    protected abstract void processRecord(String[] record);

    public void readLineByLine(String fileName, String delimiter) {
        initializeReader(fileName, delimiter);
        try {
            String record;
            while ((record = reader.readLine()) != null) {
                numberOfRecords++;
                processRecord(record.split(delimiter));
            }
        } catch (IOException e) {
            logger.error("Error parsing record number " + numberOfRecords + ": " + e.getMessage());
            System.exit(-1);
        }
        logger.info("Read " + numberOfRecords + " records.");
    }

    private void initializeReader(String fileName, String delimiter) {
        try {
            reader = new BufferedReader(new FileReader(fileName));
            processHeader(reader.readLine().split(delimiter));
        } catch (IOException e) {
            logger.error("Error initializing csv reader: " + e.getMessage());
            System.exit(-1);
        }
    }

    public TableDataSet readAsTableDataSet(String fileName) {
        // read csv file and return as TableDataSet
        File dataFile = new File(fileName);
        TableDataSet dataTable;
        boolean exists = dataFile.exists();
        if (!exists) {
            final String msg = "File not found: " + fileName;
            logger.error(msg);
//            System.exit(1);
            throw new RuntimeException(msg);
            // from the perspective of the junit testing infrastructure, a "System.exit(...)" is not a test failure ... and thus not detected.  kai, aug'16
        }
        try {
            TableDataFileReader reader = TableDataFileReader.createReader(dataFile);
            dataTable = reader.readFile(dataFile);
            reader.close();
        } catch (Exception e) {
            logger.error("Error reading file " + dataFile);
            throw new RuntimeException(e);
        }
        return dataTable;
    }
}



