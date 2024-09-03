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
package routing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.core.controler.AbstractModule;
import routing.travelDisutility.WalkTravelDisutilityFactory;
import routing.travelTime.WalkTravelTime;


public class WalkModule extends AbstractModule {

	private static final Logger LOG = Logger.getLogger(WalkModule.class);

	@Inject
	private WalkConfigGroup walkConfigGroup;

	public WalkModule() {
	}

	@Override
	public void install() {
		this.addTravelTimeBinding(walkConfigGroup.getWalkMode()).to(WalkTravelTime.class).in(Singleton.class);
		this.addTravelDisutilityFactoryBinding(walkConfigGroup.getWalkMode()).to(WalkTravelDisutilityFactory.class).in(Singleton.class);
	}
}
