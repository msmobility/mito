package routing.components;

import org.matsim.api.core.v01.network.Link;

import static routing.components.Crossing.*;


public class JctStress {

    public static double getStress(Link link, String mode) {

        if(!mode.equals("walk") && !mode.equals("bike")) {
            throw new RuntimeException("unknown mode " + mode);
        } else if(!link.getAllowedModes().contains(mode)) {
            return Double.NaN;
        } else if((boolean) link.getAttributes().getAttribute("crossVehicles")) {
            double stress = 0;
            Double crossingAadt = (Double) link.getAttributes().getAttribute("crossAadt") * 0.865;
            double crossingLanes = (double) link.getAttributes().getAttribute("crossLanes");
            double crossingSpeed = (double) link.getAttributes().getAttribute("crossSpeedLimitMPH");
            double crossingSpeed85perc = (double) link.getAttributes().getAttribute("cross85PercSpeed") * 0.621371;
            if(crossingAadt.isNaN()) crossingAadt = 800.;

            Crossing crossingType = Crossing.getType(link,mode);

            if(crossingSpeed85perc >= crossingSpeed*1.1) {
                crossingSpeed = crossingSpeed85perc;
            }

            if(crossingType.equals(UNCONTROLLED)) {
                if(crossingSpeed < 60) {
                    stress = crossingAadt/(300*crossingSpeed + 16500) + crossingSpeed/90 + crossingLanes/3 - 0.5;
                } else {
                    stress = 1.;
                }
            } else if(crossingType.equals(ZEBRA)) {
                if(crossingSpeed <= 30) {
                    stress = crossingAadt/24000 + crossingLanes/3 - 2./3;
                } else {
                    stress = crossingSpeed/90 + 1./3;
                }
            } else if(crossingType.equals(SIGNAL_MIXED)) {
                if(crossingSpeed < 60) {
                    stress = LinkStress.getStress(link,mode);
                } else {
                    stress = 1.;
                }
            } else if(crossingType.equals(SIGNAL_ACTIVE)) {
                if(crossingSpeed < 60) {
                    stress = 0;
                } else {
                    stress = 1.;
                }
            }

            // Ensure between 0 and 1
            if (stress < 0.) stress = 0;
            if (stress > 1.) stress = 1;

            return stress;
        } else return 0;
    }
}