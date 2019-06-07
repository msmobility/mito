package de.tum.bgu.msm.data.accessTimes;

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

public class AccessTimes {

    private final static Logger LOGGER = Logger.getLogger(AccessTimes.class);

	private final ConcurrentMap<String, IndexedDoubleMatrix2D> matricesByMode = new ConcurrentHashMap<>();

    public final void readSkim(final String mode, final String file, final String matrixName, final double factor) {
        LOGGER.info("Reading " + mode + " skim");
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
        matricesByMode.put(mode, skim);
        omx.close();
    }

	public double getAccessTime(Location origin, Location destination, String mode) {
		int originZone = origin.getZoneId();
		int destinationZone = destination.getZoneId();
		return matricesByMode.get(mode).getIndexed(originZone, destinationZone);
	}
}