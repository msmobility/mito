package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TripAttractionRatesReader extends AbstractCsvReader {

    private final Map<Purpose, Integer> indexForPurpose = new EnumMap<>(Purpose.class);
    private int variableIndex;
    private List<Purpose> purposes;

    public TripAttractionRatesReader(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet);
        this.purposes = purposes;
    }

    @Override
    public void read() {
        super.read(Resources.instance.getTripAttractionRatesFilePath(), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("IndependentVariable", header);
        for(Purpose purpose: purposes) {
            indexForPurpose.put(purpose, MitoUtil.findPositionInArray(purpose.name(), header));
        }
    }

    @Override
    protected void processRecord(String[] record) {
        for(Purpose purpose: purposes) {
            AttractionCalculator.ExplanatoryVariable variable = AttractionCalculator.ExplanatoryVariable.valueOf(record[variableIndex]);
            double rate = Double.parseDouble(record[indexForPurpose.get(purpose)]);
            purpose.setTripAttractionForVariable(variable, rate);
        }
    }
}