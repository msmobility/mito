package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.data.jobTypes.Category;
import de.tum.bgu.msm.io.input.readers.TripAttractionRatesReader;
import de.tum.bgu.msm.modules.tripGeneration.AttractionCalculator;
import de.tum.bgu.msm.modules.tripGeneration.ExplanatoryVariable;
import org.apache.log4j.Logger;

import static de.tum.bgu.msm.modules.tripGeneration.ExplanatoryVariable.HH;

public class AttractionCalculatorMCR implements AttractionCalculator {

    private final Purpose purpose;

    private static final Logger logger = Logger.getLogger(AttractionCalculatorMCR.class);

    private final DataSet dataSet;

    public AttractionCalculatorMCR(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
        new TripAttractionRatesReader(dataSet, purpose).read();
    }

    @Override
    public void run() {
        logger.info("  Calculating trip attractions");
        for (MitoZone zone : dataSet.getZones().values()) {
                float tripAttraction = 0;
                for (ExplanatoryVariable variable : ExplanatoryVariable.values()) {
                    float attribute;
                    if(HH.equals(variable)) {
                        attribute = zone.getNumberOfHouseholds();
                    }else if(zone.getPoiWeightsByType().get(variable.toString())==null) {
                        //logger.warn("Zone has no explanatory variable " + variable + " defined.");
                        continue;
                    }else{
                        attribute = zone.getPoiWeightsByType().get(variable.toString());
                    }
                    Double rate = purpose.getTripAttractionForVariable(variable);
                    if(rate == null) {
                        throw new RuntimeException("Purpose " + purpose + " does not have an attraction" +
                                " rate for variable " + variable + " registered.");
                    }
                    tripAttraction += attribute * rate;
                }
                zone.setTripAttraction(purpose, tripAttraction);
        }
    }
}
