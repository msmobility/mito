package de.tum.bgu.msm.modules.tripGeneration;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.readers.TripGenerationHurdleCoefficientReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix1D;
import de.tum.bgu.msm.util.matrices.IndexedDoubleMatrix2D;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import umontreal.ssj.probdist.NegativeBinomialDist;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.tum.bgu.msm.io.input.readers.LogsumReader.convertArrayListToIntArray;
import static de.tum.bgu.msm.modules.tripGeneration.RawTripGenerator.TRIP_ID_COUNTER;

public class TripsByPurposeGeneratorPersonBasedHurdleAggregateModel extends RandomizableConcurrentFunction<Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>>> implements TripsByPurposeGeneratorAggregate {

    private static final Logger logger = Logger.getLogger(TripsByPurposeGeneratorPersonBasedHurdleAggregateModel.class);
    private Map<MitoHousehold, List<MitoTrip>> tripsByHH = new HashMap<>();

    private final DataSet dataSet;
    private final Purpose purpose;

    private double scaleFactorForGeneration;
    private HouseholdTypeManager householdTypeManager;

    private Map<String, Double> binLogCoef;
    private Map<String, Double> negBinCoef;

    private int casesWithMoreThanTen = 0;
    private double speed_bicycle_m_min = Properties.SPEED_BICYCLE_M_MIN;
    private double speed_walk_m_min = Properties.SPEED_WALK_M_MIN;

    private MitoAggregatePersona persona;
    private AreaTypes.SGType areaType;

    protected TripsByPurposeGeneratorPersonBasedHurdleAggregateModel(DataSet dataSet, Purpose purpose, double scaleFactorForGeneration,
                                                                     MitoAggregatePersona persona, AreaTypes.SGType areaType) {
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
        this.persona = persona;
        this.areaType = areaType;
    }

    @Override
    public Tuple<Purpose, Map<MitoHousehold, List<MitoTrip>>> call() throws Exception {
        generateTripsForPersona(persona, areaType);
        logger.warn("Cases with more than ten trips per household - might be a problem if too frequent: " + casesWithMoreThanTen +
                " for purpose " + purpose);
        return new Tuple<>(purpose, tripsByHH);

    }

    private void generateTripsForPersona(MitoAggregatePersona persona, AreaTypes.SGType areaType) {

        double utilityTravel = calculateUtility(persona, binLogCoef, areaType);
        double probabilityTravel = Math.exp(utilityTravel) / (1. + Math.exp(utilityTravel)); // travel trip probability
        double averageTripsNegBin = calculateUtility(persona, negBinCoef, areaType); //average trips of those who travel with negative binomial
        averageTripsNegBin = Math.exp(averageTripsNegBin);

        double theta = negBinCoef.get("theta");
        double variance = averageTripsNegBin + 1 / theta * Math.pow(averageTripsNegBin, 2);
        double p = (variance - averageTripsNegBin) / variance;

        NegativeBinomialDist nb = new NegativeBinomialDist(theta, 1 - p);
        double pOfAtLeastZero = nb.cdf(0); //to cut by y = 1
        //int i = 1;
        double[] probs = new double[11];
        int[] trips = new int[11];
        double averageTrips = 0.;
        for (int i = 0; i < 11; i++) {
            trips[i] = i;
            probs[i] = nb.prob(i);
            averageTrips += trips[i] * probs[i];
        }
        double averageTripsTravelers = averageTrips / (1-pOfAtLeastZero);

        double averageTripsAll = averageTripsTravelers * probabilityTravel;

        dataSet.getAverageTripsByPurpose().get(persona).get(purpose).put(areaType, averageTripsAll);

        //assign origin to home based trips. Non-home based trips origins and destinations are assigned in trip distribution
        if (!purpose.equals(Purpose.NHBW)){
            if (!purpose.equals(Purpose.NHBO)){

                ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
                final IndexedDoubleMatrix2D matrix = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
                for (MitoZone origin : dataSet.getZones().values()){
                    if (origin.getAreaTypeSG().equals(areaType)) {
                        IndexedDoubleMatrix1D residents = dataSet.getPersonsByZone();
                        for (MitoZone destination : dataSet.getZones().values()) {
                            double tripsZone = averageTripsAll * residents.getIndexed(origin.getId());
                            matrix.setIndexed(origin.getId(), destination.getId(),tripsZone);
                        }
                    }
                }
                tripMatrix.put(Mode.pooledTaxi, matrix);
                dataSet.setAggregateTripMatrix(tripMatrix);
            } else {
                //for NHBO trips, calculate total NHBO trips generated
                ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
                final IndexedDoubleMatrix2D matrix = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
                for (MitoZone origin : dataSet.getZones().values()){
                    if (origin.getAreaTypeSG().equals(areaType)) {
                        IndexedDoubleMatrix1D residents = dataSet.getPersonsByZone();
                        double tripsZone = averageTripsAll * residents.getIndexed(origin.getId());
                        dataSet.setTotalNHBOTrips(dataSet.getTotalNHBOTrips() + tripsZone);
                        matrix.setIndexed(origin.getId(), 1,tripsZone);
                    }
                }
                tripMatrix.put(Mode.pooledTaxi, matrix);
                dataSet.setAggregateTripMatrix(tripMatrix);
            }
        } else {
            // for NHBW trips
            ConcurrentMap<Mode, IndexedDoubleMatrix2D> tripMatrix = new ConcurrentHashMap<>();
            final IndexedDoubleMatrix2D matrix = dataSet.getAggregateTripMatrix().get(Mode.pooledTaxi);
            for (MitoZone origin : dataSet.getZones().values()){
                if (origin.getAreaTypeSG().equals(areaType)) {
                    IndexedDoubleMatrix1D residents = dataSet.getPersonsByZone();
                    double tripsZone = averageTripsAll * residents.getIndexed(origin.getId());
                    dataSet.setTotalNHBWTrips(dataSet.getTotalNHBWTrips() + tripsZone);
                    matrix.setIndexed(origin.getId(), 1,tripsZone);
                }
            }
            tripMatrix.put(Mode.pooledTaxi, matrix);
            dataSet.setAggregateTripMatrix(tripMatrix);
        }


        logger.info("Trip generation. Purpose: " + purpose + " ----------------- ");
        logger.info("Trip generation. Proportion of travelers: " + probabilityTravel);
        logger.info("Trip generation. Average trips per traveler: " + averageTripsTravelers);

        summarizeTripGeneration();

        /*for (Mode mode : Mode.values()) {
            for (int i : dataSet.getZones().keySet()) {
                for (int j : dataSet.getZones().keySet()) {
                    dataSet.getAggregateTripMatrix().get(mode).setIndexed(i,j,averageTrips);
                }
            }
        }*/
    }

