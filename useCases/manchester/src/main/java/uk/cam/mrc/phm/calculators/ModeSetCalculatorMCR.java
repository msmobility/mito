package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.ModeSetCalculator;
import de.tum.bgu.msm.util.LogitTools;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static de.tum.bgu.msm.data.Purpose.HBE;
import static de.tum.bgu.msm.data.Purpose.HBW;

public class ModeSetCalculatorMCR implements ModeSetCalculator {

    private static final LogitTools<ModeSet> logitTools = new LogitTools<>(ModeSet.class);

    private final DataSet dataSet;

    public ModeSetCalculatorMCR(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public EnumMap<ModeSet, Double> calculateUtilities(MitoPerson person, Map<String, Map<String, Double>> coefficients, EnumMap<ModeSet, Double> constants) {

        EnumMap<ModeSet, Double> utilities = new EnumMap<>(ModeSet.class);
        EnumSet<ModeSet> availableChoices = EnumSet.allOf(ModeSet.class);
        if(person.hasTripsForPurpose(Purpose.RRT)) {
            availableChoices.removeAll(EnumSet.of(ModeSet.Auto, ModeSet.AutoPt, ModeSet.Pt));
        }

        for(ModeSet modeSet : availableChoices) {
            double utility = constants.get(modeSet);
            String modeSetString = modeSet.toString();
            if(modeSetString.contains("Auto"))  utility += getPredictor(person, coefficients.get("auto"));
            if(modeSetString.contains("Pt"))    utility += getPredictor(person, coefficients.get("publicTransport"));
            if(modeSetString.contains("Cycle")) utility += getPredictor(person, coefficients.get("bicycle"));
            if(modeSetString.contains("Walk"))  utility += getPredictor(person, coefficients.get("walk"));
            utilities.put(modeSet, utility);
        }

        return logitTools.getProbabilitiesMNL(utilities);
    }

    @Override
    public double getPredictor(MitoPerson pp, Map<String, Double> coefficients) {
        double predictor = 0.;
        MitoHousehold hh = pp.getHousehold();

        // Number of adults and children
        int householdChildrenUnder15 = hh.getChildrenUnderAgeForHousehold(15);
        int householdAdults = hh.getHhSize() - hh.getChildrenForHousehold();

        // Intercept
        predictor += coefficients.get("INTERCEPT");


        // Number of children under 15 in household

        predictor += householdChildrenUnder15 * coefficients.get("hh.childrenUnder15");


        // Autos
        int householdAutos = hh.getAutos();
        if (householdAutos == 0) {
            predictor += coefficients.get("hh.cars_0");
        } else if (householdAutos == 2) {
            predictor += coefficients.get("hh.cars_2");
        } else if (householdAutos >= 3) {
            predictor += coefficients.get("hh.cars_3");
        }

        // Autos per adult
        double autosPerAdult = Math.min((double) householdAutos / householdAdults , 1.);
        predictor += autosPerAdult * coefficients.get("hh.carPerAdult");

        // Age
        int age = pp.getAge();
        if (age >= 5 & age <= 14) {
            predictor += coefficients.get("p.age_group_5_14");
        }
        else if (age >= 15 & age <= 24) {
            predictor += coefficients.get("p.age_group_15_24");
        }
        else if (age >= 25 & age <= 34) {
            predictor += coefficients.get("p.age_group_25_34");
        }
        else if (age >= 35 & age <= 44) {
            predictor += coefficients.get("p.age_group_35_44");
        }
        else if (age >= 45 &age <= 64) {
            predictor += 0.;
        }else if (age >= 65 & age <= 74) {
            predictor += coefficients.get("p.age_group_65_74");
        }
        else if (age >= 75){
            predictor += coefficients.get("p.age_group_75");
        }

        // Female
        if(pp.getMitoGender().equals(MitoGender.FEMALE)) {
            predictor += coefficients.get("p.female");
        }


        // Mito occupation Status
        MitoOccupationStatus occupationStatus = pp.getMitoOccupationStatus();
        if (occupationStatus.equals(MitoOccupationStatus.STUDENT)) {
            predictor += coefficients.get("p.occupation_student");
        } else if (occupationStatus.equals(MitoOccupationStatus.WORKER)) {
            predictor += coefficients.get("p.occupation_worker");
        }


        return predictor;
    }


}
