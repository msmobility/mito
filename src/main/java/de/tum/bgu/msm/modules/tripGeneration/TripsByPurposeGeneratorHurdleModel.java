package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.TripGenerationHurdleCoefficientReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import umontreal.ssj.probdist.NegativeBinomialDist;

import javax.validation.constraints.Null;
import java.util.*;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

public class TripsByPurposeGeneratorHurdleModel extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripsByPurposeGenerator {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorHurdleModel.class);
    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose activityPurpose;

    private double scaleFactorForGeneration;
    private HouseholdTypeManager householdTypeManager;

    private Map<String, Double> binLogCoef;
    private Map<String, Double> negBinCoef;

    private int casesWithMoreThanTen = 0;
    private double speed_bicycle_m_min = 12 * 1000 / 60;
    private double speed_walk_m_min = 12 * 1000 / 60;


    protected TripsByPurposeGeneratorHurdleModel(DataSet dataSet, Purpose activityPurpose, double scaleFactorForGeneration) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.activityPurpose = activityPurpose;
        this.scaleFactorForGeneration = scaleFactorForGeneration;
        //this.householdTypeManager = new HouseholdTypeManager(activityPurpose);
        this.binLogCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, activityPurpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleBinaryLogit()).readCoefficientsForThisPurpose();
        this.negBinCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, activityPurpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleNegativeBinomial()).readCoefficientsForThisPurpose();

    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        logger.info("  Generating trips with activityPurpose " + activityPurpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + activityPurpose);
        logger.info("Started assignment of trips for hh, activityPurpose: " + activityPurpose);
        final Iterator<MitoHousehold> iterator = dataSet.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold next = iterator.next();
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration) {
                generateTripsForHousehold(next);
            }
        }
        logger.warn("Cases with more than ten trips per household - might be a problem if too frequent: " + casesWithMoreThanTen +
                " for activityPurpose " + activityPurpose);
        return new Tuple<>(activityPurpose, tripsByHH);
    }

    private void generateTripsForHousehold(MitoHousehold hh) {

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



        double utilityTravel = getUtilityTravelBinaryLogit(hh, tripsForHBW, tripsForHBE, timeHBW, timeHBE);
        double randomNumber = random.nextDouble();
        double probabilityTravel = Math.exp(utilityTravel) / (1. + Math.exp(utilityTravel));
        if (randomNumber < probabilityTravel) {
            estimateAndCreatePositiveNumberOfTrips(hh, tripsForHBW, tripsForHBE, timeHBW, timeHBE);
        }
    }

    private double getUtilityTravelBinaryLogit(MitoHousehold hh, List<MitoTrip> tripsForHBW, List<MitoTrip> tripsForHBE, double timeHBW, double timeHBE) {
        double utilityTravel = 0.;
        int size = Math.min(hh.getHhSize(), 5);
        switch (size) {
            case 1:
                utilityTravel += binLogCoef.get("size_1");
                break;
            case 2:
                utilityTravel += binLogCoef.get("size_2");
                break;
            case 3:
                utilityTravel += binLogCoef.get("size_3");
                break;
            case 4:
                utilityTravel += binLogCoef.get("size_4");
                break;
            case 5:
                utilityTravel += binLogCoef.get("size_5");
                break;
        }
        for (MitoPerson p : hh.getPersons().values()) {
            if (p.getAge() < 6) {
                utilityTravel += binLogCoef.get("pers_under6");
            } else if (p.getAge() < 18) {
                utilityTravel += binLogCoef.get("pers_6to17");
            } else if (p.getAge() < 30) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    utilityTravel += binLogCoef.get("pers_18to29_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    utilityTravel += binLogCoef.get("pers_18to29_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)) {
                    utilityTravel += binLogCoef.get("pers_18to29_u");
                }
            } else if (p.getAge() < 65) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    utilityTravel += binLogCoef.get("pers_30to64_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    utilityTravel += binLogCoef.get("pers_30to64_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)) {
                    utilityTravel += binLogCoef.get("pers_30to64_u");
                }
            } else {
                utilityTravel += binLogCoef.get("pers_65up");
            }
            if (p.getMitoGender().equals(MitoGender.FEMALE)) {
                utilityTravel += binLogCoef.get("pers_female");
            }
        }
        //we do not have pers_mobility_restriction available?

        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus) {
            case 1:
                utilityTravel += 0.;
                break;
            case 2:
                utilityTravel += binLogCoef.get("economicStatus_2");
                break;
            case 3:
                utilityTravel += binLogCoef.get("economicStatus_3");
                break;
            case 4:
                utilityTravel += binLogCoef.get("economicStatus_4");
                break;
            case 5:
                utilityTravel += binLogCoef.get("economicStatus_5");
                break;
        }

        double proportionOfAutos = Math.min(1, hh.getAutos() / hh.getPersons().values().stream().filter(mitoPerson -> mitoPerson.getAge() >= 15).count());
        utilityTravel += binLogCoef.get("propAutos") * proportionOfAutos;

        AreaTypes.SGType type = hh.getHomeZone().getAreaTypeSG();

        //is this right area type?
        switch (type) {
            case CORE_CITY:
                utilityTravel += 0.;
                break;
            case MEDIUM_SIZED_CITY:
                utilityTravel += binLogCoef.get("BBSR_2");
                break;
            case TOWN:
                utilityTravel += binLogCoef.get("BBSR_3");
                break;
            case RURAL:
                utilityTravel += binLogCoef.get("BBSR_4");
                break;
        }


        utilityTravel += tripsForHBW.size() * binLogCoef.get("tripsHBW");
        utilityTravel += timeHBW * binLogCoef.get("timeHBW");


        utilityTravel += tripsForHBE.size() * binLogCoef.get("tripsHBE");
        utilityTravel += timeHBE * binLogCoef.get("timeHBE");

        return utilityTravel;
    }

    private void estimateAndCreatePositiveNumberOfTrips(MitoHousehold hh, List<MitoTrip> tripsForHBW, List<MitoTrip> tripsForHBE, double timeHBW, double timeHBE) {
        double randomNumber = random.nextDouble();
        double averageNumberOfTrips = 0.;
        int size = hh.getHhSize();
        switch (size) {
            case 1:
                averageNumberOfTrips += negBinCoef.get("size_1");
                break;
            case 2:
                averageNumberOfTrips += negBinCoef.get("size_2");
                break;
            case 3:
                averageNumberOfTrips += negBinCoef.get("size_3");
                break;
            case 4:
                averageNumberOfTrips += negBinCoef.get("size_4");
                break;
            case 5:
                averageNumberOfTrips += negBinCoef.get("size_5");
                break;
        }
        for (MitoPerson p : hh.getPersons().values()) {
            if (p.getAge() < 6) {
                averageNumberOfTrips += negBinCoef.get("pers_under6");
            } else if (p.getAge() < 18) {
                averageNumberOfTrips += negBinCoef.get("pers_6to17");
            } else if (p.getAge() < 30) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    averageNumberOfTrips += negBinCoef.get("pers_18to29_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    averageNumberOfTrips += negBinCoef.get("pers_18to29_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)) {
                    averageNumberOfTrips += negBinCoef.get("pers_18to29_u");
                }
            } else if (p.getAge() < 65) {
                if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)) {
                    averageNumberOfTrips += negBinCoef.get("pers_30to64_w");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)) {
                    averageNumberOfTrips += negBinCoef.get("pers_30to64_s");
                } else if (p.getMitoOccupationStatus().equals(MitoOccupationStatus.UNEMPLOYED)) {
                    averageNumberOfTrips += negBinCoef.get("pers_30to64_u");
                }
            } else {
                averageNumberOfTrips += negBinCoef.get("pers_65up");
            }
            if (p.getMitoGender().equals(MitoGender.FEMALE)) {
                averageNumberOfTrips += negBinCoef.get("pers_female");
            }
        }
        //we do not have pers_mobility_restriction available?

        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus) {
            case 1:
                averageNumberOfTrips += 0.;
                break;
            case 2:
                averageNumberOfTrips += negBinCoef.get("economicStatus_2");
                break;
            case 3:
                averageNumberOfTrips += negBinCoef.get("economicStatus_3");
                break;
            case 4:
                averageNumberOfTrips += negBinCoef.get("economicStatus_4");
                break;
            case 5:
                averageNumberOfTrips += negBinCoef.get("economicStatus_5");
                break;
        }

        //check this
        double proportionOfAutos = Math.min(1, hh.getAutos() / hh.getPersons().values().stream().filter(mitoPerson -> mitoPerson.getAge() >= 15).count());
        averageNumberOfTrips += negBinCoef.get("propAutos") * proportionOfAutos;

        AreaTypes.SGType type = hh.getHomeZone().getAreaTypeSG();

        //is this right area type?
        switch (type) {
            case CORE_CITY:
                averageNumberOfTrips += 0.;
                break;
            case MEDIUM_SIZED_CITY:
                averageNumberOfTrips += negBinCoef.get("BBSR_2");
                break;
            case TOWN:
                averageNumberOfTrips += negBinCoef.get("BBSR_3");
                break;
            case RURAL:
                averageNumberOfTrips += negBinCoef.get("BBSR_4");
                break;
        }

        //is this the right value?
        double theta = negBinCoef.get("theta");

        averageNumberOfTrips += tripsForHBW.size() * negBinCoef.get("tripsHBW");
        averageNumberOfTrips += timeHBW * negBinCoef.get("timeHBW");
        averageNumberOfTrips += tripsForHBE.size() * negBinCoef.get("tripsHBE");
        averageNumberOfTrips += timeHBE * negBinCoef.get("timeHBE");

        averageNumberOfTrips = Math.exp(averageNumberOfTrips);

        double variance = averageNumberOfTrips + 1 / theta * Math.pow(averageNumberOfTrips, 2);
        double p = (variance - averageNumberOfTrips) / variance;

        NegativeBinomialDist nb = new NegativeBinomialDist(theta, 1 - p);
        double pOfAtLeastZero = nb.cdf(0); //to cut by y = 1
        int i = 1;
        while (i < 10) {
            if (randomNumber < (nb.cdf(i) - pOfAtLeastZero) / (1 - pOfAtLeastZero)) {
                break;
            }
            i++;
        }
        if (averageNumberOfTrips >= 10) {
            casesWithMoreThanTen++;
        }
        int numberOfTrips = i;
        generateTripsForHousehold(hh, numberOfTrips);

    }

    private void generateTripsForHousehold(MitoHousehold hh, int numberOfTrips) {
        List<MitoTrip> trips = new ArrayList<>();
        for (int i = 0; i < numberOfTrips; i++) {
            MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), activityPurpose);
            if (trip != null) {
                trips.add(trip);
            }
        }
        tripsByHH.put(hh, trips);
    }


}
