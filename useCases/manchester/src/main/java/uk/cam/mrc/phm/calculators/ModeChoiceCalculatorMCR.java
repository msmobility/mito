package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.AbstractModeChoiceCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import jakarta.validation.constraints.Negative;
import org.matsim.core.utils.collections.Tuple;

import java.util.*;

import static de.tum.bgu.msm.data.Mode.*;

public class ModeChoiceCalculatorMCR extends AbstractModeChoiceCalculator {

    private final Map<Mode, Map<String, Double>> coef;

    public ModeChoiceCalculatorMCR(Purpose purpose, DataSet dataSet) {
        super();
        coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
        setNests();
    }

    private void setNests() {
        List<Tuple<EnumSet<Mode>, Double>> nests = new ArrayList<>();
        //TODO: currently the manchester mode choice model is MNL, no nests
        nests = null;
//        nests.add(new Tuple<>(EnumSet.of(autoDriver,autoPassenger), 0.25));
//        nests.add(new Tuple<>(EnumSet.of(train, tramOrMetro, bus, taxi), 0.25));
//        nests.add(new Tuple<>(EnumSet.of(walk,bicycle), 1.));
        super.setNests(nests);
    }

    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        int age = person.getAge();
        boolean male = person.getMitoGender().equals(MitoGender.MALE);
        int hhincome = household.getMonthlyIncome_EUR();

        EnumMap<Mode, Double> generalizedCosts = calculateGeneralizedCosts(purpose, household, person,
                originZone, destinationZone, travelTimes, travelDistanceAuto, travelDistanceNMT, peakHour_s);


        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);

        Set<Mode> availableModes = null;
        if(Resources.instance.getBoolean(Properties.RUN_MODESET,false)){
            availableModes = ((MitoPerson7days)person).getModeSet().getModesMNL();
        }else {
            availableModes = coef.keySet();
        }

        for (Mode mode : availableModes){
            final Map<String, Double> modeCoef = coef.get(mode);

            // Intercept
            double utility = modeCoef.get("asc");


            // Sex
            if (male) {
                utility += modeCoef.getOrDefault("male",0.);
            }

            // Age
            if(age>=5 & age <=14){
                utility += modeCoef.getOrDefault("age_5_14",0.);
            }

            if(age>=15 & age <=24){
                utility += modeCoef.getOrDefault("age_15_24",0.);
            }

            if(age>=40 & age <=69){
                utility += modeCoef.getOrDefault("age_40_69",0.);
            }

            if(age>=55){
                utility += modeCoef.getOrDefault("age_55_and_over",0.);
            }

            if(age>=70){
                utility += modeCoef.getOrDefault("age_70_and_over",0.);
            }


            // Household income
            if (hhincome < 1500) {
                utility += modeCoef.getOrDefault("income_low",0.);
            } else if (hhincome > 5000) {
                utility += modeCoef.getOrDefault("income_high",0.);
            }

            if(purpose.equals(Purpose.HBE)){
                utility += modeCoef.getOrDefault("education_trip",0.);
            }

            double gc = generalizedCosts.get(mode);
            utility += gc * modeCoef.get("cost");

            utilities.put(mode, utility);
        }

        return utilities;
    }

    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone,
                                                           MitoZone destinationZone, TravelTimes travelTimes,
                                                           double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        double timeAutoD = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "car") ; //min
        double timeAutoP = timeAutoD;

        double timePt = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "pt") ;//min

        //TODO, check Inf travel time, intrazonal or missing PT connection?
        if(timePt == Double.POSITIVE_INFINITY){
            timePt = 9999;
        }


        double gcWalk;
        double gcBicycle;

        if(Purpose.getMandatoryPurposes().contains(purpose)){
            gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkCommute");
            gcBicycle = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bikeCommute");
        }else{
            gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkDiscretionary");;
            gcBicycle = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bikeDiscretionary");
        }

        EnumMap<Mode, Double> generalizedCosts = new EnumMap<>(Mode.class);
        generalizedCosts.put(Mode.autoDriver, timeAutoD);
        generalizedCosts.put(Mode.autoPassenger, timeAutoP);
        generalizedCosts.put(Mode.pt, timePt);
        generalizedCosts.put(Mode.bicycle, gcBicycle);
        generalizedCosts.put(Mode.walk, gcWalk);
        return generalizedCosts;

    }

}
