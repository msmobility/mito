package de.tum.bgu.msm.modules.travelTimeBudget;

import de.tum.bgu.msm.data.AreaTypes;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.Purpose;
import org.apache.log4j.Logger;

class TravelTimeBudgetCalculatorImpl implements TravelTimeBudgetCalculator {

    private final static Logger logger = Logger.getLogger(TravelTimeBudgetCalculatorImpl.class);

    @Override
    public double calculateBudget(MitoHousehold household, String activityPurpose) {

        int retirees = DataSet.getRetireesForHousehold(household);
        int females = DataSet.getFemalesForHousehold(household);
        int youngAdults = DataSet.getYoungAdultsForHousehold(household);
        int workers = DataSet.getNumberOfWorkersForHousehold(household);
        int cars = household.getAutos();

        int HBW = household.getTripsForPurpose(Purpose.HBW).size();
        int HBE = household.getTripsForPurpose(Purpose.HBE).size();
        int HBS = household.getTripsForPurpose(Purpose.HBS).size();
        int HBO = household.getTripsForPurpose(Purpose.HBO).size();
        int NHBW = household.getTripsForPurpose(Purpose.NHBW).size();
        int NHBO = household.getTripsForPurpose(Purpose.NHBO).size();

        double intercept;
        double youngAdultsParam;
        double carsParam;
        double wbScale;
        double hbwParam;
        double hboParam;
        double hbeParam;
        double nhbwParam;
        double nhboParam;
        double hbsParam;
        double workersParam;
        double femalesParam;
        double retireesParam;

        int householdSize = household.getHhSize();

        double householdSizeParam;
        double economicStatusParam;
        int economicStatus = household.getEconomicStatus();
        double area;
        AreaTypes.SGType areaType = null;
        if(household.getHomeZone() != null) {
            areaType = household.getHomeZone().getAreaTypeSG();
        }


        if ("Total".equals(activityPurpose)) {
            intercept = 4.3818;
            youngAdultsParam = 0.051;
            carsParam = -0.0125;
            wbScale = 0.713;
            hbwParam = 0.0438;
            hboParam = 0.1314;
            hbeParam = 0.0459;
            nhbwParam = 0.0269;
            nhboParam = 0.1200;
            hbsParam = 0;
            workersParam = 0;
            femalesParam = 0;
            retireesParam = 0;

            if (householdSize == 2) {
                householdSizeParam = 0.4090;
            } else if (householdSize == 3) {
                householdSizeParam = 0.5487;
            } else if (householdSize == 4) {
                householdSizeParam = 0.6440;
            } else if (householdSize > 4) {
                householdSizeParam = 0.6913;
            } else {
                householdSizeParam = 0;
            }

            if (economicStatus == 4) {
                economicStatusParam = 0.0547;
            } else if (economicStatus == 5) {
                economicStatusParam = 0.1107;
            } else {
                economicStatusParam = 0;
            }


            if (areaType == AreaTypes.SGType.MEDIUM_SIZED_CITY) {
                area = -0.0878;
            } else if (areaType ==  AreaTypes.SGType.TOWN) {
                area = -0.0832;
            } else if (areaType ==  AreaTypes.SGType.RURAL) {
                area = -0.1061;
            } else if (areaType == null) {
                logger.warn("Unknown area type of household "
                        + household.getId()
                        + "'s homezone. Budget estimation might be wrong. " +
                        "(Check if household has dwelling)");
                area = 0;
            } else {
                area = 0;
            }

        } else if (activityPurpose == "HBO") {

            intercept = 4.0951;
            youngAdultsParam = 0.0885;
            workersParam = 0.0203;
            carsParam = -0.0225;
            wbScale = 0.899;
            hbwParam = -0.2404;
            hboParam = 0.2850;
            hbeParam = -0.1792;
            hbsParam = -0.1544;
            nhbwParam = -0.0770;
            nhboParam = -0.0175;
            femalesParam = 0;
            retireesParam = 0;

            if (householdSize == 2) {
                householdSizeParam = 0.2939;
            } else if (householdSize == 3) {
                householdSizeParam = 0.3022;
            } else if (householdSize == 4) {
                householdSizeParam = 0.3931;
            } else if (householdSize > 4) {
                householdSizeParam = 0.4893;
            } else {
                householdSizeParam = 0;
            }

            if (economicStatus == 5) {
                economicStatusParam = 0.0449;
            } else {
                economicStatusParam = 0;
            }

            if (areaType == AreaTypes.SGType.MEDIUM_SIZED_CITY) {
                area = -0.1474;
            } else if (areaType == AreaTypes.SGType.TOWN) {
                area = -0.1594;
            } else if (areaType == AreaTypes.SGType.RURAL) {
                area = -0.1133;
            } else if (areaType == null) {
                logger.warn("Unknown area type of household "
                        + household.getId()
                        + "'s homezone. Budget estimation might be wrong. " +
                        "(Check if household has dwelling)");
                area = 0;
            } else {
                area = 0;
            }

        } else if (activityPurpose == "HBS") {
            intercept = 2.751;
            youngAdultsParam = 0;
            workersParam = 0;
            carsParam = 0;
            wbScale = 0.814;
            hbwParam = -0.065;
            hboParam = -0.062;
            hbeParam = -0.046;
            hbsParam = 0.599;
            nhbwParam = -0.045;
            nhboParam = 0;
            femalesParam = 0.139;
            retireesParam = 0.118;

            if (householdSize == 3) {
                householdSizeParam = 0.080;
            } else {
                householdSizeParam = 0;
            }

            if (economicStatus == 2) {
                economicStatusParam = 0.142;
            } else if (economicStatus == 4) {
                economicStatusParam = -0.051;
            } else if (economicStatus == 5) {
                economicStatusParam = -0.065;
            } else {
                economicStatusParam = 0;
            }

            if (areaType == AreaTypes.SGType.TOWN) {
                area = -0.136;
            } else if (areaType == AreaTypes.SGType.RURAL) {
                area = 0.046;
            } else if (areaType == null) {
                logger.warn("Unknown area type of household "
                        + household.getId()
                        + "'s homezone. Budget estimation might be wrong. " +
                        "(Check if household has dwelling)");
                area = 0;
            } else {
                area = 0;
            }

        } else if (activityPurpose == "NHBW") {
            intercept = 2.921;
            youngAdultsParam = 0.075;
            workersParam = 0;
            carsParam = 0;
            wbScale = 0.812;
            hbwParam = -0.155;
            hboParam = -0.021;
            hbeParam = 0;
            hbsParam = -0.067;
            nhbwParam = 0.317;
            nhboParam = 0;
            femalesParam = -0.09;
            retireesParam = 0;
            area = 0;

            if (householdSize == 2) {
                householdSizeParam = 0.225;
            } else if (householdSize == 3) {
                householdSizeParam = 0.308;
            } else if (householdSize == 4) {
                householdSizeParam = 0.420;
            } else if (householdSize >= 5) {
                householdSizeParam = 0.441;
            } else {
                householdSizeParam = 0;
            }

            if (economicStatus == 4) {
                economicStatusParam = 0.13;
            } else if (economicStatus == 5) {
                economicStatusParam = 0.19;
            } else {
                economicStatusParam = 0;
            }

        } else if (activityPurpose == "NHBO") {
            intercept = 3.45457;
            youngAdultsParam = 0;
            workersParam = 0.04670;
            carsParam = 0;
            wbScale = 1.01;
            hbwParam = -0.24295;
            hboParam = -0.07980;
            hbeParam = -0.14712;
            hbsParam = -0.15595;
            nhbwParam = -0.08459;
            nhboParam = 0.27826;
            femalesParam = 0;
            retireesParam = 0;

            if (householdSize == 2) {
                householdSizeParam = 0.27228;
            } else if (householdSize == 3) {
                householdSizeParam = 0.35532;
            } else if (householdSize == 4) {
                householdSizeParam = 0.52997;
            } else if (householdSize >= 5) {
                householdSizeParam = 0.70424;
            } else {
                householdSizeParam = 0;
            }

            if (economicStatus == 4) {
                economicStatusParam = 0.06283;
            } else if (economicStatus == 5) {
                economicStatusParam = 0.09628;
            } else {
                economicStatusParam = 0;
            }

            if (areaType == AreaTypes.SGType.MEDIUM_SIZED_CITY) {
                area = -0.05425;
            } else if (areaType == AreaTypes.SGType.RURAL) {
                area = -0.13383;
            } else if (areaType == null) {
                logger.warn("Unknown area type of household "
                        + household.getId()
                        + "'s homezone. Budget estimation might be wrong. " +
                        "(Check if household has dwelling)");
                area = 0;
            } else {
                area = 0;
            }

        } else if (activityPurpose == "HBR") {
            return 0;
        } else {
            throw new RuntimeException("Undefined activityPurpose given!");
        }

        double femalesImpact = females * femalesParam;
        double youngAdultsImpact = youngAdults * youngAdultsParam;
        double retireesImpact = retirees * retireesParam;
        double workersImpact = workers * workersParam;
        double carsImpact = cars * carsParam;
        double hbwImpact = HBW * hbwParam;
        double hbeImpact = HBE * hbeParam;
        double hbsImpact = HBS * hbsParam;
        double hboImpact = HBO * hboParam;
        double nhbwImpact = NHBW * nhbwParam;
        double nhboImpact = NHBO * nhboParam;


        double utility = intercept + householdSizeParam + area + femalesImpact + economicStatusParam +
                youngAdultsImpact + retireesImpact + workersImpact + carsImpact +
                hbwImpact + hbeImpact + hbsImpact + hboImpact + nhbwImpact + nhboImpact;

        return Math.pow(-Math.log(0.5), 1. / wbScale) * Math.exp(utility);
    }
}
