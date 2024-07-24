package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.modules.tripGeneration.ExplanatoryVariable;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TripAttractionRatesReader extends AbstractCsvReader {

    private final Map<Purpose, Integer> indexForPurpose = new EnumMap<>(Purpose.class);
    private int variableIndex;
    private Purpose purpose;

    public TripAttractionRatesReader(DataSet dataSet, Purpose purpose) {
        super(dataSet);
        this.purpose = purpose;
    }

    @Override
    public void read() {
        super.read(Resources.instance.getTripAttractionRatesFilePath(), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("IndependentVariable", header);

        indexForPurpose.put(purpose, MitoUtil.findPositionInArray(purpose.name(), header));

    }

    @Override
    protected void processRecord(String[] record) {
            ExplanatoryVariable variable = ExplanatoryVariable.valueOf(record[variableIndex]);
            double rate = Double.parseDouble(record[indexForPurpose.get(purpose)]);
            purpose.setTripAttractionForVariable(variable, rate);
    }
}