    private void summarizeTripGeneration(){
        Path fileTripGen = Path.of("F:/models/mitoAggregate/mitoMunich/interimFiles/" + persona.getId() + "TripGen_summary.csv");
        PrintWriter pw = MitoUtil.openFileForSequentialWriting(fileTripGen.toAbsolutePath().toString(), true);
        if (purpose.equals(Purpose.HBW) && areaType.equals(AreaTypes.SGType.CORE_CITY)) {
            pw.println("persona,purpose,areaType,averageTrips,totalTrips");
        }
        pw.print(persona.getId());
        pw.print(",");
        pw.print(purpose);
        pw.print(",");
        pw.print(areaType);
        pw.print(",");
        pw.print(dataSet.getAverageTripsByPurpose().get(persona).get(purpose).get(areaType).toString());
        pw.print(",");
        pw.print(dataSet.getTotalTripsGenByPurpose().get(persona).get(purpose).get(areaType).toString());
        pw.println();
        pw.close();
    }

     private double calculateUtility(MitoAggregatePersona persona, Map<String, Double> coefficients, AreaTypes.SGType areaType) {

        double utilityTravel = coefficients.get("(Intercept)");

        utilityTravel += coefficients.get("hh.size_1")*persona.getAggregateAttributes().get("hh.size_1");
        utilityTravel += coefficients.get("hh.size_2")*persona.getAggregateAttributes().get("hh.size_2");
        utilityTravel += coefficients.get("hh.size_3")*persona.getAggregateAttributes().get("hh.size_3");
        utilityTravel += coefficients.get("hh.size_4")*persona.getAggregateAttributes().get("hh.size_4");
        utilityTravel += coefficients.get("hh.size_5")*persona.getAggregateAttributes().get("hh.size_5");

        utilityTravel += coefficients.get("hh.children_1")*persona.getAggregateAttributes().get("hh.children_1");
        utilityTravel += coefficients.get("hh.children_2")*persona.getAggregateAttributes().get("hh.children_2");
        utilityTravel += coefficients.get("hh.children_3")*persona.getAggregateAttributes().get("hh.children_3");

        utilityTravel += coefficients.get("hh.cars_1")*persona.getAggregateAttributes().get("hh.cars_1");
        utilityTravel += coefficients.get("hh.cars_2")*persona.getAggregateAttributes().get("hh.cars_2");
        utilityTravel += coefficients.get("hh.cars_3")*persona.getAggregateAttributes().get("hh.cars_3");

        utilityTravel += coefficients.get("hh.carsPerAdult")*Math.min(persona.getAggregateAttributes().get("hh.carsPerAdult"),1.0);

        utilityTravel += coefficients.get("hh.econStatus_1")*persona.getAggregateAttributes().get("hh.econStatus_1");
        utilityTravel += coefficients.get("hh.econStatus_2")*persona.getAggregateAttributes().get("hh.econStatus_2");
        utilityTravel += coefficients.get("hh.econStatus_3")*persona.getAggregateAttributes().get("hh.econStatus_3");
        utilityTravel += coefficients.get("hh.econStatus_4")*persona.getAggregateAttributes().get("hh.econStatus_4");
        utilityTravel += coefficients.get("hh.econStatus_5")*persona.getAggregateAttributes().get("hh.econStatus_5");

        if (areaType.equals(AreaTypes.SGType.MEDIUM_SIZED_CITY)) {
            utilityTravel += coefficients.get("hh.BBSR_2") * persona.getAggregateAttributes().get("hh.BBSR_20");
        } else if (areaType.equals(AreaTypes.SGType.TOWN)) {
            utilityTravel += coefficients.get("hh.BBSR_3") * persona.getAggregateAttributes().get("hh.BBSR_30");
        } else if (areaType.equals(AreaTypes.SGType.RURAL)) {
            utilityTravel += coefficients.get("hh.BBSR_4") * persona.getAggregateAttributes().get("hh.BBSR_40");
        }

        utilityTravel += coefficients.get("p.age_gr_1")*persona.getAggregateAttributes().get("tripGenp.age_gr_1");
        utilityTravel += coefficients.get("p.age_gr_2")*persona.getAggregateAttributes().get("tripGenp.age_gr_2");
        utilityTravel += coefficients.get("p.age_gr_3")*persona.getAggregateAttributes().get("tripGenp.age_gr_3");
        utilityTravel += coefficients.get("p.age_gr_4")*persona.getAggregateAttributes().get("tripGenp.age_gr_4");
        utilityTravel += coefficients.get("p.age_gr_5")*persona.getAggregateAttributes().get("tripGenp.age_gr_5");
        utilityTravel += coefficients.get("p.age_gr_6")*persona.getAggregateAttributes().get("tripGenp.age_gr_6");

        utilityTravel += coefficients.get("p.female")*persona.getAggregateAttributes().get("p.FEMALE");

        utilityTravel += coefficients.get("p.driversLicense")*persona.getAggregateAttributes().get("p.driversLicense");

        utilityTravel += coefficients.get("p.occupation_worker")*persona.getAggregateAttributes().get("p.occupation_WORKER");
        utilityTravel += coefficients.get("p.occupation_student")*persona.getAggregateAttributes().get("p.occupation_STUDENT");
        utilityTravel += coefficients.get("p.occupation_unemployed")*persona.getAggregateAttributes().get("p.occupation_UNEMPLOYED");

        if (!purpose.equals(Purpose.HBW)){
            if (!purpose.equals(Purpose.HBE)){

                utilityTravel += coefficients.get("p.isMobile_HBW_car")*persona.getAggregateAttributes().get("p.isMobile_HBW_car_yes");
                utilityTravel += coefficients.get("p.isMobile_HBW_PT")*persona.getAggregateAttributes().get("p.isMobile_HBW_PT_yes");
                utilityTravel += coefficients.get("p.isMobile_HBW_cycle")*persona.getAggregateAttributes().get("p.isMobile_HBW_cycle_yes");
                utilityTravel += coefficients.get("p.isMobile_HBW_walk")*persona.getAggregateAttributes().get("p.isMobile_HBW_walk_yes");

                utilityTravel += coefficients.get("p.sqrtTTB_HBW_car")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBW_car"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_PT")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBW_PT"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_cycle")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBW_cycle"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBW_walk")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBW_walk"));

