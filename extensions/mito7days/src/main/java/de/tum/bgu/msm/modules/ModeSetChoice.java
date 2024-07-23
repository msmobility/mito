package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.ModeSetCoefficientReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ModeSetChoice extends Module {

    private final static Logger logger = Logger.getLogger(ModeSetChoice.class);
    private final static Path modeSetCoefficientsPath = Resources.instance.getModeSetCoefficients();
    private final static Path modeSetConstantsPath = Resources.instance.getModeSetConstants();
    private final static String[] MODES = {"auto", "publicTransport", "walk", "bicycle"};

    private final Map<String, Map<String, Double>> coefficients = new HashMap<>();
    private final EnumMap<ModeSet, Double> constants = new EnumMap<>(ModeSet.class);
    private int nonMobilePersons;
    private final ModeSetCalculator modeSetCalculator;

    public ModeSetChoice(DataSet dataSet, List<Purpose> purposes, ModeSetCalculator modeSetCalculator) {
        super(dataSet, purposes);
        this.modeSetCalculator = modeSetCalculator;
        Map<String, Double> constantsAsStrings = new ModeSetCoefficientReader(dataSet, "constant", modeSetConstantsPath).readCoefficients();
        for (ModeSet modeSet : ModeSet.values()) {
            constants.put(modeSet, constantsAsStrings.get(modeSet.toString()));
        }
        for(String mode : MODES) {
            coefficients.put(mode, new ModeSetCoefficientReader(dataSet, mode, modeSetCoefficientsPath).readCoefficients());
        }
    }

    @Override
    public void run() {
        logger.info(" Calculating mode set.");
        modeSet();
        printModeSetShares();
    }

    private void modeSet() {

        for (MitoPerson person : dataSet.getModelledPersons().values()) {
            if(person.getTrips().size() == 0) {
                nonMobilePersons++;
            } else {
                chooseModeSet(person, modeSetCalculator.calculateUtilities(person, coefficients, constants));
            }
        }
        logger.info(nonMobilePersons + " non-mobile persons skipped");
    }



    private void chooseModeSet(MitoPerson person, EnumMap<ModeSet, Double> probabilities) {
        double sum = MitoUtil.getSum(probabilities.values());
        if (Math.abs(sum - 1) > 0.1) {
            logger.warn("Mode probabilities don't add to 1 for person " + person.getId());
        }
        if (sum > 0) {
            final ModeSet select = MitoUtil.select(probabilities, MitoUtil.getRandomObject());
            ((MitoPerson7days)person).setModeSet(select);
        } else {
            logger.error("Zero/negative probabilities for person " + person.getId());
            ((MitoPerson7days)person).setModeSet(null);
        }
    }


    private void printModeSetShares() {
        Map<ModeSet,Double> modeSetShare = new HashMap<>();

        //filter valid persons
        List<MitoPerson> persons = dataSet.getPersons().values().stream()
                .filter(p -> ((MitoPerson7days)p).getModeSet() != null).
                collect(Collectors.toList());

        final long totalPersons = persons.size();
        persons.stream()
                .map(person -> (MitoPerson7days) person)
                // Group number of persons by mode set
                .collect(Collectors.groupingBy(MitoPerson7days::getModeSet, Collectors.counting()))
                //calculate and add share to data set table
                .forEach((modeset, count) ->
                        modeSetShare.put(modeset, (double) count / totalPersons));


        logger.info("#################################################");
        logger.info("Mode set share :");
        for (ModeSet modeSet : ModeSet.values()) {
            Double share = modeSetShare.get(modeSet);
            if (share != null) {
                logger.info(modeSet + " = " + share * 100 + "%");
            }
        }




    }
}