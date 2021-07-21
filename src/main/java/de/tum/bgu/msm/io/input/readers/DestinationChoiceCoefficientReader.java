package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.MitoUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DestinationChoiceCoefficientReader extends AbstractCsvReader {


    //string represents variable name, mode is the mdoe and double is the coefficient
    private final Map<String, Double> coefficients = new HashMap<>();
    private final String purpose;

    private int variableIndex;
    private int purposeIndex;
    private int valueIndex;
    private Map<Mode, Integer> coefficientIndexes = new HashMap<>();

    private final Path path;


    public DestinationChoiceCoefficientReader(DataSet dataSet, Purpose purpose, Path path) {
        super(dataSet);
        this.purpose = purpose.toString();
        this.path = path;
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("variable", header);
        purposeIndex = MitoUtil.findPositionInArray("purpose", header);
        valueIndex = MitoUtil.findPositionInArray("value", header);
    }


    @Override
    protected void processRecord(String[] record) {
        String variableName = record[variableIndex];
        String purposeName = record[purposeIndex];
        if (purposeName.equalsIgnoreCase(purpose.toString())){
            double value = Double.parseDouble(record[valueIndex]);
            coefficients.put(variableName, value);
        }
    }

    @Override
    public void read() {
        super.read(path, ",");
    }

    public Map<String, Double> readCoefficientsForThisPurpose() {
        read();
        return coefficients;
    }

    ;
}
