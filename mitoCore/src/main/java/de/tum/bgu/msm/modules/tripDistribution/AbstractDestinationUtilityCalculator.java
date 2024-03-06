package de.tum.bgu.msm.modules.tripDistribution;

import de.tum.bgu.msm.data.MitoPerson;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractDestinationUtilityCalculator {

    protected double[] distanceParams;
    protected double impedanceParam;
    protected double attractionParam = 1.;

    public double calculateUtility(double attraction, double travelDistance, int index) {
        if(attraction == 0) {
            return 0.;
        }
        double impedance = impedanceParam * Math.exp(distanceParams[index] * travelDistance);
        return Math.exp(impedance) * Math.pow(attraction,attractionParam);
    }

    public void adjustDistanceParams(double[] adjustment, Logger logger) {
        assert (adjustment.length == distanceParams.length);
        for(int i = 0 ; i < adjustment.length ; i++) {
            distanceParams[i] *= adjustment[i];
        }
        logger.info("Adjusted distance parameters. New parameters: " + Arrays.toString(distanceParams));
    }

    public List<Predicate<MitoPerson>> getCategories() {
        List<Predicate<MitoPerson>> all = new ArrayList<>();
        all.add(person -> true);
        return all;
    }
}