                utilityTravel += coefficients.get("p.isMobile_HBE_car")*persona.getAggregateAttributes().get("p.isMobile_HBE_car_yes");
                utilityTravel += coefficients.get("p.isMobile_HBE_PT")*persona.getAggregateAttributes().get("p.isMobile_HBE_PT_yes");
                utilityTravel += coefficients.get("p.isMobile_HBE_cycle")*persona.getAggregateAttributes().get("p.isMobile_HBE_cycle_yes");
                utilityTravel += coefficients.get("p.isMobile_HBE_walk")*persona.getAggregateAttributes().get("p.isMobile_HBE_walk_yes");

                utilityTravel += coefficients.get("p.sqrtTTB_HBE_car")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBE_car"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_PT")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBE_PT"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_cycle")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBE_cycle"));
                utilityTravel += coefficients.get("p.sqrtTTB_HBE_walk")*Math.sqrt(persona.getAggregateAttributes().get("p.TTB_HBE_walk"));

                utilityTravel += coefficients.get("p.TTB_HBE_car")*persona.getAggregateAttributes().get("p.TTB_HBE_car");
                utilityTravel += coefficients.get("p.TTB_HBE_PT")*persona.getAggregateAttributes().get("p.TTB_HBE_PT");
            }
        }

            utilityTravel += coefficients.get("calibrationFactor");

        return utilityTravel;
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
