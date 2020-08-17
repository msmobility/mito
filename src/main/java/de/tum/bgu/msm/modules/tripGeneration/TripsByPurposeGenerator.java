package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Vector;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.DROPPED_TRIPS_AT_BORDER_COUNTER;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

class TripsByPurposeGenerator extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGenerator.class);
    private static final ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<>();

    private final boolean dropAtBorder = Resources.instance.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER);

    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private double scaleFactorForGeneration;


    TripsByPurposeGenerator(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.scaleFactorForGeneration = scaleFactorForGeneration;
    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        generateTripsInR(purpose);
        return new Tuple<>(purpose, tripsByHH);
    }

    private void generateTripsInR(Purpose purpose) throws Exception {

        int numberOfHouseholds = dataSet.getHouseholds().size();
        Vector probabilityVector = null;
        Matrix probabilityMatrix;

        ScriptEngine engine = ENGINE.get();

        if(engine == null) {
            // create a new engine for this thread
            RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
            engine = factory.getScriptEngine();

            String modelFileName = "tripGenModel_" + purpose + ".rds";
            String modelFilePath = this.getClass().getResource(modelFileName).getPath();

            logger.info("Sharing " + purpose + " MITO data with R");
            engine.put("modelFilePath",modelFilePath);
            engine.put("model_data", dataSet.getRdataFrame());

            logger.info("Reading " + modelFileName + " into R");
            engine.eval("model <- readRDS(modelFilePath)");

            logger.info("Building " + purpose + " probability matrix in R");
            engine.eval("probability_matrix <- predict(model, type = \"prob\", newdata = model_data)");

            // Dump R output to a .rda file (this is an example for reading a largeR R script from resources folder)
//            engine.put("purpose",purpose.toString());
//            engine.eval(new InputStreamReader(this.getClass().getResourceAsStream("tripGenModel.R")));

            // Retrieve probability matrix & corresponding household IDs from R
            probabilityVector = (Vector)engine.eval("probability_matrix");

            ENGINE.set(engine);
        }

        // Print details on probability matrices for each purpose
        probabilityMatrix = new Matrix(probabilityVector);
        try {
            logger.info(purpose + ": Probability Matrix is a " + probabilityMatrix.getNumRows() + "x" + probabilityMatrix.getNumCols() + " matrix.");
        } catch(IllegalArgumentException e) {
            logger.info(purpose + ": Probability Matrix is not a matrix: " + e);
        }

        // Get vector of household IDs that match probability rows of probability matrix
        Vector hhIdVector = dataSet.getRdataFrame().getElementAsVector("hh.id");
        int probabilityMatrixCols = probabilityMatrix.getNumCols();

        // Possible numbers of trips that can be chosen (column names of probability matrix)
        int[] possibleSelections = new int[probabilityMatrixCols];
        for(int i = 0 ; i < probabilityMatrixCols ; i++) {
            possibleSelections[i] = Integer.parseInt(probabilityMatrix.getColName(i));
        }

        // Loop over householdIDs and probabilities and generate trips
        logger.info("Assigning trips for purpose: " + purpose);
        for(int i = 0 ; i < numberOfHouseholds ; i++ ) {

            int hhId = hhIdVector.getElementAsInt(i);
            MitoHousehold hh = dataSet.getHouseholds().get(hhId);

            double[] tripFrequencies = new double[probabilityMatrixCols];
            for(int j = 0 ; j < probabilityMatrixCols ; j++) {
                tripFrequencies[j] = probabilityMatrix.getElementAsDouble(i,j);
            }

            int selection = MitoUtil.select(tripFrequencies);
            int numberOfTrips = possibleSelections[selection];

            if(hh.getHomeZone() != null) {
                generateTripsForHousehold(hh, numberOfTrips);
            } else {
                logger.info("Couldn't generate trips for household " + hhId + " purpose " + purpose + ": no home zone");
            }

        }
    }

    private void generateTripsForHousehold(MitoHousehold hh, int numberOfTrips) {
        List<MitoTrip> trips = new ArrayList<>();
        for (int i = 0; i < numberOfTrips; i++) {
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration){
                MitoTrip trip = createTrip(hh);
                if (trip != null) {
                    trips.add(trip);
                }
            }
        }
        tripsByHH.put(hh, trips);
    }

    private MitoTrip createTrip(MitoHousehold hh) {
        boolean dropThisTrip = reduceTripGenAtStudyAreaBorder(hh.getHomeZone());
        if (dropThisTrip) {
            DROPPED_TRIPS_AT_BORDER_COUNTER.incrementAndGet();
            return null;
        }
        return new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), purpose);
    }

    private boolean reduceTripGenAtStudyAreaBorder(MitoZone tripOrigin) {
        if (dropAtBorder) {
            float damper = dataSet.getZones().get(tripOrigin.getId()).getReductionAtBorderDamper();
            return random.nextFloat() < damper;
        }
        return false;
    }
}
