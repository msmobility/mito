package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.data.Zone;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkimTravelTimes implements TravelTimes {

    private final static Logger logger = Logger.getLogger(SkimTravelTimes.class);

    private final ConcurrentMap<String, IndexedDoubleMatrix2D> matricesByMode = new ConcurrentHashMap<>();

	private  IndexedDoubleMatrix2D travelTimeToRegion;

    /**
     * Reads a skim matrix from an omx file and stores it for the given mode and year. To allow conversion between units
     * use the factor to multiply all values.
     * @param mode the mode for which the travel times are read
     * @param file the path to the omx file
     * @param matrixName the name of the matrix inside the omx file
     * @param factor a scalar factor which every entry is multiplied with
     */
    public final void readSkim(final String mode, final String file, final String matrixName, final double factor) {
        logger.info("Reading " + mode + " skim");
        final OmxFile omx = new OmxFile(file);
        omx.openReadOnly();
        final Set<String> lookupNames = omx.getLookupNames();
        OmxLookup lookup = null;
        if(!lookupNames.isEmpty()) {
            final Iterator<String> iterator = omx.getLookupNames().iterator();
            final String next = iterator.next();
            lookup = omx.getLookup(next);
            if(!lookup.getOmxJavaType().equals(OmxHdf5Datatype.OmxJavaType.INT)) {
                throw new IllegalArgumentException("Provided omx matrix lookup " +
                        "is not of type int but of type: " + lookup.getOmxJavaType().name() +
                        " please use int.");
            }
            if(iterator.hasNext()) {
                logger.warn("More than one lookup was provided. Will use the first one (name: " + next + ")");
            }
        }
        final OmxMatrix timeOmxSkimTransit = omx.getMatrix(matrixName);
        final IndexedDoubleMatrix2D skim = Matrices.convertOmxToDoubleMatrix2D(timeOmxSkimTransit, lookup, factor);
        matricesByMode.put(mode, skim);
        omx.close();
        travelTimeToRegion = null;
    }

    public void updateZoneToRegionTravelTimes(Collection<Zone> zones, Collection<Region> regions) {
        logger.info("Updating minimal zone to region travel times...");
        travelTimeToRegion = new IndexedDoubleMatrix2D(zones, regions);
        zones.parallelStream().forEach( z -> {
            for(Region region: regions) {
                int originZone = z.getZoneId();
                double min = Double.MAX_VALUE;
                for (Zone zoneInRegion : region.getZones()) {
                    double travelTime = matricesByMode.get(TransportMode.car).getIndexed(originZone, zoneInRegion.getZoneId());
                    if (travelTime < min) {
                        min = travelTime;
                    }
                }
                travelTimeToRegion.setIndexed(originZone, region.getId(), min);
            }
        });
    }


    /**
     * Updates a skim matrix from an external source
     * @param mode the mode for which the travel times are read
     * @param skim the skim matrix with travel times in minutes
     */
    public void updateSkimMatrix(IndexedDoubleMatrix2D skim, String mode){
        matricesByMode.put(mode, skim);
        logger.warn("The skim matrix for mode " + mode + " has been updated");

        if(TransportMode.car.equals(mode)) {
            travelTimeToRegion = null;
        }
    }

    private double getMinimumPtTravelTime(int origin, int destination, double timeOfDay_s) {
        double travelTime = Double.MAX_VALUE;
        if (matricesByMode.get("bus").getIndexed(origin, destination) < travelTime) {
            travelTime = matricesByMode.get("bus").getIndexed(origin, destination);
        }
        if (matricesByMode.get("tramMetro").getIndexed(origin, destination) < travelTime){
            travelTime = matricesByMode.get("tramMetro").getIndexed(origin, destination);
        }
        if (matricesByMode.get("train").getIndexed(origin, destination) < travelTime) {
            travelTime = matricesByMode.get("train").getIndexed(origin, destination);
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
		int originZone = origin.getZoneId();
		int destinationZone = destination.getZoneId();
	
		// Currently, the time of day is not used here, but it could. E.g. if there are multiple matrices for
		// different "time-of-day slices" the argument could be used to select the correct matrix, nk/dz, jan'18
		if (mode.equals("pt")) {
			if (matricesByMode.containsKey("pt")) {
				return matricesByMode.get(mode).getIndexed(originZone, destinationZone);
			} else if (matricesByMode.containsKey("bus") && matricesByMode.containsKey("tramMetro") && matricesByMode.containsKey("train")){
				return getMinimumPtTravelTime(originZone, destinationZone, timeOfDay_s);
			} else {
				throw new RuntimeException("define transit travel modes!!");
			}
		} else {
			return matricesByMode.get(mode).getIndexed(originZone, destinationZone);
		}
	}
	
	@Override
	public double getTravelTimeToRegion(Location origin, Region destination, double timeOfDay_s, String mode) {
        if(travelTimeToRegion == null) {
            throw new RuntimeException("Travel time to regions not initialized. " +
                    "Make sure to call updateZoneToRegionTravelTimes() first");
        }
        return travelTimeToRegion.getIndexed(origin.getZoneId(), destination.getId());

//        if (origin instanceof Zone) {
//            int originZone = origin.getZoneId();
//            if (travelTimeToRegion.getIndexed(originZone, destination.getId()) > 0) {
//                return travelTimeToRegion.getIndexed(originZone, destination.getId());
//            }
//            double min = Double.MAX_VALUE;
//            for (Zone zoneInRegion : destination.getZones()) {
//                double travelTime = matricesByMode.get(mode).getIndexed(originZone, zoneInRegion.getZoneId());
//                if (travelTime < min) {
//                    min = travelTime;
//                }
//            }
//            travelTimeToRegion.setIndexed(originZone, destination.getId(), min);
//            return min;
//        } else {
//            throw new IllegalArgumentException("Not implemented for origins of types other than Zone. Type is of type " + origin.getClass());
//        }
	}

    @Override
    public IndexedDoubleMatrix2D getPeakSkim(String mode) {
        return matricesByMode.get(mode);
    }

    @Override
    public TravelTimes duplicate() {
        SkimTravelTimes travelTimes = new SkimTravelTimes();
        for(Map.Entry<String, IndexedDoubleMatrix2D> skims: this.matricesByMode.entrySet()) {
            travelTimes.matricesByMode.put(skims.getKey(), skims.getValue().copy());
        }
        travelTimes.travelTimeToRegion = this.travelTimeToRegion.copy();
        return travelTimes;
    }

    //TODO: used in silo. should probably return a deep copy to prevent illegal changes.
	public IndexedDoubleMatrix2D getMatrixForMode(String mode) {
			return matricesByMode.get(mode);
	}
}