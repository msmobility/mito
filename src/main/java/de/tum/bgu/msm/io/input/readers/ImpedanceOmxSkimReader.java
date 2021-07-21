package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.impedances.Impedance;
import de.tum.bgu.msm.data.impedances.TomTomImpedance;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.input.AbstractOmxReader;
import de.tum.bgu.msm.modules.tripDistribution.ExplanatoryVariable;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;

public class ImpedanceOmxSkimReader extends AbstractOmxReader {

    private static final Logger LOGGER = Logger.getLogger(ImpedanceOmxSkimReader.class);

    public ImpedanceOmxSkimReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        readTomTomMatrix();
    }

    public void readTomTomMatrix(){
        LOGGER.info("Reading skims for tomtom od trips");
        IndexedDoubleMatrix2D impedanceMatrix =
                AbstractOmxReader.readAndConvertToDoubleMatrix(Resources.instance.getTomTomMatrixPath().toString(),
                        "tomtom_od" ,
                        1./1000);
        Impedance impedance = new TomTomImpedance(impedanceMatrix);
        dataSet.addImpedance(ExplanatoryVariable.tomTomOdIntensity, impedance);
    }

}
