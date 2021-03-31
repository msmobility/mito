package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.data.DataSet;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * @author Nico,Carlos
 */
public abstract class AbstractCsvGzReader extends AbstractInputReader {

    private static final Logger logger = Logger.getLogger(AbstractCsvGzReader.class);

    private BufferedReader reader;

    private int numberOfRecords = 0;

    protected AbstractCsvGzReader(DataSet dataSet) {
        super(dataSet);
    }

    protected abstract void processHeader(String[] header);

    protected abstract void processRecord(String[] record);

    public void read(Path filePath, String delimiter) {
        initializeReader(filePath, delimiter);
        try {
            String record;
            while ((record = reader.readLine()) != null) {
                numberOfRecords++;
                processRecord(record.split(delimiter));
            }
        } catch (IOException e) {
            logger.error("Error parsing record number " + numberOfRecords + ": " + e.getMessage(), e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info(this.getClass().getSimpleName() + ": Read " + numberOfRecords + " records.");
    }


    private void initializeReader(Path filePath, String delimiter) {
        try {

            GZIPInputStream in = new GZIPInputStream(new FileInputStream(String.valueOf(filePath)));
            reader = new BufferedReader(new InputStreamReader(in));
            processHeader(reader.readLine().split(delimiter));
        } catch (IOException e) {
            logger.error("Error initializing csv reader: " + e.getMessage(), e);
        }
    }
}



