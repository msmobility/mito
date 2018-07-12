package de.tum.bgu.msm.data;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Should be moved to MSM CommonBase once this exists
 * @author nico, dziemke
 */
public class MicroLocation implements Location {
	private final Coordinate coordinate;
	private final Zone zone;
	
	/**
	 *  Constructs a <code>MicroLocation</code> at (x,y).
	 *
	 *@param  x  The x-value
	 *@param  y  The y-value
	 *@param  zone  The zone in which this microlocation is located
	 */
	public MicroLocation(double x, double y, Zone zone) {
		this.coordinate = new Coordinate(x, y);
		this.zone = zone;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public Zone getZone() {
		return zone;
	}
}