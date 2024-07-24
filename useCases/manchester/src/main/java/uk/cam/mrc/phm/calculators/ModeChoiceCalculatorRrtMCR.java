package uk.cam.mrc.phm.calculators;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.ModeChoiceCoefficientReader;
import de.tum.bgu.msm.modules.modeChoice.AbstractModeChoiceCalculator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static de.tum.bgu.msm.data.Mode.*;
import static de.tum.bgu.msm.data.Purpose.RRT;

public class ModeChoiceCalculatorRrtMCR extends AbstractModeChoiceCalculator {

    private final static Logger logger = Logger.getLogger(ModeChoiceCalculatorRrtMCR.class);
    private final Map<Mode, Map<String, Double>> coef;

    public ModeChoiceCalculatorRrtMCR(DataSet dataSet) {
        super();
        coef = new ModeChoiceCoefficientReader(dataSet, RRT, Resources.instance.getModeChoiceCoefficients(RRT)).readCoefficientsForThisPurpose();
    }

    @Override
    public EnumMap<Mode, Double> calculateUtilities(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {

        EnumMap<Mode, Double> utilities = new EnumMap<>(Mode.class);

        Set<Mode> availableChoices;
        if(Resources.instance.getBoolean(Properties.RUN_MODESET,false)){
            availableChoices = ((MitoPerson7days)person).getModeSet().getModesMNL();
        }else {
            availableChoices = coef.keySet();
        }

        assert availableChoices != null;
        availableChoices.removeAll(EnumSet.of(autoDriver, autoPassenger, pt));
        for (Mode mode : availableChoices){
            final Map<String, Double> modeCoef = coef.get(mode);

            // Intercept
            double utility = modeCoef.get("INTERCEPT");

            // Household in urban region
            if(!(household.getHomeZone().getAreaTypeR().equals(AreaTypes.RType.RURAL))) {
                utility += modeCoef.get("hh.urban");
            }

            // Household size
            int hhSize = household.getHhSize();
            if (hhSize == 2) {
                utility += modeCoef.get("hh.size_2");
            } else if (hhSize == 3) {
                utility += modeCoef.get("hh.size_3");
            } else if (hhSize == 4) {
                utility += modeCoef.get("hh.size_4");
            } else if (hhSize >= 5) {
                utility += modeCoef.get("hh.size_5");
            }

            // Age
            int age = person.getAge();
            if (age <= 18) {
                utility += modeCoef.get("p.age_gr_1");
            } else if (age <= 59) {
                utility += 0.;
            } else if (age <= 69) {
                utility += modeCoef.get("p.age_gr_5");
            } else {
                utility += modeCoef.get("p.age_gr_6");
            }

            // Sex
            if(person.getMitoGender().equals(MitoGender.FEMALE)) {
                utility += modeCoef.get("p.female");
            }

            // Distance
            if(travelDistanceNMT == 0) {
                logger.info("0 trip distance for RRT trip");
            } else {
                utility += Math.log(travelDistanceNMT) * modeCoef.get("t.distance_T");
            }

            utilities.put(mode, utility);
        }

        return utilities;
    }

    @Override
    public EnumMap<Mode, Double> calculateGeneralizedCosts(Purpose purpose, MitoHousehold household, MitoPerson person, MitoZone originZone, MitoZone destinationZone, TravelTimes travelTimes, double travelDistanceAuto, double travelDistanceNMT, double peakHour_s) {
        return null;
    }
}
