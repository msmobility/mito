package de.tum.bgu.msm.io.input.readers;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TimeOfDayDistributionsReader extends AbstractCsvReader {

    private final EnumMap<Purpose, DoubleMatrix1D> arrivalTimeCumProbByPurpose = new EnumMap<>(Purpose.class);
    private final EnumMap<Purpose, DoubleMatrix1D> durationCumProbByPurpose = new EnumMap<>(Purpose.class);
    private final EnumMap<Purpose, DoubleMatrix1D> departureTimeCumProbByPurpose = new EnumMap<>(Purpose.class);
    private int minuteIndex;
    private final Map<Purpose, Integer> arrivalIndexForPurpose = new EnumMap<>(Purpose.class);
    private final Map<Purpose, Integer> durationIndexForPurpose = new EnumMap<>(Purpose.class);
    private final Map<Purpose, Integer> departureIndexForPurpose = new EnumMap<>(Purpose.class);
    private int airport_arrival_index;
    private int airport_departure_index;
    private List<Purpose> purposes;

    public TimeOfDayDistributionsReader(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet);
        for (Purpose purpose : purposes) {
            arrivalTimeCumProbByPurpose.put(purpose, new DenseDoubleMatrix1D(24 * 60 + 1));
            durationCumProbByPurpose.put(purpose, new DenseDoubleMatrix1D(24 * 60 + 1));
            departureTimeCumProbByPurpose.put(purpose, new DenseDoubleMatrix1D(24 * 60 + 1));
        }
        this.purposes = purposes;
    }

    @Override
    protected void processHeader(String[] header) {
        minuteIndex = MitoUtil.findPositionInArray("minute", header);
        for(Purpose purpose: purposes) {
            arrivalIndexForPurpose.put(purpose, MitoUtil.findPositionInArray("arrival_" + purpose.name(), header));
            durationIndexForPurpose.put(purpose, MitoUtil.findPositionInArray("duration_" + purpose.name(), header));
            departureIndexForPurpose.put(purpose,MitoUtil.findPositionInArray("departure_" + purpose.name(), header));
        }
        airport_arrival_index = MitoUtil.findPositionInArray("arrival_airport", header);
        airport_departure_index = MitoUtil.findPositionInArray("departure_airport", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int minute = Integer.parseInt(record[minuteIndex]);
        for(Purpose purpose: purposes) {
            departureTimeCumProbByPurpose.get(purpose).setQuick(minute, departureIndexForPurpose.get(purpose)==-1? 0. : Double.parseDouble(record[departureIndexForPurpose.get(purpose)]));
            arrivalTimeCumProbByPurpose.get(purpose).setQuick(minute, arrivalIndexForPurpose.get(purpose)==-1? 0. : Double.parseDouble(record[arrivalIndexForPurpose.get(purpose)]));
            durationCumProbByPurpose.get(purpose).setQuick(minute, durationIndexForPurpose.get(purpose)==-1? 0. : Double.parseDouble(record[durationIndexForPurpose.get(purpose)]));
        }

        if (Resources.instance.getBoolean(Properties.ADD_AIRPORT_DEMAND, false)){
            arrivalTimeCumProbByPurpose.get(Purpose.AIRPORT).setQuick(minute, Double.parseDouble(record[airport_arrival_index]));
            departureTimeCumProbByPurpose.get(Purpose.AIRPORT).setQuick(minute, Double.parseDouble(record[airport_departure_index]));
        }
    }

    @Override
    public void read() {
        super.read(Resources.instance.getTimeOfDayDistributionsFilePath(), ",");
        dataSet.setArrivalMinuteCumProbByPurpose(arrivalTimeCumProbByPurpose);
        dataSet.setDurationMinuteCumProbByPurpose(durationCumProbByPurpose);
        dataSet.setDepartureMinuteCumProbByPurpose(departureTimeCumProbByPurpose);
    }


}
