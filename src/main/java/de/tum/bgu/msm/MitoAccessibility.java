package de.tum.bgu.msm;

import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Calculates and stores accessibilities
 * Author: Rolf Moeckel, Technical University of Munich
 * Created on 15 December 2014 in College Park, MD
 * Revised on 20 October 2016 in Munich
 **/

public class MitoAccessibility {


    static Logger logger = Logger.getLogger(MitoAccessibility.class);
    private MitoData td;
    private ResourceBundle rb;
    private float[] autoAccessibilityHouseholds;
    private float[] autoAccessibilityRetail;
    private float[] autoAccessibilityOther;
    private float[] transitAccessibilityOther;


    public MitoAccessibility(ResourceBundle rb, MitoData td) {
        this.td = td;
        this.rb = rb;
    }


    public void calculateAccessibilities () {
        // Calculate Hansen TripGenAccessibility (recalculated every year)

        logger.info("  Calculating accessibilities");
        float alpha = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.alpha");
        float beta = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.beta");

        Collection<Zone> zones = td.getZones().values();
        autoAccessibilityHouseholds = new float[zones.size()];
        autoAccessibilityRetail = new float[zones.size()];
        autoAccessibilityOther = new float[zones.size()];
        transitAccessibilityOther = new float[zones.size()];
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

                autoAccessibilityHouseholds[i] += Math.pow(td.getHouseholdsByZone(zone), alpha) * autoImpedance;
                autoAccessibilityRetail[i] += Math.pow(td.getRetailEmplByZone(zone), alpha) * autoImpedance;
                autoAccessibilityOther[i] += Math.pow(td.getOtherEmplByZone(zone), alpha) * autoImpedance;
                transitAccessibilityOther[i] += Math.pow(td.getOtherEmplByZone(zone), alpha) * transitImpedance;
            }
        }
        autoAccessibilityHouseholds = MitoUtil.scaleArray(autoAccessibilityHouseholds, 100);
        autoAccessibilityRetail = MitoUtil.scaleArray(autoAccessibilityRetail, 100);
        autoAccessibilityOther = MitoUtil.scaleArray(autoAccessibilityOther, 100);
        transitAccessibilityOther = MitoUtil.scaleArray(transitAccessibilityOther, 100);
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
