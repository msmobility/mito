package de.tum.bgu.msm.modules;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.tripGeneration.TripGenPredictor;
import de.tum.bgu.msm.resources.Properties;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class TripGenCalculatorHouseholdBasedHurdleNegBin implements TripGenPredictor {
    private static final Logger logger = Logger.getLogger(TripGenCalculatorHouseholdBasedHurdleNegBin.class);
    private double speed_bicycle_m_min = Properties.SPEED_BICYCLE_M_MIN;
    private double speed_walk_m_min = Properties.SPEED_WALK_M_MIN;

    private final DataSet dataSet;

    public TripGenCalculatorHouseholdBasedHurdleNegBin(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public double getPredictor(MitoHousehold hh,MitoPerson person, Map<String, Double> coefficients) {
        double predictor = 0.;
        int size = Math.min(hh.getHhSize(), 5);
        switch (size) {
            case 1:
                predictor += coefficients.get("size_1");
                break;
            case 2:
                predictor += coefficients.get("size_2");
                break;
            case 3:
                predictor += coefficients.get("size_3");
                break;
            case 4:
                predictor += coefficients.get("size_4");
                break;
            case 5:
                predictor += coefficients.get("size_5");
                break;
        }
        for (MitoPerson p : hh.getPersons().values()) {
            if (p.getAge() < 6) {
                predictor += coefficients.get("pers_under6");
            } else if (p.getAge() < 18) {
                predictor += coefficients.get("pers_6to17");
            } else if (p.getAge() < 30) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    predictor += coefficients.get("pers_18to29_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    predictor += coefficients.get("pers_18to29_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED) ||
                        p.getMitoOccupationStatus().equals(MitoOccupationStatus.RETIRED)) {
                    predictor += coefficients.get("pers_18to29_u");
                }
            } else if (p.getAge() < 65) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    predictor += coefficients.get("pers_30to64_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    predictor += coefficients.get("pers_30to64_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED) ||
                        p.getMitoOccupationStatus().equals(MitoOccupationStatus.RETIRED)) {
                    predictor += coefficients.get("pers_30to64_u");
                }
            } else {
                predictor += coefficients.get("pers_65up");
            }
            if (p.getMitoGender().equals(MitoGender.FEMALE)) {
                predictor += coefficients.get("pers_female");
            }
        }
        //we do not have pers_mobility_restriction available?

        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus) {
            case 1:
                predictor += 0.;
                break;
            case 2:
                predictor += coefficients.get("economicStatus_2");
                break;
            case 3:
                predictor += coefficients.get("economicStatus_3");
                break;
            case 4:
                predictor += coefficients.get("economicStatus_4");
                break;
            case 5:
                predictor += coefficients.get("economicStatus_5");
                break;
        }

        double proportionOfAutos = Math.min(1, hh.getAutos() / hh.getPersons().values().stream().filter(mitoPerson -> mitoPerson.getAge() >= 15).count());
        predictor += coefficients.get("propAutos") * proportionOfAutos;

        AreaTypes.SGType type = hh.getHomeZone().getAreaTypeSG();

        //is this right area type?
        switch (type) {
            case CORE_CITY:
                predictor += 0.;
                break;
            case MEDIUM_SIZED_CITY:
                predictor += coefficients.get("BBSR_2");
                break;
            case TOWN:
                predictor += coefficients.get("BBSR_3");
                break;
            case RURAL:
                predictor += coefficients.get("BBSR_4");
                break;
        }

        List<MitoTrip> tripsForHBW = hh.getTripsForPurpose(Purpose.HBW);
        double timeHBW = 0;
        List<MitoTrip> tripsForHBE = hh.getTripsForPurpose(Purpose.HBE);
        double timeHBE = 0;

        for (MitoTrip t : tripsForHBW) {
            if (t.getTripOrigin() != null && t.getTripDestination() != null && t.getTripMode() != null) {
                if (t.getTripMode().equals(Mode.walk)) {
                    timeHBW += 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId()) / speed_walk_m_min;
                } else if (t.getTripMode().equals(Mode.bicycle)) {
                    timeHBW += 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId()) / speed_bicycle_m_min;
                } else {
                    String modeString;
                    if (t.getTripMode().equals(Mode.autoPassenger) || t.getTripMode().equals(Mode.autoDriver)) {
                        modeString = "car";
                    } else if (t.getTripMode().equals(Mode.tramOrMetro)) {
                        modeString  = "tramMetro";
                    } else if (t.getTripMode().equals(Mode.bus) || t.getTripMode().equals(Mode.train)) {
                        modeString = t.getTripMode().toString();
                    } else {
                        logger.warn("No idea how to measure the time for mode " + t.getTripMode() + " (car is assigned)");
                        modeString = "car";
                    }

                    timeHBW += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripDestination(), t.getTripOrigin(), t.getDepartureInMinutes(), modeString);
                    timeHBW += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutesReturnTrip(), modeString);
                }
            } else {
                logger.warn("There is a trip for HBW without origin or destination or mode");
            }

        }

        for (MitoTrip t : tripsForHBE) {
            if (t.getTripOrigin() != null && t.getTripDestination() != null && t.getTripMode() != null) {
                if (t.getTripMode().equals(Mode.walk)) {
                    timeHBE += 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId()) / speed_walk_m_min;
                } else if (t.getTripMode().equals(Mode.bicycle)) {
                    timeHBE += 2 * dataSet.
                            getTravelDistancesNMT().getTravelDistance(t.getTripDestination().getZoneId(), t.getTripOrigin().getZoneId()) / speed_bicycle_m_min;
                } else {
                    String modeString;
                    if (t.getTripMode().equals(Mode.autoPassenger) || t.getTripMode().equals(Mode.autoDriver)) {
                        modeString = "car";
                    } else if (t.getTripMode().equals(Mode.tramOrMetro)) {
                        modeString  = "tramMetro";
                    } else if (t.getTripMode().equals(Mode.bus) || t.getTripMode().equals(Mode.train)) {
                        modeString = t.getTripMode().toString();
                    } else {
                        logger.warn("No idea how to measure the time for mode " + t.getTripMode() + " (car is assigned)");
                        modeString = "car";
                    }

                    timeHBE += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripDestination(), t.getTripOrigin(), t.getDepartureInMinutes(), modeString);
                    timeHBE += dataSet.
                            getTravelTimes().
                            getTravelTime(t.getTripOrigin(), t.getTripDestination(), t.getDepartureInMinutesReturnTrip(), modeString);
                }
            } else {
                logger.warn("There is a trip for HBE without origin or destination or mode");
            }
        }
        predictor += tripsForHBW.size() * coefficients.get("tripsHBW");
        predictor += timeHBW * coefficients.get("timeHBW");


        predictor += tripsForHBE.size() * coefficients.get("tripsHBE");
        predictor += timeHBE * coefficients.get("timeHBE");

        return predictor;
    }

}
