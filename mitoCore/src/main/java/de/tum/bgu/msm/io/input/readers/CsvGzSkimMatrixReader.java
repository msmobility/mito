package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.Id;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class CsvGzSkimMatrixReader {
    private int positionOrigin;

    private int positionDestination;
    private int positionvalue;

    private static final Logger logger = Logger.getLogger(CsvGzSkimMatrixReader.class);

    private BufferedReader reader;

    private int numberOfRecords = 0;

    public IndexedDoubleMatrix2D readAndConvertToDoubleMatrix2D(String fileName, double factor, Collection<? extends Id> zoneLookup){
        matrix = new IndexedDoubleMatrix2D(zoneLookup, zoneLookup);
        //the csv files do not include the cells without values. They need to set up to a very high number
        matrix.assign(Double.MAX_VALUE);
        read(fileName, ",", factor);
        return matrix;
    }


    private void processHeader(String[] header) {
        positionOrigin = MitoUtil.findPositionInArray("FROM", header);
        positionDestination = MitoUtil.findPositionInArray("TO", header);
        positionvalue = MitoUtil.findPositionInArray("VALUE", header);
    }

    private void processRecord(String[] record, double factor) {
        int origin = Integer.parseInt(record[positionOrigin]);
        int destination = Integer.parseInt(record[positionDestination]);
        double time = Double.parseDouble(record[positionvalue]) * factor;

        matrix.setIndexed(origin, destination, time);

    }

    private IndexedDoubleMatrix2D matrix;

    private void read(String filePath, String delimiter, double factor) {
        initializeReader(filePath, delimiter);
        try {
            String record;
            while ((record = reader.readLine()) != null) {
                numberOfRecords++;
                processRecord(record.split(delimiter), factor);
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


    private void initializeReader(String filePath, String delimiter) {
        try {

            GZIPInputStream in = new GZIPInputStream(new FileInputStream(filePath));
            reader = new BufferedReader(new InputStreamReader(in));
            processHeader(reader.readLine().split(delimiter));
        } catch (IOException e) {
            logger.error("Error initializing csv.gz reader: " + e.getMessage(), e);
        }
    }

}
