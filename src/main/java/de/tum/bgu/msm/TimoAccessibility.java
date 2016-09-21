package main.java.de.tum.bgu.msm;

import com.pb.common.util.ResourceUtil;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * Calculates and stores accessibilities
 * Author: Rolf Moeckel, University of Maryland
 * Created on 15 December 2014 in College Park, MD
 **/

public class TimoAccessibility {


    static Logger logger = Logger.getLogger(TimoAccessibility.class);
    private TimoData td;
    private ResourceBundle rb;
    private float[] autoAccessibilityHouseholds;
    private float[] autoAccessibilityRetail;
    private float[] autoAccessibilityOther;
    private float[] transitAccessibilityOther;
    private int[] householdsByZone;
    private int[] retailEmplByZone;
    private int[] otherEmplByZone;


    public TimoAccessibility(ResourceBundle rb, TimoData td) {
        this.td = td;
        this.rb = rb;
    }

        this.householdsByZone = householdsByZone;
        this.retailEmplByZone = retailEmplByZone;
        this.otherEmplByZone = otherEmplByZone ;


    public void calculateAccessibilities () {
        // Calculate Hansen TripGenAccessibility (recalculated every year)

        logger.info("  Calculating accessibilities");
        float alpha = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.alpha");
        float beta = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.beta");

        int[] zones = td.getZones();
        autoAccessibilityHouseholds = new float[zones.length];
        autoAccessibilityRetail = new float[zones.length];
        autoAccessibilityOther = new float[zones.length];
        transitAccessibilityOther = new float[zones.length];
        for (int i = 0; i < zones.length; i++) {
            autoAccessibilityHouseholds[i] = 0;
            autoAccessibilityRetail[i] = 0;
            autoAccessibilityOther[i] = 0;
            transitAccessibilityOther[i] = 0;
            for (int zone : zones) {
                double autoImpedance;
                if (td.getAutoTravelTimes(zones[i], zone) == 0) {      // should never happen for auto
                    autoImpedance = 0;
                } else {
                    autoImpedance = Math.exp(beta * td.getAutoTravelTimes(zones[i], zone));
                }
                double transitImpedance;
                if (td.getTransitTravelTimes(zones[i], zone) == 0) {   // zone is not connected by walk-to-transit
                    transitImpedance = 0;
                } else {
                    transitImpedance = Math.exp(beta * td.getTransitTravelTimes(zones[i], zone));
                }

                autoAccessibilityHouseholds[i] += Math.pow(householdsByZone[td.getZoneIndex(zone)], alpha) * autoImpedance;
                autoAccessibilityRetail[i] += Math.pow(retailEmplByZone[td.getZoneIndex(zone)], alpha) * autoImpedance;
                autoAccessibilityOther[i] += Math.pow(otherEmplByZone[td.getZoneIndex(zone)], alpha) * autoImpedance;
                transitAccessibilityOther[i] += Math.pow(otherEmplByZone[td.getZoneIndex(zone)], alpha) * transitImpedance;
            }
        }
        autoAccessibilityHouseholds = TimoUtil.scaleArray(autoAccessibilityHouseholds, 100);
        autoAccessibilityRetail = TimoUtil.scaleArray(autoAccessibilityRetail, 100);
        autoAccessibilityOther = TimoUtil.scaleArray(autoAccessibilityOther, 100);
        transitAccessibilityOther = TimoUtil.scaleArray(transitAccessibilityOther, 100);
    }



    public float getAutoAccessibilityHouseholds(int zone) {
        return autoAccessibilityHouseholds[td.getZoneIndex(zone)];
    }

    public float getAutoAccessibilityRetail(int zone) {
        return autoAccessibilityRetail[td.getZoneIndex(zone)];
    }

    public float getAutoAccessibilityOther(int zone) {
        return autoAccessibilityOther[td.getZoneIndex(zone)];
    }

    public float getTransitAccessibilityOther(int zone) {
        return transitAccessibilityOther[td.getZoneIndex(zone)];
    }

}
