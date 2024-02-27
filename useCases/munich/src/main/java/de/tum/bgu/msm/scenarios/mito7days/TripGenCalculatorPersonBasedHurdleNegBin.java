package de.tum.bgu.msm.scenarios.mito7days;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripGeneration.TripGenPredictor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripGenCalculatorPersonBasedHurdleNegBin implements TripGenPredictor {

    private final DataSet dataSet;

    public TripGenCalculatorPersonBasedHurdleNegBin(DataSet dataSet) {
        this.dataSet = dataSet;
    }


    @Override
    public double getPredictor(MitoHousehold hh,MitoPerson pp, Map<String, Double> coefficients) {
        double predictor = 0.;

        // Intercept
        predictor += coefficients.get("(Intercept)");

        // Household size
        int householdSize = hh.getHhSize();
        if(householdSize == 1) {
            predictor += coefficients.get("hh.size_1");
        }
        else if(householdSize == 2) {
            predictor += coefficients.get("hh.size_2");
        }
        else if(householdSize == 3) {
            predictor += coefficients.get("hh.size_3");
        }
        else if(householdSize == 4) {
            predictor += coefficients.get("hh.size_4");
        }
        else  {
            assert(householdSize >= 5);
            predictor += coefficients.get("hh.size_5");
        }

        // Number of children in household
        int householdChildren = DataSet.getChildrenForHousehold(hh);
        if(householdChildren == 1) {
            predictor += coefficients.get("hh.children_1");
        }
        else if (householdChildren == 2) {
            predictor += coefficients.get("hh.children_2");
        }
        else if (householdChildren >= 3) {
            predictor += coefficients.get("hh.children_3");
        }

        // Household in urban region
        if(!(hh.getHomeZone().getAreaTypeR().equals(AreaTypes.RType.RURAL))) {
            predictor += coefficients.get("hh.urban");
        }

        // Household autos
        int householdAutos = hh.getAutos();
        if(householdAutos == 1) {
            predictor += coefficients.get("hh.cars_1");
        }
        else if(householdAutos == 2) {
            predictor += coefficients.get("hh.cars_2");
        }
        else if(householdAutos >= 3) {
            predictor += coefficients.get("hh.cars_3");
        }

        // Autos per adult
        int householdAdults = householdSize - householdChildren;
        double autosPerAdult = Math.min((double) hh.getAutos() / (double) householdAdults , 1.0);
        predictor += autosPerAdult * coefficients.get("hh.autosPerAdult");

        // Age
        int age = pp.getAge();
        if (age <= 18) {
            predictor += coefficients.get("p.age_gr_1");
        }
        else if (age <= 29) {
            predictor += coefficients.get("p.age_gr_2");
        }
        else if (age <= 49) {
            predictor += coefficients.get("p.age_gr_3");
        }
        else if (age <= 59) {
            predictor += coefficients.get("p.age_gr_4");
        }
        else if (age <= 69) {
            predictor += coefficients.get("p.age_gr_5");
        }
        else {
            predictor += coefficients.get("p.age_gr_6");
        }

        // Female
        if (pp.getMitoGender().equals(MitoGender.FEMALE)) {
            predictor += coefficients.get("p.female");
        }

        // Has drivers Licence
        if (pp.hasDriversLicense()) {
            predictor += coefficients.get("p.driversLicense");
        }

        // Has bicycle
        if (pp.getHasBicycle().get()) {
            predictor += coefficients.get("p.ownBicycle");
        }

        // Mito occupation Status
        MitoOccupationStatus occupationStatus = pp.getMitoOccupationStatus();
        if (occupationStatus.equals(MitoOccupationStatus.STUDENT)) {
            predictor += coefficients.get("p.occupationStatus_Student");
        } else if (occupationStatus.equals(MitoOccupationStatus.UNEMPLOYED)) {
            predictor += coefficients.get("p.occupationStatus_Unemployed");
        }

        // Work trips & mean distance
        List<MitoTrip> workTrips = pp.getTrips().stream().filter(tt -> Purpose.HBW.equals(tt.getTripPurpose())).collect(Collectors.toList());
        int workTripCount = workTrips.size();
        if(workTripCount > 0) {
            if (workTripCount == 1) {
                predictor += coefficients.get("p.workTrips_1");
            } else if (workTripCount == 2) {
                predictor += coefficients.get("p.workTrips_2");
            } else if (workTripCount == 3) {
                predictor += coefficients.get("p.workTrips_3");
            } else if (workTripCount == 4) {
                predictor += coefficients.get("p.workTrips_4");
            } else {
                predictor += coefficients.get("p.workTrips_5");
            }
            int homeZoneId = pp.getHousehold().getZoneId();
            double meanWorkKm = workTrips.stream().
                    mapToDouble(t -> dataSet.getTravelDistancesNMT().
                            getTravelDistance(homeZoneId, t.getTripDestination().getZoneId())).average().getAsDouble();
            predictor += Math.log(meanWorkKm) * coefficients.get("p.log_km_mean_HBW");
        }

        // Education trips & mean distance
        List<MitoTrip> eduTrips = pp.getTrips().stream().filter(tt -> Purpose.HBE.equals(tt.getTripPurpose())).collect(Collectors.toList());
        int eduTripCount = eduTrips.size();
        if(eduTripCount > 0) {
            if (eduTripCount == 1) {
                predictor += coefficients.get("p.eduTrips_1");
            } else if (eduTripCount == 2) {
                predictor += coefficients.get("p.eduTrips_2");
            } else if (eduTripCount == 3) {
                predictor += coefficients.get("p.eduTrips_3");
            } else if (eduTripCount == 4) {
                predictor += coefficients.get("p.eduTrips_4");
            } else {
                predictor += coefficients.get("p.eduTrips_5");
            }
            int homeZoneId = pp.getHousehold().getZoneId();
            double meanWorkKm = eduTrips.stream().
                    mapToDouble(t -> dataSet.getTravelDistancesNMT().
                            getTravelDistance(homeZoneId, t.getTripDestination().getZoneId())).average().getAsDouble();
            predictor += Math.log(meanWorkKm) * coefficients.get("p.log_km_mean_HBW");
        }

        return predictor;
    }



}
