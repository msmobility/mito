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

import org.matsim.core.config.ReflectiveConfigGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * @author smetzler, dziemke
 */
public class BicycleConfigGroup extends ReflectiveConfigGroup {
	// necessary to have this public

	public static final String GROUP_NAME = "bike";
	private HashMap<String, Double> marginalCostGradient = new HashMap<>();
	private HashMap<String, Double> marginalCostVgvi = new HashMap<>();
	private HashMap<String, Double> marginalCostLinkStress = new HashMap<>();
	private HashMap<String, Double> marginalCostJctStress = new HashMap<>();

	private double maxBicycleSpeedForRouting = 20./3.6;
	private String bicycleMode = "bike";


	public BicycleConfigGroup() {
		super(GROUP_NAME);
	}


	public HashMap<String, Double> getMarginalCostGradient() {
		return marginalCostGradient;
	}

	public HashMap<String, Double> getMarginalCostVgvi() {
		return marginalCostVgvi;
	}

	public HashMap<String, Double> getMarginalCostLinkStress() {
		return marginalCostLinkStress;
	}

	public HashMap<String, Double> getMarginalCostJctStress() {
		return marginalCostJctStress;
	}

	public String getBicycleMode() {
		return bicycleMode;
	}

	public double getMaxBicycleSpeedForRouting() {
		return maxBicycleSpeedForRouting;
	}
}
