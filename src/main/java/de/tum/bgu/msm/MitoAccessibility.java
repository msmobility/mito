package de.tum.bgu.msm;

import com.pb.common.util.ResourceUtil;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Calculates and stores accessibilities
 * Author: Rolf Moeckel, Technical University of Munich
 * Created on 15 December 2014 in College Park, MD
 * Revised on 20 October 2016 in Munich
 **/

public class MitoAccessibility {


    static Logger logger = Logger.getLogger(MitoAccessibility.class);
    private final DataSet dataSet;
    private final ResourceBundle rb;

    public MitoAccessibility(ResourceBundle rb, DataSet dataSet) {
        this.dataSet = dataSet;
        this.rb = rb;
    }


    public void calculateAccessibilities () {
        // Calculate Hansen TripGenAccessibility (recalculated every year)

        logger.info("  Calculating accessibilities");
        float alpha = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.alpha");
        float beta = (float) ResourceUtil.getDoubleProperty(rb, "accessibility.beta");

        Collection<Zone> zones = dataSet.getZones().values();
        Map<Integer, Float> autoAccessibilityHouseholdsByZone = new HashMap<>();
        Map<Integer, Float>  autoAccessibilityRetailByZone = new HashMap<>();
        Map<Integer, Float>  autoAccessibilityOtherByZone  = new HashMap<>();
        Map<Integer, Float>  transitAccessibilityOtherByZone  = new HashMap<>();

        for (Zone zone: zones) {
            float autoAccessibilityHouseholds = 0;
            float autoAccessibilityRetail = 0;
            float autoAccessibilityOther = 0;
            float transitAccessibilityOther = 0;
            for (Zone toZone : zones) {
                double autoImpedance;
                float autoTravelTime = dataSet.getAutoTravelTimeFromTo(zone.getZoneId(), toZone.getZoneId());
                if (autoTravelTime == 0) {      // should never happen for auto
                    autoImpedance = 0;
                } else {
                    autoImpedance = Math.exp(beta * autoTravelTime);
                }
                double transitImpedance;
                float transitTravelTime = dataSet.getTransitTravelTimedFromTo(zone.getZoneId(), toZone.getZoneId());
                if (transitTravelTime == 0) {   // zone is not connected by walk-to-transit
                    transitImpedance = 0;
                } else {
                    transitImpedance = Math.exp(beta * transitTravelTime);
                }

                autoAccessibilityHouseholds += Math.pow(zone.getNumberOfHouseholds(), alpha) * autoImpedance;
                autoAccessibilityRetail += Math.pow(zone.getRetailEmpl(), alpha) * autoImpedance;
                autoAccessibilityOther += Math.pow(zone.getOtherEmpl(), alpha) * autoImpedance;
                transitAccessibilityOther += Math.pow(zone.getOtherEmpl(), alpha) * transitImpedance;
            }
            autoAccessibilityHouseholdsByZone.put(zone.getZoneId(), autoAccessibilityHouseholds);
            autoAccessibilityRetailByZone.put(zone.getZoneId(), autoAccessibilityRetail);
            autoAccessibilityOtherByZone.put(zone.getZoneId(), autoAccessibilityOther);
            transitAccessibilityOtherByZone.put(zone.getZoneId(), transitAccessibilityOther);
        }

        MitoUtil.scaleMap(autoAccessibilityHouseholdsByZone, 100);
        MitoUtil.scaleMap(autoAccessibilityRetailByZone, 100);
        MitoUtil.scaleMap(autoAccessibilityOtherByZone, 100);
        MitoUtil.scaleMap(transitAccessibilityOtherByZone, 100);

        for(Zone zone: dataSet.getZones().values()) {
            zone.setAutoAccessibilityHouseholds(autoAccessibilityHouseholdsByZone.get(zone.getZoneId()));
            zone.setAutoAccessibilityRetail(autoAccessibilityRetailByZone.get(zone.getZoneId()));
            zone.setAutoAccessibilityOther(autoAccessibilityOtherByZone.get(zone.getZoneId()));
            zone.setTransitAccessibilityOther(transitAccessibilityOtherByZone.get(zone.getZoneId()));
        }
    }

}
