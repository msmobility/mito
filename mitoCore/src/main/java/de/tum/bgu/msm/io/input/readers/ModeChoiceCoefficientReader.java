package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Mode;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.MitoUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModeChoiceCoefficientReader extends AbstractCsvReader {


    //string represents variable name, mode is the mdoe and double is the coefficient
    private final Map<Mode, Map<String, Double>> coefficients = new HashMap<>();
    private final String purpose;

    private int variableIndex;
    private Map<Mode, Integer> coefficientIndexes = new HashMap<>();

    private final Path path;


    public ModeChoiceCoefficientReader(DataSet dataSet, Purpose purpose, Path path) {
        super(dataSet);
        this.purpose = purpose.toString();
        this.path = path;
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("variable", header);
        for (Mode mode : Mode.values()){
            String colName = mode.toString().toLowerCase();
            int coefficientIndex = MitoUtil.findPositionInArray(colName, header);
            if (coefficientIndex >= 0){
                coefficientIndexes.put(mode, coefficientIndex);
            }
        }


    }

    @Override
    protected void processRecord(String[] record) {
        String name = record[variableIndex];
        for (Mode mode : coefficientIndexes.keySet()){
            int coefficientIndex = coefficientIndexes.get(mode);
            double value = Double.parseDouble(record[coefficientIndex]);
            coefficients.putIfAbsent(mode, new HashMap<>());
            coefficients.get(mode).put(name, value);
        }
    }

    @Override
    public void read() {
        super.read(path, ",");
    }

    public Map<Mode, Map<String, Double>> readCoefficientsForThisPurpose(){
        read();
        return coefficients;
    };
}
