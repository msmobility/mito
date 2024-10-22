package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.AbstractModeChoiceCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import uk.cam.mrc.phm.io.OmxSkimsReaderMCR;

import java.util.*;

import static de.tum.bgu.msm.data.Mode.*;

public class ModeChoiceCalculatorMCR extends AbstractModeChoiceCalculator {

    private static final Logger LOGGER = Logger.getLogger(ModeChoiceCalculatorMCR.class);
    private final Map<Mode, Map<String, Double>> coef;
    private final DataSet dataSet;

    public ModeChoiceCalculatorMCR(Purpose purpose, DataSet dataSet) {
        super();
        coef = new ModeChoiceCoefficientReader(dataSet, purpose, Resources.instance.getModeChoiceCoefficients(purpose)).readCoefficientsForThisPurpose();
        setNests();
        this.dataSet = dataSet;
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

            if (!male) {
                utility += modeCoef.getOrDefault("female",0.);
            }

            // Age
            if(age>=5 & age <=14){
                utility += modeCoef.getOrDefault("age_5_14",0.);
            }

            if(age>=15 & age <=24){
                utility += modeCoef.getOrDefault("age_15_24",0.);
            }

            if(age>=5 & age <=24){
                utility += modeCoef.getOrDefault("age_5_24",0.);
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

            // occupation
            if (MitoOccupationStatus.WORKER.equals(person.getMitoOccupationStatus())){
                utility += modeCoef.getOrDefault("occupation_worker",0.);
            }

            // Household income
            if (hhincome < 1500) {
                utility += modeCoef.getOrDefault("income_low",0.);
            } else if (hhincome > 5000) {
                utility += modeCoef.getOrDefault("income_high",0.);
            }

            // Household car
            switch (household.getAutos()){
                case 0:
                    utility += modeCoef.getOrDefault("car_0",0.);
                    break;
                case 2:
                    utility += modeCoef.getOrDefault("car_2",0.);
                    break;
                case 3:
                    utility += modeCoef.getOrDefault("car_3",0.);
                    break;
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


        double gcWalk = 0;
        double gcBicycle = 0;

        //TODO: no bike time skim for HBA, NHBO, NHBW, so currently use dist skim/constant speed 4.932 m/s
        //TODO: no walk time skim for NHBW, so currently use dist skim/constant speed 1.381 m/s
        switch (purpose){
            case HBW:
            case HBE:
                gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkCommute");
                gcBicycle = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bikeCommute");
                break;
            case HBO:
            case HBS:
            case HBR:
                gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkDiscretionary");;
                gcBicycle = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "bikeDiscretionary");
                break;
            case HBA:
                gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkHBA");;
                gcBicycle = ((DataSetImpl)dataSet).getTravelDistancesBike().getTravelDistance(originZone.getZoneId(), destinationZone.getZoneId()) * 1000. / 4.932 / 60; //dist (m) /speed (m/s) /60 --> time in min
                break;
            case NHBO:
                gcWalk = travelTimes.getTravelTime(originZone, destinationZone, peakHour_s, "walkNHBO");;
                gcBicycle = ((DataSetImpl)dataSet).getTravelDistancesBike().getTravelDistance(originZone.getZoneId(), destinationZone.getZoneId()) * 1000. / 4.932 / 60;
                break;
            case NHBW:
                gcWalk = ((DataSetImpl)dataSet).getTravelDistancesWalk().getTravelDistance(originZone.getZoneId(), destinationZone.getZoneId()) * 1000. / 1.381 / 60;
                gcBicycle = ((DataSetImpl)dataSet).getTravelDistancesBike().getTravelDistance(originZone.getZoneId(), destinationZone.getZoneId()) * 1000. / 4.932 / 60;
                break;
            default:
                LOGGER.error("Unknown purpose " + purpose);
        }


        EnumMap<Mode, Double> generalizedCosts = new EnumMap<>(Mode.class);
        generalizedCosts.put(autoDriver, timeAutoD);
        generalizedCosts.put(autoPassenger, timeAutoP);
        generalizedCosts.put(pt, timePt);
        generalizedCosts.put(bicycle, gcBicycle);
        generalizedCosts.put(walk, gcWalk);
        return generalizedCosts;

    }

}
