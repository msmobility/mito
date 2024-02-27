package de.tum.bgu.msm.util;

import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class LogitTools<E extends Enum<E>> {

    private static final Logger logger = Logger.getLogger(LogitTools.class);
    private final Class<E> enumClass;

    public LogitTools(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    // Not currently used
    public List<Tuple<EnumSet<E>, Double>> identifyNests(EnumMap<E, Map<String, Double>> coefficients) {

        Map<Integer,Tuple<EnumSet<E>, Double>> nests = new HashMap<>();

        if(!coefficients.entrySet().iterator().next().getValue().containsKey("nestingCoefficient")) {
            logger.info("Identified Multinomial Logit.");
            return null;
        } else {
            for(E option : coefficients.keySet()) {
                int nestCode = coefficients.get(option).get("nest").intValue();
                if(nests.keySet().contains(nestCode)) {
                    nests.get(nestCode).getFirst().add(option);
                } else {
                    nests.put(nestCode, new Tuple<>(EnumSet.of(option),coefficients.get(option).get("nestingCoefficient")));
                }
            }

            logger.info("Identified Nested Logit. Nests:");
            for(int code : nests.keySet()) {
                logger.info("Nest " + code + ": " + nests.get(code).getFirst().stream().map(Enum::toString).collect(Collectors.joining(",")) +
                        " | Coefficient: " + nests.get(code).getSecond());
            }

            return nests.values().stream().collect(Collectors.toList());
        }
    }

    public EnumMap<E, Double> getProbabilitiesMNL(EnumMap<E, Double> utilities) {

        EnumMap<E, Double> probabilities = new EnumMap<>(enumClass);
        EnumMap<E, Double> expUtils = new EnumMap<>(enumClass);
        double expUtilsSum = 0.;

        for(E option : utilities.keySet()) {
            double expUtil = Math.exp(utilities.get(option));
            expUtils.put(option, expUtil);
            expUtilsSum += expUtil;
        }

        for(E option : utilities.keySet()) {
            probabilities.put(option, expUtils.get(option) / expUtilsSum);
        }

        return probabilities;
    }

    public EnumMap<E, Double> getProbabilitiesNL(EnumMap<E, Double> utilities, List<Tuple<EnumSet<E>, Double>> nests) {

        EnumMap<E, Double> expOptionUtils = new EnumMap(enumClass);
        EnumMap<E, Double> expNestSums = new EnumMap(enumClass);
        EnumMap<E, Double> expNestUtils = new EnumMap(enumClass);
        EnumMap<E, Double> probabilities = new EnumMap(enumClass);
        double expSumRoot = 0;

        for (Tuple<EnumSet<E>, Double> nest : nests) {
            double expNestSum = 0;
            EnumSet<E> nestOptions = EnumSet.copyOf(nest.getFirst());
            nestOptions.retainAll(utilities.keySet());
            double nestingCoefficient = nest.getSecond();
            for (E option : nestOptions) {
                double expOptionUtil = Math.exp(utilities.get(option) / nestingCoefficient);
                expOptionUtils.put(option, expOptionUtil);
                expNestSum += expOptionUtil;
            }
            double expNestUtil = Math.exp(nestingCoefficient * Math.log(expNestSum));
            for (E option : nestOptions) {
                expNestSums.put(option, expNestSum);
                expNestUtils.put(option, expNestUtil);
            }
            expSumRoot += expNestUtil;
        }

        for (E option : utilities.keySet()) {
            if (expNestSums.get(option) == 0) {
                probabilities.put(option, 0.);
            } else {
                probabilities.put(option, (expOptionUtils.get(option) * expNestUtils.get(option)) / (expNestSums.get(option) * expSumRoot));
            }
        }
        return probabilities;
    }

    public EnumMap<E, Double> getProbabilities(EnumMap<E, Double> utilities, List<Tuple<EnumSet<E>, Double>> nests) {

        if(nests == null) {
            return getProbabilitiesMNL(utilities);
        } else {
            return getProbabilitiesNL(utilities, nests);
        }
    }

    public double getLogsumMNL(EnumMap<E, Double> utilities, double scaleParameter) {
        double expSum = 0.;
        for(E option : utilities.keySet()) {
            double expUtil = Math.exp(utilities.get(option) * scaleParameter);
            expSum += expUtil;
        }
        return(Math.log(expSum) / scaleParameter);
    }

    public double getLogsumNL(EnumMap<E, Double> utilities, List<Tuple<EnumSet<E>, Double>> nests, double scaleParameter) {
        double expSumRoot = 0.;
        for(Tuple<EnumSet<E>,Double> nest : nests) {
            double expNestSum = 0;
            EnumSet<E> nestOptions = EnumSet.copyOf(nest.getFirst());
            nestOptions.retainAll(utilities.keySet());
            double nestScaleParameter = scaleParameter / nest.getSecond();
            for(E option : nestOptions) {
                double expOptionUtil = Math.exp(utilities.get(option) * nestScaleParameter);
                expNestSum += expOptionUtil;
            }
            double expNestUtil = Math.exp(scaleParameter * Math.log(expNestSum) / nestScaleParameter);
            expSumRoot += expNestUtil;
        }
        return(Math.log(expSumRoot) / scaleParameter);
    }
}
