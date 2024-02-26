package de.tum.bgu.msm.trafficAssignment;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.travelDistances.MatrixTravelDistances;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import de.tum.bgu.msm.util.skim.Matsim2Skim;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

public class CarSkimUpdater {

    private final static Logger logger = Logger.getLogger(CarSkimUpdater.class);
    private final Matsim2Skim matsim2Skim;
    private final Network network;
    private final static int NUMBER_OF_CALC_POINTS = 1;
    private final int DEFAULT_PEAK_H_S = 8 * 3600;
    private final IndexedDoubleMatrix2D carTravelTimeMatrix;
    private final IndexedDoubleMatrix2D carDistanceMatrix;
    private TravelDisutility travelDisutility;
    private TravelTime travelTime;
    private DataSet dataSet;
    private final String scenarioName;

    public CarSkimUpdater(Controler controler, DataSet dataSet, String scenarioName) {
        this.network = controler.getScenario().getNetwork();
        //this.zoneFeatureMap = zoneFeatureMap;
        this.travelTime = controler.getLinkTravelTimes();
        this.travelDisutility = controler.getTravelDisutilityFactory().createTravelDisutility(travelTime);
        //creates a matrix of (n+1 zones) rows and columns
        this.carTravelTimeMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.carDistanceMatrix = new IndexedDoubleMatrix2D(dataSet.getZones().values(), dataSet.getZones().values());
        this.dataSet = dataSet;
        this.scenarioName = scenarioName;
        this.matsim2Skim = new Matsim2Skim(network, travelDisutility, travelTime);
    }

    public void run() {
        matsim2Skim.calculateMatrixFromMatsim(NUMBER_OF_CALC_POINTS, carTravelTimeMatrix,
                carDistanceMatrix, DEFAULT_PEAK_H_S, dataSet.getZones().values());
        updateMatrices();
        printSkim();
    }

    private void printSkim() {
        try {
            String fileName = "./scenOutput/" + scenarioName + "/" + dataSet.getYear() + "/" + Resources.instance.getString(Properties.SKIM_FILE_NAME);
            int dimension = dataSet.getZones().size();
            OmxMatrixWriter.createOmxFile(fileName, dimension);

            SkimTravelTimes tt = (SkimTravelTimes) dataSet.getTravelTimes();
            tt.printOutCarSkim(TransportMode.car, fileName, "timeByTime");

            MatrixTravelDistances td = (MatrixTravelDistances) dataSet.getTravelDistancesAuto();
            td.printOutDistanceSkim(fileName, "distanceByTime");

        } catch (ClassCastException e) {
            logger.info("Currently it is not possible to print out a matrix from an object which is not SkimTravelTime");
        }
    }


    private void updateMatrices() {
        SkimTravelTimes skimTravelTimes = (SkimTravelTimes) dataSet.getTravelTimes();
        skimTravelTimes.updateSkimMatrix(carTravelTimeMatrix, TransportMode.car);
        MatrixTravelDistances autoTravelDistances = new MatrixTravelDistances(carDistanceMatrix);
        dataSet.setTravelDistancesAuto(autoTravelDistances);
    }
}
