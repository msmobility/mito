package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.TripGenerationHurdleCoefficientReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import umontreal.ssj.probdist.NegativeBinomialDist;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

public class TripsByPurposeGeneratorPersonBasedHurdleModel extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripsByPurposeGenerator {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorPersonBasedHurdleModel.class);
    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private double scaleFactorForGeneration;
    private HouseholdTypeManager householdTypeManager;

    private Map<String, Double> binLogCoef;
    private Map<String, Double> negBinCoef;

    private int casesWithMoreThanTen = 0;
    private double speed_bicycle_m_min = 12 * 1000 / 60;
    private double speed_walk_m_min = 12 * 1000 / 60;


    protected TripsByPurposeGeneratorPersonBasedHurdleModel(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration) {
        super(MitoUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.scaleFactorForGeneration = scaleFactorForGeneration;
        //this.householdTypeManager = new HouseholdTypeManager(purpose);
        this.binLogCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, purpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleBinaryLogit()).readCoefficientsForThisPurpose();
        this.negBinCoef =
                new TripGenerationHurdleCoefficientReader(dataSet, purpose,
                        Resources.instance.getTripGenerationCoefficientsHurdleNegativeBinomial()).readCoefficientsForThisPurpose();

    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        logger.info("  Generating trips with purpose " + purpose + " (multi-threaded)");
        logger.info("Created trip frequency distributions for " + purpose);
        logger.info("Started assignment of trips for hh, purpose: " + purpose);
        final Iterator<MitoHousehold> iterator = dataSet.getHouseholds().values().iterator();
        for (; iterator.hasNext(); ) {
            MitoHousehold next = iterator.next();
            if (MitoUtil.getRandomObject().nextDouble() < scaleFactorForGeneration) {
                generateTripsForHousehold(next);
            }
        }
        logger.warn("Cases with more than ten trips per household - might be a problem if too frequent: " + casesWithMoreThanTen +
                " for purpose " + purpose);
        return new Tuple<>(purpose, tripsByHH);
    }

    private void generateTripsForHousehold(MitoHousehold hh) {

        for (MitoPerson person : hh.getPersons().values()) {
            generateTripsForPerson(hh, person);
        }
    }

    private void generateTripsForPerson(MitoHousehold hh, MitoPerson person) {
        Map<String, Double> timesHBW;
        Map<String, Double> timesHBE;
        if (Purpose.getDiscretionaryPurposes().contains(purpose)){
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
        } else {
            timesHBW = Collections.emptyMap();
            timesHBE = Collections.emptyMap();
        }

        double utilityTravel = calculateUtility(hh, person, timesHBW, timesHBE, binLogCoef);
        double randomNumber = random.nextDouble();
        double probabilityTravel = Math.exp(utilityTravel) / (1. + Math.exp(utilityTravel));
        if (randomNumber < probabilityTravel) {
            estimateAndCreatePositiveNumberOfTrips(hh, person, timesHBW, timesHBE);
        }
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
                } else if (t.getTripMode().equals(Mode.autoPassenger) || t.getTripMode().equals(Mode.autoDriver)) {
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

    private double calculateUtility(MitoHousehold hh, MitoPerson person,
                                    Map<String, Double> timesHBW, Map<String, Double> timesHBE, Map<String, Double> coefficients) {
        double utilityTravel = coefficients.get("(Intercept)");
        int size = Math.min(hh.getHhSize(), 5);
        switch (size) {
            case 1:
                utilityTravel += coefficients.get("hh.size_1");
                break;
            case 2:
                utilityTravel += coefficients.get("hh.size_2");
                break;
            case 3:
                utilityTravel += coefficients.get("hh.size_3");
                break;
            case 4:
                utilityTravel += coefficients.get("hh.size_4");
                break;
            case 5:
                utilityTravel += coefficients.get("hh.size_5");
                break;
            default:
                utilityTravel += coefficients.get("hh.size_5");
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
                utilityTravel += coefficients.get("hh.children_1");
                break;
            case 2:
                utilityTravel += coefficients.get("hh.children_2");
                break;
            case 3:
                utilityTravel += coefficients.get("hh.children_3");
                break;
            default:
                utilityTravel += coefficients.get("hh.children_3");
                break;

        }

        int cars = hh.getAutos();

        switch (cars) {
            case 0:
                utilityTravel += 0;
                break;
            case 1:
                utilityTravel += coefficients.get("hh.cars_1");
                break;
            case 2:
                utilityTravel += coefficients.get("hh.cars_2");
                break;
            case 3:
                utilityTravel += coefficients.get("hh.cars_3");
                break;
            default:
                utilityTravel += coefficients.get("hh.cars_3");
                break;
        }

        double carsPerAdult = Math.min((double) cars / ((double) adults), 1.0);
        utilityTravel += coefficients.get("hh.carsPerAdult") * carsPerAdult;

        int economicStatus = hh.getEconomicStatus();
        switch (economicStatus) {
            case 1:
                utilityTravel += coefficients.get("hh.econStatus_1");
                break;
            case 2:
                utilityTravel += coefficients.get("hh.econStatus_2");
                break;
            case 3:
                utilityTravel += coefficients.get("hh.econStatus_3");
                break;
            case 4:
                utilityTravel += coefficients.get("hh.econStatus_4");
                break;
            case 5:
                utilityTravel += coefficients.get("hh.econStatus_5");
                break;
            default:
                throw new RuntimeException("Economic status cannot be zero?");
        }

        AreaTypes.SGType type = hh.getHomeZone().getAreaTypeSG();

        //is this right area type?
        switch (type) {
            case CORE_CITY:
                utilityTravel += 0.;
                break;
            case MEDIUM_SIZED_CITY:
                utilityTravel += coefficients.get("hh.BBSR_2");
                break;
            case TOWN:
                utilityTravel += coefficients.get("hh.BBSR_3");
                break;
            case RURAL:
                utilityTravel += coefficients.get("hh.BBSR_4");
                break;
        }

        int age = person.getAge();
        if (age < 19) {
            utilityTravel += coefficients.get("p.age_gr_1");
        } else if (age < 30) {
            utilityTravel += coefficients.get("p.age_gr_2");
        } else if (age < 50) {
            utilityTravel += coefficients.get("p.age_gr_3");
        } else if (age < 60) {
            utilityTravel += coefficients.get("p.age_gr_4");
        } else if (age < 70) {
            utilityTravel += coefficients.get("p.age_gr_5");
        } else {
            utilityTravel += coefficients.get("p.age_gr_6");
        }

        if (person.getMitoGender().equals(MitoGender.FEMALE)) {
            utilityTravel += coefficients.get("p.female");
        }

        if (person.hasDriversLicense()) {
            utilityTravel += coefficients.get("p.driversLicense");
        }

        switch (person.getMitoOccupationStatus()) {
            case WORKER:
                utilityTravel += coefficients.get("p.occupation_worker");
                break;
            case STUDENT:
                utilityTravel += coefficients.get("p.occupation_student");
                break;
            case UNEMPLOYED:
            case RETIRED:
                utilityTravel += coefficients.get("p.occupation_unemployed");
                break;
        }

        for (String mode : timesHBW.keySet()) {
            if (mode.equals("car")) {
                utilityTravel += coefficients.get("p.isMobile_HBW_car");
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_car") * Math.sqrt(timesHBW.get("car"));
            } else if (mode.equals("PT")) {
                utilityTravel += coefficients.get("p.isMobile_HBW_PT");
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_PT") * Math.sqrt(timesHBW.get("PT"));
            } else if (mode.equals("cycle")) {
                utilityTravel += coefficients.get("p.isMobile_HBW_cycle");
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_cycle") * Math.sqrt(timesHBW.get("cycle"));
            } else if (mode.equals("walk")) {
                utilityTravel += coefficients.get("p.isMobile_HBW_walk");
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_walk") * Math.sqrt(timesHBW.get("walk"));
            }
        }

        for (String mode : timesHBE.keySet()) {
            if (mode.equals("car")) {
                utilityTravel += coefficients.get("p.isMobile_HBE_car");
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_car") * Math.sqrt(timesHBE.get("car"));
                utilityTravel += coefficients.get("p.TTB_HBE_car") * timesHBE.get("car");
            } else if (mode.equals("PT")) {
                utilityTravel += coefficients.get("p.isMobile_HBE_PT");
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_PT") * Math.sqrt(timesHBE.get("PT"));
                utilityTravel += coefficients.get("p.TTB_HBE_PT") * timesHBE.get("PT");
            } else if (mode.equals("cycle")) {
                utilityTravel += coefficients.get("p.isMobile_HBE_cycle");
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_cycle") * Math.sqrt(timesHBE.get("cycle"));
            } else if (mode.equals("walk")) {
                utilityTravel += coefficients.get("p.isMobile_HBE_walk");
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_walk") * Math.sqrt(timesHBE.get("walk"));
            }
        }

        return utilityTravel;
    }

    private void estimateAndCreatePositiveNumberOfTrips(MitoHousehold hh, MitoPerson person,
                                                        Map<String, Double> timesHBW, Map<String, Double> timesHBE) {
        double randomNumber = random.nextDouble();

        double averageNumberOfTrips = calculateUtility(hh, person, timesHBW, timesHBE, negBinCoef);

        double theta = negBinCoef.get("theta");

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
        generateTripsForHouseholdAndPerson(person, hh, numberOfTrips);

    }

    private void generateTripsForHouseholdAndPerson(MitoPerson person, MitoHousehold hh, int numberOfTrips) {

        tripsByHH.putIfAbsent(hh, new ArrayList<>());
        List<MitoTrip> currentTrips = tripsByHH.get(hh);
        for (int i = 0; i < numberOfTrips; i++) {
            MitoTrip trip = new MitoTrip(TRIP_ID_COUNTER.incrementAndGet(), purpose);
            //person.addTrip(trip);
            trip.setPerson(person);
            if (trip != null) {
                currentTrips.add(trip);
            }
        }

        tripsByHH.put(hh, currentTrips);
    }


}
