package de.tum.bgu.msm.data.accessTimes;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import de.tum.bgu.msm.util.matrices.Matrices;
import omx.OmxFile;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccessAndEgressVariables {

    private final static Logger LOGGER = Logger.getLogger(AccessAndEgressVariables.class);

    public enum AccessVariable {ACCESS_T_MIN, ACCESS_DIST_KM, EGRESS_T_MIN, EGRESS_DIST_KM, ACCESS_VERTIPORT, EGRESS_VERTIPORT, ACCESS_VERTIPORT_NAME}

	private final ConcurrentMap<String, ConcurrentMap<AccessVariable, IndexedDoubleMatrix2D>> matricesByMode = new ConcurrentHashMap<>();

    public final void readSkim(final String mode, final AccessVariable type, final String file, final String matrixName, final double factor) {
        LOGGER.info("Reading " + type + " skim of " + mode);
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
                LOGGER.warn("More than one lookup was provided. Will use the first one (name: " + next + ")");
            }
        }

        final OmxMatrix timeOmxSkimTransit = omx.getMatrix(matrixName);
        final IndexedDoubleMatrix2D skim = Matrices.convertOmxToDoubleMatrix2D(timeOmxSkimTransit, lookup, factor);
        matricesByMode.putIfAbsent(mode, new ConcurrentHashMap<>());
        matricesByMode.get(mode).put(type, skim);
        omx.close();
    }

    /**
     * returns the access time in minutes for the mode
     * @param origin
     * @param destination
     * @param mode
     * @return
     */
	public double getAccessTime(Location origin, Location destination, String mode) {
		int originZone = origin.getZoneId();
		int destinationZone = destination.getZoneId();
		return matricesByMode.get(mode).get(AccessVariable.ACCESS_T_MIN).getIndexed(originZone, destinationZone);
	}

    /**
     * returns a generic access or egress variable
     * @param origin
     * @param destination
     * @param mode
     * @param variable
     * @return
     */
    public double getAccessVariable(Location origin, Location destination, String mode, AccessVariable variable) {
        int originZone = origin.getZoneId();
        int destinationZone = destination.getZoneId();
        return matricesByMode.get(mode).get(variable).getIndexed(originZone, destinationZone);
    }

    public void setExternally(IndexedDoubleMatrix2D matrix, String mode, AccessVariable variable){
        matricesByMode.putIfAbsent(mode, new ConcurrentHashMap<>());
        matricesByMode.get(mode).put(variable, matrix);
    }

}