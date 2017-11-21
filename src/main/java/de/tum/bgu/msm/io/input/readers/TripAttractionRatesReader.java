package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;
import java.util.Map;

public class TripAttractionRatesReader extends CSVReader{

    private final Map<Purpose, Integer> indexForPurpose = new EnumMap<>(Purpose.class);
    private int variableIndex;

    public TripAttractionRatesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.TRIP_ATTRACTION_RATES), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("IndependentVariable", header);
        for(Purpose purpose: Purpose.values()) {
            indexForPurpose.put(purpose, MitoUtil.findPositionInArray(purpose.name(), header));
        }
    }

    @Override
    protected void processRecord(String[] record) {
        for(Purpose purpose: Purpose.values()) {
            AttractionCalculator.ExplanatoryVariable variable = AttractionCalculator.ExplanatoryVariable.valueOf(record[variableIndex]);
            double rate = Double.parseDouble(record[indexForPurpose.get(purpose)]);
            purpose.putTripAttractionForVariable(variable, rate);
        }
    }
}