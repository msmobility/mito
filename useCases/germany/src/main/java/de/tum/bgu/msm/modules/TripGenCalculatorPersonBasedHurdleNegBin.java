package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripGeneration.TripGenPredictor;
import de.tum.bgu.msm.resources.Properties;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripGenCalculatorPersonBasedHurdleNegBin implements TripGenPredictor {
    private static final Logger logger = Logger.getLogger(TripGenCalculatorPersonBasedHurdleNegBin.class);
    private double speed_bicycle_m_min = Properties.SPEED_BICYCLE_M_MIN;
    private double speed_walk_m_min = Properties.SPEED_WALK_M_MIN;

    private final DataSet dataSet;

    public TripGenCalculatorPersonBasedHurdleNegBin(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    @Override
    public double getPredictor(MitoHousehold hh, MitoPerson person, Map<String, Double> coefficients) {
        double predictor = coefficients.get("(Intercept)");
        int size = Math.min(hh.getHhSize(), 5);
        switch (size) {
            case 1:
                predictor += coefficients.get("hh.size_1");
                break;
            case 2:
                predictor += coefficients.get("hh.size_2");
                break;
            case 3:
                predictor += coefficients.get("hh.size_3");
                break;
            case 4:
                predictor += coefficients.get("hh.size_4");
                break;
            case 5:
                predictor += coefficients.get("hh.size_5");
                break;
            default:
                predictor += coefficients.get("hh.size_5");
                break;
        }
        int children = 0;
        int adults = 0;

        for (MitoPerson p : hh.getPersons().values()) {
            if (p.getAge() < 18) {
                children++;
            } else {
                adults++;
            }
        }

        switch (children) {
            case 0:
                break;
            case 1:
                predictor += coefficients.get("hh.children_1");
                break;
            case 2:
                predictor += coefficients.get("hh.children_2");
                break;
            case 3:
                predictor += coefficients.get("hh.children_3");
                break;
            default:
                predictor += coefficients.get("hh.children_3");
                break;

        }

        int cars = hh.getAutos();

        switch (cars) {
            case 0:
                predictor += 0;
                break;
            case 1:
                predictor += coefficients.get("hh.cars_1");
                break;
            case 2:
                predictor += coefficients.get("hh.cars_2");
                break;
            case 3:
                predictor += coefficients.get("hh.cars_3");
                break;
            default:
                predictor += coefficients.get("hh.cars_3");
                break;
        }

        double carsPerAdult = Math.min((double) cars / ((double) adults), 1.0);
        predictor += coefficients.get("hh.carsPerAdult") * carsPerAdult;

        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus) {
            case 1:
                predictor += coefficients.get("hh.econStatus_1");
                break;
            case 2:
                predictor += coefficients.get("hh.econStatus_2");
                break;
            case 3:
                predictor += coefficients.get("hh.econStatus_3");
                break;
            case 4:
                predictor += coefficients.get("hh.econStatus_4");
                break;
            case 5:
                predictor += coefficients.get("hh.econStatus_5");
                break;
            default:
                throw new RuntimeException("Economic status cannot be zero?");
        }

        AreaTypes.SGType type = hh.getHomeZone().getAreaTypeSG();

        //is this right area type?
        switch (type) {
            case CORE_CITY:
                predictor += 0.;
                break;
            case MEDIUM_SIZED_CITY:
                predictor += coefficients.get("hh.BBSR_2");
                break;
            case TOWN:
                predictor += coefficients.get("hh.BBSR_3");
                break;
            case RURAL:
                predictor += coefficients.get("hh.BBSR_4");
                break;
        }

        int age = person.getAge();
        if (age < 19) {
            predictor += coefficients.get("p.age_gr_1");
        } else if (age < 30) {
            predictor += coefficients.get("p.age_gr_2");
        } else if (age < 50) {
            predictor += coefficients.get("p.age_gr_3");
        } else if (age < 60) {
            predictor += coefficients.get("p.age_gr_4");
        } else if (age < 70) {
            predictor += coefficients.get("p.age_gr_5");
        } else {
            predictor += coefficients.get("p.age_gr_6");
        }

        if (person.getMitoGender().equals(MitoGender.FEMALE)) {
            predictor += coefficients.get("p.female");
        }

        if (person.hasDriversLicense()) {
            predictor += coefficients.get("p.driversLicense");
        }

        switch (person.getMitoOccupationStatus()) {
            case WORKER:
                predictor += coefficients.get("p.occupation_worker");
                break;
            case STUDENT:
                predictor += coefficients.get("p.occupation_student");
                break;
            case UNEMPLOYED:
            case RETIRED:
                predictor += coefficients.get("p.occupation_unemployed");
                break;
        }

        Map<String, Double> timesHBW;
        Map<String, Double> timesHBE;

        List<MitoTrip> tripsForHBW = new ArrayList<>();
        List<MitoTrip> tripsForHBE = new ArrayList<>();
        person.getTrips().forEach(trip -> {
            if (trip.getTripPurpose().equals(Purpose.HBW)){
                tripsForHBW.add(trip);
            } else if (trip.getTripPurpose().equals(Purpose.HBE)){
                tripsForHBE.add(trip);
            }
        });
        timesHBW = getTimesByModeForPurpose(tripsForHBW);
        timesHBE = getTimesByModeForPurpose(tripsForHBE);


        for (String mode : timesHBW.keySet()) {
            if (mode.equals("car")) {
                predictor += coefficients.get("p.isMobile_HBW_car");
                predictor += coefficients.get("p.sqrtTTB_HBW_car") * Math.sqrt(timesHBW.get("car"));
            } else if (mode.equals("PT")) {
                predictor += coefficients.get("p.isMobile_HBW_PT");
                predictor += coefficients.get("p.sqrtTTB_HBW_PT") * Math.sqrt(timesHBW.get("PT"));
            } else if (mode.equals("cycle")) {
                predictor += coefficients.get("p.isMobile_HBW_cycle");
                predictor += coefficients.get("p.sqrtTTB_HBW_cycle") * Math.sqrt(timesHBW.get("cycle"));
            } else if (mode.equals("walk")) {
                predictor += coefficients.get("p.isMobile_HBW_walk");
                predictor += coefficients.get("p.sqrtTTB_HBW_walk") * Math.sqrt(timesHBW.get("walk"));
            }
        }

        for (String mode : timesHBE.keySet()) {
            if (mode.equals("car")) {
                predictor += coefficients.get("p.isMobile_HBE_car");
                predictor += coefficients.get("p.sqrtTTB_HBE_car") * Math.sqrt(timesHBE.get("car"));
                predictor += coefficients.get("p.TTB_HBE_car") * timesHBE.get("car");
            } else if (mode.equals("PT")) {
                predictor += coefficients.get("p.isMobile_HBE_PT");
                predictor += coefficients.get("p.sqrtTTB_HBE_PT") * Math.sqrt(timesHBE.get("PT"));
                predictor += coefficients.get("p.TTB_HBE_PT") * timesHBE.get("PT");
            } else if (mode.equals("cycle")) {
                predictor += coefficients.get("p.isMobile_HBE_cycle");
                predictor += coefficients.get("p.sqrtTTB_HBE_cycle") * Math.sqrt(timesHBE.get("cycle"));
            } else if (mode.equals("walk")) {
                predictor += coefficients.get("p.isMobile_HBE_walk");
                predictor += coefficients.get("p.sqrtTTB_HBE_walk") * Math.sqrt(timesHBE.get("walk"));
            }
        }

        return predictor;
    }


    private Map<String, Double> getTimesByModeForPurpose(List<MitoTrip> trips) {
        Map<String, Double> times = new HashMap<>();

        for (MitoTrip t : trips) {
            if (t.getTripOrigin() != null && t.getTripDestination() != null && t.getTripMode() != null) {
                if (t.getTripMode().equals(Mode.walk)) {
                    double time = 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId())
                            / speed_walk_m_min;
                    times.putIfAbsent("walk", 0.);
                    times.put("walk", time + times.get("walk"));
                } else if (t.getTripMode().equals(Mode.bicycle)) {

                    double time = 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId())
                            / speed_bicycle_m_min;
                    times.putIfAbsent("cycle", 0.);
                    times.put("cycle", time + times.get("cycle"));
                } else if (t.getTripMode().equals(Mode.autoPassenger) || t.getTripMode().equals(Mode.autoDriver) || t.getTripMode().equals(Mode.taxi)) {
                    double time = dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripDestination(), t.getTripOrigin(), t.getDepartureInMinutes(), "car");
                    time += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutesReturnTrip(), "car");
                    times.putIfAbsent("car", 0.);
                    times.put("car", time + times.get("car"));
                } else if (t.getTripMode().equals(Mode.tramOrMetro) || t.getTripMode().equals(Mode.bus) || t.getTripMode().equals(Mode.train)) {
                    String modeString;
                    if (t.getTripMode().equals(Mode.tramOrMetro)) {
                        modeString = "tramMetro";
                    } else {
                        modeString = t.getTripMode().toString();
                    }
                    double time = dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripDestination(), t.getTripOrigin(), t.getDepartureInMinutes(), modeString);
                    time += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutesReturnTrip(), modeString);
                    times.putIfAbsent("PT", 0.);
                    times.put("PT", time + times.get("PT"));
                } else {
                    logger.warn("MITO is not able to calculate the time for mode " + t.getTripMode() + ", then car is assumed");
                    double time = dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripDestination(), t.getTripOrigin(), t.getDepartureInMinutes(), "car");
                    time += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutesReturnTrip(), "car");
                    times.putIfAbsent("car", 0.);
                    times.put("car", time + times.get("car"));
                }
            } else {
                logger.warn("There is a trip for mandatory purposes without origin or destination or mode");
            }
        }
        return times;
    }

}
