/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package routing.travelDisutility;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import routing.BicycleConfigGroup;
import routing.WalkConfigGroup;

//import org.matsim.api.core.v01.Scenario;
//import org.matsim.core.router.costcalculators.RandomizingTimeDistanceTravelDisutilityFactory;

/**
 * @author smetzler, dziemke
 */
public final class WalkTravelDisutilityFactory implements TravelDisutilityFactory {
	// public-final is ok since ctor is package-private: can only be used through injection

	private static final Logger LOG = Logger.getLogger(WalkTravelDisutilityFactory.class);

	@Inject
	WalkConfigGroup walkConfigGroup;

	/* package-private */ WalkTravelDisutilityFactory(){}
	
	@Override
	public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
		return new WalkTravelDisutility(walkConfigGroup, timeCalculator);
	}
}
