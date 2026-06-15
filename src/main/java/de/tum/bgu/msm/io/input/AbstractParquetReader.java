package de.tum.bgu.msm.io.input;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Id;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class AbstractParquetReader extends AbstractInputReader{

    private static final Logger logger = Logger.getLogger(AbstractParquetReader.class);

    protected AbstractParquetReader(DataSet dataSet) {
        super(dataSet);
    }

    public static IndexedDoubleMatrix2D readAndConvertToDoubleMatrix(
            String fileName,
            String originColumn,
            String destinationColumn,
            String valueColumn,
            double factor,
            Collection<? extends Id> zoneLookup
    ) {

        IndexedDoubleMatrix2D matrix =
                new IndexedDoubleMatrix2D(zoneLookup, zoneLookup);

        org.apache.hadoop.fs.Path path =
                new org.apache.hadoop.fs.Path(fileName);

        Configuration conf = new Configuration();

        try (ParquetReader<GenericRecord> reader =
                     AvroParquetReader.<GenericRecord>builder(path)
                             .withConf(conf)
                             .build()) {

            GenericRecord record;
            int numberOfRecords = 0;

            while ((record = reader.read()) != null) {

                int origin = getInt(record, originColumn);
                int destination = getInt(record, destinationColumn);
                double value = getDouble(record, valueColumn) * factor;

                matrix.setIndexed(origin, destination, value);

                numberOfRecords++;
            }

            logger.info("Read " + numberOfRecords +
                    " parquet skim records from " + fileName);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading parquet skim file: " + fileName, e
            );
        }

        return matrix;
    }

    private static int getInt(GenericRecord record, String columnName) {

        Object value = record.get(columnName);

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }

        throw new IllegalArgumentException(
                "Column " + columnName +
                        " cannot be converted to int. Value: " + value
        );
    }

    private static double getDouble(GenericRecord record, String columnName) {

        Object value = record.get(columnName);

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }

        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }

        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }

        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }

        throw new IllegalArgumentException(
                "Column " + columnName +
                        " cannot be converted to double. Value: " + value
        );
    }
}
