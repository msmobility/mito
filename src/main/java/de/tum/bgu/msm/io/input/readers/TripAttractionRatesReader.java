package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.CSVReader;

/**
 * Created by Nico on 19.07.2017.
 */
public class TripAttractionRatesReader extends CSVReader{

    public TripAttractionRatesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        dataSet.setTripAttractionRates(super.readAsTableDataSet(Properties.getString(Properties.TRIP_ATTRACTION_RATES)));
    }

    @Override
    protected void processHeader(String[] header) {

    }

    @Override
    protected void processRecord(String[] record) {

    }
}
