package de.tum.bgu.msm.data.travelTimes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.MicroLocation;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxMatrix;

public class SkimTravelTimes implements TravelTimes {

    private final static Logger LOGGER = Logger.getLogger(SkimTravelTimes.class);

    private final ConcurrentMap<String, DoubleMatrix2D> matricesByMode = new ConcurrentHashMap<>();

	private final Table<Zone, Region, Double> travelTimeToRegion = HashBasedTable.create();

    /**
     * Use method getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) instead
     */
    @Deprecated
    @Override
    public double getTravelTime(int origin, int destination, double timeOfDay_s, String mode) {
        // Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
        if (mode.equals("pt")) {
            if (matricesByMode.containsKey("pt")) {
                return matricesByMode.get(mode).getQuick(origin, destination);
            } else if (matricesByMode.containsKey("bus") && matricesByMode.containsKey("tramMetro") && matricesByMode.containsKey("train")){
                return getMinimumPtTravelTime(origin, destination, timeOfDay_s);
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
        LOGGER.warn("The skim matrix for mode " + mode + " has been updated");
        
        travelTimeToRegion.clear();
    }

    private double getMinimumPtTravelTime(int origin, int destination, double timeOfDay_s) {
        double travelTime = Double.MAX_VALUE;
        if (matricesByMode.get("bus").getQuick(origin, destination) < travelTime) {
            travelTime = matricesByMode.get("bus").getQuick(origin, destination);
        }
        if (matricesByMode.get("tramMetro").getQuick(origin, destination) < travelTime){
            travelTime = matricesByMode.get("tramMetro").getQuick(origin, destination);
        }
        if (matricesByMode.get("train").getQuick(origin, destination) < travelTime) {
            travelTime = matricesByMode.get("train").getQuick(origin, destination);
        }
        return travelTime;
    }

    public void printOutCarSkim(String mode, String filePath, String matrixName) {
        OmxMatrixWriter.createOmxSkimMatrix(matricesByMode.get(mode),
                filePath,
                matrixName);
    }

	@Override
	public double getTravelTime(Location origin, Location destination, double timeOfDay_s, String mode) {
		if (origin instanceof MicroLocation) {
			if (destination instanceof MicroLocation) {
				return matricesByMode.get(mode).getQuick(((MicroLocation) origin).getZone().getId(),
						((MicroLocation) destination).getZone().getId());
			} else {
				throw new IllegalArgumentException("Travel time requests involving for MicroLocation are only implemented for "
						+ "skim-based travel times if both origin and destination are Microlocations.");
			}
		} else if (origin instanceof Zone) {
			Zone originZone = (Zone) origin;
			if (destination instanceof Zone) {
				Zone destinationZone = (Zone) destination;
				// Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
		        // different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
		        if (mode.equals("pt")) {
		            if (matricesByMode.containsKey("pt")) {
		                return matricesByMode.get(mode).getQuick(originZone.getId(), destinationZone.getId());
		            } else if (matricesByMode.containsKey("bus") && matricesByMode.containsKey("tramMetro") && matricesByMode.containsKey("train")){
		                return getMinimumPtTravelTime(originZone.getId(), destinationZone.getId(), timeOfDay_s);
		            } else {
		                throw new RuntimeException("define transit travel modes!!");
		            }
		        } else {
		            return matricesByMode.get(mode).getQuick(originZone.getId(), destinationZone.getId());
		        }
			} else if (destination instanceof Region) {
				Region destinationRegion = (Region) destination;
				if (travelTimeToRegion.contains(originZone, destinationRegion)) {
					return travelTimeToRegion.get(originZone, destinationRegion);
				}
				double min = Double.MAX_VALUE;
        		for (Zone zoneInRegion : destinationRegion.getZones()) {
        			double travelTime = getTravelTime(origin, zoneInRegion, timeOfDay_s, mode);
        			if (travelTime < min) {
        				min = travelTime;
        			}
        		}
        		travelTimeToRegion.put(originZone, destinationRegion, min);
        		// TODO This suggestion was in Accessibility before with the following comment (by Carlos)
        	    //this is method is proposed as an alternative for the calculation of time from zone to region
        		// ...nk/dz, july'18
//        	    double average = destinationRegion.getZones().stream().mapToDouble(zoneInRegion -> 
//        	    	getTravelTime(originZone, zoneInRegion, timeOfDay_s, mode)).average().getAsDouble();
//        	    travelTimeToRegion.put(originZone, destinationRegion, average);
        		return min;
			}
		}
		throw new IllegalArgumentException("The combination with origin of type " + origin.getClass().getName() 
				+ " and destination of type " + destination.getClass().getName() + " is not valid.");
	}
	
	public DoubleMatrix2D getMatrixForMode(String mode) {
			return matricesByMode.get(mode);
	}
}