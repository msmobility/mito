package de.tum.bgu.msm.data.travelTimes;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.CsvGzSkimMatrixReader;
import de.tum.bgu.msm.io.output.OmxMatrixWriter;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkimTravelTimes implements TravelTimes {

    private final static Logger logger = Logger.getLogger(SkimTravelTimes.class);

    private final ConcurrentMap<String, IndexedDoubleMatrix2D> matricesByMode = new ConcurrentHashMap<>();

    private Map<String, IndexedDoubleMatrix2D> travelTimesFromRegion = new HashMap<>();
    private final Map<String, IndexedDoubleMatrix2D> travelTimesToRegion = new HashMap<>();

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
        travelTimesFromRegion.clear();
        travelTimesToRegion.clear();
    }

    /**
     * Reads a skim matrix from an csv.gz file and stores it for the given mode and year. To allow conversion between units
     * use the factor to multiply all values.
     * @param mode the mode for which the travel times are read
     * @param file the path to the file
     * @param factor a scalar factor which every entry is multiplied with
     */
    public final void readSkimFromCsvGz(final String mode, final String file, final double factor,Collection<? extends Id> zoneLookup) {
        logger.info("Reading " + mode + " skim");
        IndexedDoubleMatrix2D skim = new CsvGzSkimMatrixReader().readAndConvertToDoubleMatrix2D(file, factor, zoneLookup);
        matricesByMode.put(mode, skim);
        travelTimesFromRegion.clear();
        travelTimesToRegion.clear();
    }

    // called from within SILO!
    public void updateRegionalTravelTimes(Collection<Region> regions, Collection<Zone> zones) {
        logger.info("Updating minimal zone to region travel times...");
        IndexedDoubleMatrix2D travelTimesFromRegionCar = new IndexedDoubleMatrix2D(regions, zones);
        IndexedDoubleMatrix2D travelTimesToRegionCar = new IndexedDoubleMatrix2D(zones, regions);
        IndexedDoubleMatrix2D travelTimesFromRegionPt = new IndexedDoubleMatrix2D(regions, zones);
        IndexedDoubleMatrix2D travelTimesToRegionPt = new IndexedDoubleMatrix2D(zones, regions);

        regions.parallelStream().forEach( r -> {
            for(Zone zone: zones) {
                int zoneId = zone.getZoneId();
                double minFromCar = Double.MAX_VALUE;
                double minToCar = Double.MAX_VALUE;
                double minFromPt = Double.MAX_VALUE;
                double minToPt = Double.MAX_VALUE;

                for (Zone zoneInRegion : r.getZones()) {
                    double travelTimeFromRegionCar = matricesByMode.get(TransportMode.car).getIndexed(zoneInRegion.getZoneId(), zoneId);
                    if (travelTimeFromRegionCar < minFromCar) {
                        minFromCar = travelTimeFromRegionCar;
                    }
                    double travelTimeToRegionCar = matricesByMode.get(TransportMode.car).getIndexed(zoneId, zoneInRegion.getZoneId());
                    if (travelTimeToRegionCar < minToCar) {
                        minToCar = travelTimeToRegionCar;
                    }
                    double travelTimeFromRegionPt = matricesByMode.get(TransportMode.pt).getIndexed(zoneInRegion.getZoneId(), zoneId);
                    if (travelTimeFromRegionCar < minFromPt) {
                        minFromPt = travelTimeFromRegionPt;
                    }
                    double travelTimeToRegionPt = matricesByMode.get(TransportMode.pt).getIndexed(zoneId, zoneInRegion.getZoneId());
                    if (travelTimeToRegionPt < minToPt) {
                        minToPt = travelTimeToRegionPt;
                    }
                }
                travelTimesFromRegionCar.setIndexed(r.getId(), zoneId, minFromCar);
                travelTimesToRegionCar.setIndexed(zoneId, r.getId(), minToCar);
                travelTimesFromRegionPt.setIndexed(r.getId(), zoneId, minFromPt);
                travelTimesToRegionPt.setIndexed(zoneId, r.getId(), minToPt);
            }
        });
        travelTimesFromRegion.put(TransportMode.car, travelTimesFromRegionCar);
        travelTimesFromRegion.put(TransportMode.pt, travelTimesFromRegionPt);
        travelTimesToRegion.put(TransportMode.car, travelTimesToRegionCar);
        travelTimesToRegion.put(TransportMode.pt, travelTimesToRegionPt);
    }


    /**
     * Updates a skim matrix from an external source
     * @param mode the mode for which the travel times are read
     * @param skim the skim matrix with travel times in minutes
     */
    public void updateSkimMatrix(IndexedDoubleMatrix2D skim, String mode){
        matricesByMode.put(mode, skim);
        logger.warn("The skim matrix for mode " + mode + " has been updated");
        travelTimesFromRegion.remove(mode);
        travelTimesToRegion.remove(mode);
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
	public double getTravelTimeFromRegion(Region origin, Zone destination, double timeOfDay_s, String mode) {
        if(!travelTimesFromRegion.containsKey(mode)) {
            throw new RuntimeException("Travel time to regions not initialized. " +
                    "Make sure to call updateZoneToRegionTravelTimes() first");
        }
        return travelTimesFromRegion.get(mode).getIndexed(origin.getId(), destination.getId());
	}

    @Override
    public double getTravelTimeToRegion(Zone origin, Region destination, double timeOfDay_s, String mode) {
        if(!travelTimesToRegion.containsKey(mode)) {
            throw new RuntimeException("Travel time to regions not initialized. " +
                    "Make sure to call updateZoneToRegionTravelTimes() first");
        }
        return travelTimesToRegion.get(mode).getIndexed(origin.getId(), destination.getId());
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
        for(Map.Entry<String, IndexedDoubleMatrix2D> entry: travelTimesFromRegion.entrySet()) {
            travelTimes.travelTimesFromRegion.put(entry.getKey(), entry.getValue().copy());
        }
        for(Map.Entry<String, IndexedDoubleMatrix2D> entry: travelTimesToRegion.entrySet()) {
            travelTimes.travelTimesToRegion.put(entry.getKey(), entry.getValue().copy());
        }
        return travelTimes;
    }

    //TODO: used in silo. should probably return a deep copy to prevent illegal changes.
	public IndexedDoubleMatrix2D getMatrixForMode(String mode) {
			return matricesByMode.get(mode);
	}
}