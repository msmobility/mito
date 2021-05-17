package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TripGenerationHurdleCoefficientReader extends AbstractCsvReader {


    private final Map<String, Double> coefficients = new HashMap<>();
    private final String activityPurpose;

    private int variableIndex;
    private int coefficientIndex;

    private final Path path;


    public TripGenerationHurdleCoefficientReader(DataSet dataSet, Purpose activityPurpose, Path path) {
        super(dataSet);
        this.activityPurpose = activityPurpose.toString();
        this.path = path;
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("variable", header);
        coefficientIndex = MitoUtil.findPositionInArray(activityPurpose, header);
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

    public Map<String, Double> readCoefficientsForThisPurpose(){
        read();
        return coefficients;
    };
}
