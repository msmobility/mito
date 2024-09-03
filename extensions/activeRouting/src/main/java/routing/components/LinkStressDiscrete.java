package routing.components;

import org.matsim.api.core.v01.network.Link;

import static routing.components.Protection.*;

public enum LinkStressDiscrete {
    GREEN,
    AMBER,
    RED;

    public static LinkStressDiscrete getCycleStress(Link link) {

        if (!link.getAllowedModes().contains("bike")) {
            return null;
        } else if ((boolean) link.getAttributes().getAttribute("allowsCar")) {
            String junction = (String) link.getAttributes().getAttribute("junction");
            if (junction.equals("roundabout") || junction.equals("circular")) {
                return RED;
            } else {
                double speedLimit = (double) link.getAttributes().getAttribute("speedLimitMPH");
                double speed85perc = (double) link.getAttributes().getAttribute("veh85percSpeedKPH") * 0.621371;
                double aadt = ((int) link.getAttributes().getAttribute("aadt")) * 0.865;
                Protection protection = Protection.getType(link);

                if (speed85perc >= speedLimit * 1.1) {
                    speedLimit = speed85perc;
                }

                if (speedLimit <= 21.75) {
                    if (protection.equals(LANE)) {
                        if (aadt >= 4000) {
                            return AMBER;
                        }
                    } else if (protection.equals(MIXED)) {
                        if (aadt >= 4000) {
                            return RED;
                        } else if (aadt >= 2000) {
                            return AMBER;
                        }
                    }
                } else if (speedLimit <= 31.07) {
                    if (protection.equals(LANE)) {
                        if (aadt >= 4000) {
                            return RED;
                        } else {
                            return AMBER;
                        }
                    } else if (protection.equals(MIXED)) {
                        if (aadt >= 2000) {
                            return RED;
                        } else {
                            return AMBER;
                        }
                    }
                } else if (speedLimit <= 40.39) {
                    if (protection.equals(LANE) || protection.equals(MIXED)) {
                        return RED;
                    } else if (protection.equals(PROTECTED)) {
                        return AMBER;
                    }
                } else {
                    if (!protection.equals(KERBED)) {
                        return RED;
                    }
                }
            }
        }
        return GREEN;
    }
}
