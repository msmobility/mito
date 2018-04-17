package de.tum.bgu.msm.io.input.readers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import de.tum.bgu.msm.io.input.CSVReader;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericCsvReader extends CSVReader {

    private final String fileName;
    private GenericCsvTable table;
    private int currentRow = 0;

    public GenericCsvReader(String fileName) {
        super(null);
        this.fileName = fileName;
    }

    @Override
    protected void processHeader(String[] header) {
        table.createHeader(header);
    }

    @Override
    protected void processRecord(String[] record) {
        for(int i = 0; i < record.length; i++) {
            table.put(currentRow, i, record[i]);
        }
        currentRow++;
    }

    @Override
    public void read() {
        table = new GenericCsvTable();
        super.read(fileName, ",");
    }

    public GenericCsvTable getTable() {
        return table;
    }

    public static class GenericCsvTable {

        private final Table<Integer, Integer, String> table;
        private List<String> header;


        private GenericCsvTable() {
            this.table = HashBasedTable.create();
        }

        private void createHeader(String[] header) {
            this.header = Lists.newArrayList(header);
        }

        private void put(int row, int column, String s) {
            this.table.put(row, column, s);
        }

        public String getString(int row, int column) {
            return table.get(row, column);
        }

        public int getInt(int row, int column) {
            return Integer.parseInt(table.get(row, column));
        }

        public double getDouble(int row, int column) {
            return Double.parseDouble(table.get(row, column));
        }

        public int getColumnIndexOf(String string) {
            return header.indexOf(string);
        }

        /**
         * returns the column as list of string entries in ascending row order.
         * @param string the name of the column
         * @return
         */
        public List<String> getStringColumn(String string) {
            return getStringColumn(getColumnIndexOf(string));
        }

        /**
         * returns the column as list of string entries in ascending row order.
         * @param index the index of the column
         * @return
         */
        public List<String> getStringColumn(int index) {
            return table.column(index).entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(entry -> entry.getValue())
                    .collect(Collectors.toList());
        }

        /**
         * returns the column as list of double entries in ascending row order.
         * @param string the name of the column
         * @return
         */
        public List<Double> getDoubleColumn(String string) {
            return getDoubleColumn(getColumnIndexOf(string));
        }

        /**
         * returns the column as list of double entries in ascending row order.
         * @param index the index of the column
         * @return
         */
        public List<Double> getDoubleColumn(int index) {
            return table.column(index).entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(entry -> Double.parseDouble(entry.getValue()))
                    .collect(Collectors.toList());
        }
        /**
         * returns the column as list of float entries in ascending row order.
         * @param string the name of the column
         * @return
         */
        public List<Float> getFloatColumn(String string) {
            return getFloatColumn(getColumnIndexOf(string));
        }

        /**
         * returns the column as list of float entries in ascending row order.
         * @param index the index of the column
         * @return
         */
        public List<Float> getFloatColumn(int index) {
            return table.column(index).entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(entry -> Float.parseFloat(entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
