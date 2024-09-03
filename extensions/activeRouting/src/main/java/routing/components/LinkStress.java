package routing.components;

import org.matsim.api.core.v01.network.Link;

import static routing.components.Protection.*;


public class LinkStress {

    public static double getStress(Link link, String mode) {

        if (!mode.equals("walk") && !mode.equals("bike")) {
            throw new RuntimeException("unknown mode " + mode);
        } else if (!link.getAllowedModes().contains(mode)) {
            return Double.NaN;
        } else if ((boolean) link.getAttributes().getAttribute("allowsCar")) {
            String junction = (String) link.getAttributes().getAttribute("junction");
            if (mode.equals("bike") && (junction.equals("roundabout") || junction.equals("circular"))) {
                return 1;
            } else {
                double stress;
                double speedLimit = (double) link.getAttributes().getAttribute("speedLimitMPH");
                double speed85perc = (double) link.getAttributes().getAttribute("veh85percSpeedKPH") * 0.621371;
                double aadt = ((int) link.getAttributes().getAttribute("aadt")) * 0.865;
                Protection protection = Protection.getType(link);

                if (speed85perc >= speedLimit * 1.1) {
                    speedLimit = speed85perc;
                }

                double intercept;
                double speedFactor;
                double aadtFactor;

                if (protection.equals(KERBED)) {
                    intercept = 0;
                    speedFactor = 0;
                    aadtFactor = 0;
                } else if (mode.equals("walk") || protection.equals(PROTECTED)) {
                    intercept = -1.5;
                    speedFactor = 0.05;
                    aadtFactor = 0;
                } else if (protection.equals(LANE)) {
                    intercept = -1.625;
                    speedFactor = 0.0625;
                    aadtFactor = 0.000125;
                } else {
                    intercept = -1.25;
                    speedFactor = 0.0583;
                    aadtFactor = 0.000167;
                }

                double freightPoiFactor = getFreightPoiFactor(link);

                stress = intercept + speedFactor * speedLimit + aadtFactor * aadt + 0.2 * freightPoiFactor;

                // Ensure between 0 and 1
                if (stress < 0.) stress = 0;
                if (stress > 1.) stress = 1;

                return stress;
            }
        } else return 0;
    }

    public static double getFreightPoiFactor (Link link){
        int hgvPois = (int) link.getAttributes().getAttribute("hgvPOIs");
        return Math.min(1., 24 * hgvPois / link.getLength());
    }

}