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
import org.matsim.contrib.bicycle.BicycleLinkSpeedCalculator;
import org.matsim.core.controler.AbstractModule;
import routing.travelDisutility.BicycleTravelDisutilityFactory;
import routing.travelTime.BicycleTravelTime;
import routing.travelTime.speed.BicycleLinkSpeedCalculatorDefaultImpl;


public class BicycleModule extends AbstractModule {

	private static final Logger LOG = Logger.getLogger(BicycleModule.class);

	@Inject
	private BicycleConfigGroup bicycleConfigGroup;

	public BicycleModule() {
	}

	@Override
	public void install() {
		addTravelTimeBinding(bicycleConfigGroup.getBicycleMode()).to(BicycleTravelTime.class).in(Singleton.class);
		addTravelDisutilityFactoryBinding(bicycleConfigGroup.getBicycleMode()).to(BicycleTravelDisutilityFactory.class).in(Singleton.class);
		bind( BicycleLinkSpeedCalculator.class ).to( BicycleLinkSpeedCalculatorDefaultImpl.class) ;
	}
}
