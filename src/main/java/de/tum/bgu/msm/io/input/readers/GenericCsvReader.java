package de.tum.bgu.msm.io.input.readers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import de.tum.bgu.msm.io.input.AbstractCsvReader;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that can be used for reading arbitrary csv files. Provides a {@link GenericCsvTable} that holds the data.
 * Make sure to call {@link #read()} first.
 */
public final class GenericCsvReader extends AbstractCsvReader {

    private final Path filePath;
    private final GenericCsvTable table = new GenericCsvTable();
    private int currentRow = 0;

    public GenericCsvReader(Path filePath) {
        super(null);
        this.filePath = filePath;
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
        //return null;
    }

    @Override
    public void read() {
        super.read(filePath, ",");
    }

    public GenericCsvTable getTable() {
        return table;
    }

    /**
     * Class that stores data read by {@link GenericCsvReader}. Uses a {@link Table} internally to store Strings that
     * can be queried and converted by the provided methods.
     */
    public final static class GenericCsvTable {

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

        public boolean containsColumn(String column) {
            return header.contains(column);
        }

        /**
         * returns the value at given row and column indexes as a string.
         * @param row the row index
         * @param column the column index
         * @return the value that is stored at the given position
         */
        public String getString(int row, int column) {
            return table.get(row, column);
        }

        /**
         * returns the value at given row and column indexes as an integer.
         * @param row the row index
         * @param column the column index
         * @return the value that is stored at the given position, parsed as integer.
         * @throws NumberFormatException if the String cannot be parsed
         */
        public int getInt(int row, int column) throws NumberFormatException {
            return Integer.parseInt(table.get(row, column));
        }

        public int getInt(int row, String columnAsString) throws NumberFormatException {
            int column = getColumnIndexOf(columnAsString);
            return Integer.parseInt(table.get(row, column));
        }

        /**
         * returns the value at given row and column indexes as a double.
         * @param row the row index
         * @param column the column index
         * @return the value that is stored at the given position, parsed as double.
         * @throws NumberFormatException if the String cannot be parsed
         */
        public double getDouble(int row, int column) throws NumberFormatException {
            return Double.parseDouble(table.get(row, column));
        }

        /**
         * returns the index of the given column's header
         * @param string the header of the column
         * @return
         */
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

        public int getRowCount () {
            return table.column(0).entrySet().size();
        }
    }
}
