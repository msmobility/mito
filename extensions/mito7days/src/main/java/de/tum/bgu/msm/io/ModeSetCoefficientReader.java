package de.tum.bgu.msm.io;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Day;
import de.tum.bgu.msm.data.Id;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.util.MitoUtil;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

//TODO: generalize coefficient readers into one
public class ModeSetCoefficientReader extends AbstractCsvReader {


    private final Map<String, Double> coefficients = new HashMap<>();
    private final String id;

    private int variableIndex;
    private int coefficientIndex;

    private final Path path;


    public ModeSetCoefficientReader(DataSet dataSet, Id id, Path path) {
        super(dataSet);
        this.id = id.toString();
        this.path = path;
        read();
    }

    public ModeSetCoefficientReader(DataSet dataSet, String id, Path path) {
        super(dataSet);
        this.id = id;
        this.path = path;
        read();
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("variable", header);
        coefficientIndex = MitoUtil.findPositionInArray(id, header);
    }

    @Override
    protected void processRecord(String[] record) {
        String name = record[variableIndex];
        double value = Double.parseDouble(record[coefficientIndex]);

        coefficients.put(name, value);

    }

    @Override
    public void read() {
        super.read(path, ",");
    }

    public Map<String, Double> readCoefficients(){
        read();
        return coefficients;
    };
}
