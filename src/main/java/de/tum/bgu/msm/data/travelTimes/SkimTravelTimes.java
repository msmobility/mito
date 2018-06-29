package de.tum.bgu.msm.data.travelTimes;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxMatrix;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkimTravelTimes implements TravelTimes {

    private final static Logger LOGGER = Logger.getLogger(SkimTravelTimes.class);

    private final ConcurrentMap<String, DoubleMatrix2D> matricesByMode = new ConcurrentHashMap<>();

    /**
     * retrieves the travel time between origin and destination for a specific mode and time.
     * @param origin zone id of the origin
     * @param destination zone id of the destination
     * @param timeOfDay_s time of day in seconds
     * @param mode mode for which the travel time applies
     * @return the travel time in minutes
     */
    @Override
    public double getTravelTime(int origin, int destination, double timeOfDay_s, String mode) {
        // Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
        if (mode.equals("pt")) {
            if (matricesByMode.containsKey("pt")) {
                return matricesByMode.get(mode).getQuick(origin, destination);
            } else if (matricesByMode.containsKey("bus") && matricesByMode.containsKey("tramMetro") && matricesByMode.containsKey("train")){
                return getPtTime(origin, destination, timeOfDay_s);
            } else {
                throw new RuntimeException("define transit travel modes!!");
            }
        } else {
            return matricesByMode.get(mode).getQuick(origin, destination);
        }
    }

    /**
     * Reads a skim matrix from an omx file and stores it for the given mode and year. To allow conversion between units
     * use the factor to multiply all values.
     * @param mode the mode for which the travel times are read
     * @param file the path to the omx file
     * @param matrixName the name of the matrix inside the omx file
     * @param factor a scalar factor which every entry is multiplied with
     */
    public final void readSkim(final String mode, final String file, final String matrixName, final double factor) {
        LOGGER.info("Reading " + mode + " skim");
        final OmxFile omx = new OmxFile(file);
        omx.openReadOnly();
        final OmxMatrix timeOmxSkimTransit = omx.getMatrix(matrixName);
        final DoubleMatrix2D skim = Matrices.convertOmxToDoubleMatrix2D(timeOmxSkimTransit, factor);
        matricesByMode.put(mode, skim);
    }

    /**
     * Updates a skim matrix from an external source
     * @param mode the mode for which the travel times are read
     * @param skim the skim matrix with travel times in minutes
     */
    public void updateSkimMatrix(DoubleMatrix2D skim, String mode){
        matricesByMode.put(mode, skim);
        LOGGER.warn("The skim matrix for mode " + mode + "has been updated");
    }

    private double getPtTime(int origin, int destination, double timeOfDay_s) {
        double travelTime = Double.MAX_VALUE;
        if (matricesByMode.get("bus").getQuick(origin, destination) < travelTime) {
            travelTime = getTravelTime(origin, destination, timeOfDay_s, "bus");
        }
        if (matricesByMode.get("bus").getQuick(origin, destination) < travelTime){
            travelTime = getTravelTime(origin, destination, timeOfDay_s, "tramMetro");
        }
        if (matricesByMode.get("bus").getQuick(origin, destination) < travelTime) {
            travelTime = getTravelTime(origin, destination, timeOfDay_s, "train");
        }
        return travelTime;
    }
}