package de.tum.bgu.msm.modules;

import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.moped.MoPeDModel;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.io.input.InputManager;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class PedestrianModel {

    private static final Logger logger = Logger.getLogger( PedestrianModel.class );
    private static AtomicInteger failedMatchingMopedZoneCounter = new AtomicInteger(0);
    private MoPeDModel mopedModel;
    private final DataSet dataSet;
    private final String propertiesPath;
    private ConcurrentMap<Integer, MopedHousehold>  households = new ConcurrentHashMap<>();
    private Quadtree mopedZoneQuadTree = new Quadtree();

    public PedestrianModel(DataSet dataSet) {
        this.dataSet = dataSet;
        this.propertiesPath = Resources.instance.getString(Properties.MOPED_PROPERTIES); //TODO: propoertiesPath MITO?
    }

    public void initializeMoped(){
        this.mopedModel = MoPeDModel.initializeModelFromMito(propertiesPath);
        mopedModel.getManager().readZoneData();
        updateData(dataSet.getYear());
    }

    public void runMopedMandatory() {
        logger.info("  Running Moped Walk Mode Choice for Mandatory trips for the year: " + dataSet.getYear());
        mopedModel.runAgentBasedModelForMandatoryTrips();
        feedDataBackToMito(Purpose.getMandatoryPurposes());
        writeOutMoPeDTrips(dataSet, Resources.instance.getString(Properties.SCENARIO_NAME),"mandatory");
    }

    public void runMopedHomeBasedDiscretionary() {
        logger.info("  Running pedestrian model MITO for the year: " + dataSet.getYear());
        updateDiscretionaryTripList();
        mopedModel.runAgentBasedModelForHomeBasedDiscretionaryTrips();
        feedDataBackToMito(Purpose.getHomeBasedDiscretionaryPurposes());
        writeOutMoPeDTrips(dataSet, Resources.instance.getString(Properties.SCENARIO_NAME),"hbdiscretionary");

    }

    public void runMopedNonHomeBased() {
        updateNonHomeBasedTripList();
        mopedModel.runAgentBasedModelForNonHomeBasedTrips();
        feedDataBackToMito(Purpose.getNonHomeBasedPurposes());
        writeOutMoPeDTrips(dataSet, Resources.instance.getString(Properties.SCENARIO_NAME),"nhb");
    }

    private void updateData(int year) {
        prepareMopedZoneSearchTree();
        logger.info("  Converting mito household to moped");
        convertHhs();
        logger.info("  MITO data being sent to MoPeD");
        InputManager.InputFeed feed = new InputManager.InputFeed(households, year);
        mopedModel.feedDataFromMITO(feed);
    }

    private void convertHhs() {

        final int partitionSize = (int) ((double) dataSet.getHouseholds().values().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(dataSet.getHouseholds().values(), partitionSize);
        logger.info(dataSet.getHouseholds().values().size() + " households are going to be converted...");
        logger.info("partition size: " + partitionSize);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger partitionCounter = new AtomicInteger(0);
        AtomicInteger ppCounter = new AtomicInteger(0);
        AtomicInteger ttCounter = new AtomicInteger(0);
        AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);

        for (final List<MitoHousehold> partition : partitions) {
            executor.addTaskToQueue(() -> {
                try {
                    int id = partitionCounter.incrementAndGet();
                    int hhCounter = 0;
                    for (MitoHousehold hh : partition) {
                        if (hasTrip(hh)) {
                            if (LongMath.isPowerOfTwo(hhCounter)) {
                                logger.info(hhCounter + " households done in " + id);
                            }
                            MopedHousehold mopedHousehold = convertToMopedHh(hh);
                            for (MitoPerson pp : hh.getPersons().values()) {
                                MopedPerson mopedPerson = convertToMopedPp(pp);
                                ppCounter.incrementAndGet();
                                mopedPerson.setMopedHousehold(mopedHousehold);
                                for (MitoTrip tt : pp.getTrips()) {
                                    MopedTrip mopedTrip = convertToMopedTt(tt);
                                    ttCounter.incrementAndGet();
                                    mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                                    tt.setTripOriginMopedZone(mopedHousehold.getHomeZone());
                                    if(Purpose.getMandatoryPurposes().contains(mopedTrip.getTripPurpose())){
                                        if(mopedPerson.getOccupationZone()!=null){
                                            mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                                            tt.setTripDestinationMopedZone(mopedPerson.getOccupationZone());
                                        }else{
                                            randomOccupationDestinationTrips.incrementAndGet();
                                            Coord destinationCoord;
                                            if(tt.getTripDestination() instanceof MicroLocation) {
                                                destinationCoord = CoordUtils.createCoord(((MicroLocation) tt.getTripDestination()).getCoordinate());
                                            } else {
                                                destinationCoord = CoordUtils.createCoord(((MitoZone)tt.getTripDestination()).getRandomCoord(MitoUtil.getRandomObject()));
                                            }
                                            MopedZone destination = locateMicrolationToMopedZone(new Coordinate(destinationCoord.getX(),destinationCoord.getY()));
                                            mopedTrip.setTripDestination(destination);
                                            tt.setTripDestinationMopedZone(destination);
                                        }
                                    }

                                    mopedPerson.addTrip(mopedTrip);
                                    if (mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).isEmpty()) {
                                        mopedHousehold.setTripsByPurpose(new ArrayList<>(), mopedTrip.getTripPurpose());
                                    }
                                    mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).add(mopedTrip);
                                }
                                mopedHousehold.addPerson(mopedPerson);
                            }
                            households.put(mopedHousehold.getId(), mopedHousehold);
                            hhCounter++;
                        }else{
                            if (LongMath.isPowerOfTwo(hhCounter)) {
                                logger.info(hhCounter + " households done in " + id);
                            }
                            MopedHousehold mopedHousehold = convertToMopedHh(hh);
                            for (MitoPerson pp : hh.getPersons().values()) {
                                MopedPerson mopedPerson = convertToMopedPp(pp);
                                mopedPerson.setMopedHousehold(mopedHousehold);
                                mopedHousehold.addPerson(mopedPerson);
                            }
                            households.put(mopedHousehold.getId(), mopedHousehold);
                            hhCounter++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();

        logger.info(ppCounter.get() + " persons have been transfered to Moped...");
        logger.info(ttCounter.get() + " trips have been transfered to Moped...");
        logger.warn(failedMatchingMopedZoneCounter.get() + " home/job locations failed to be located to a moped zone!");
        logger.info("There have been " + randomOccupationDestinationTrips.get() +
                " HBW or HBE trips not done by a worker or student or missing occupation Coord. " +
                "Picked a PAZ by random point in TAZ.");

    }

    private void updateDiscretionaryTripList() {
        logger.info(mopedModel.getDataSet().getTrips().size() + " trips in Moped!");

        final int partitionSize = (int) ((double) dataSet.getHouseholds().values().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MitoHousehold>> partitions = Iterables.partition(dataSet.getHouseholds().values(), partitionSize);
        logger.info(dataSet.getHouseholds().values().size() + " households are going to be converted...");
        logger.info("partition size: " + partitionSize);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger partitionCounter = new AtomicInteger(0);
        AtomicInteger ttCounter = new AtomicInteger(0);

        for (final List<MitoHousehold> partition : partitions) {
            executor.addTaskToQueue(() -> {
                try {
                    int id = partitionCounter.incrementAndGet();
                    int hhCounter = 0;
                    for (MitoHousehold hh : partition) {
                        if (hasDiscretionaryTrip(hh)) {
                            if (LongMath.isPowerOfTwo(hhCounter)) {
                                logger.info(hhCounter + " households done in " + id);
                            }
                            for (MitoPerson pp : hh.getPersons().values()) {
                                for (MitoTrip tt : pp.getTrips()) {
                                    if (de.tum.bgu.msm.data.Purpose.getDiscretionaryPurposes().contains(tt.getTripPurpose())) {
                                        MopedHousehold mopedHousehold = mopedModel.getDataSet().getHouseholds().get(hh.getId());
                                        if(mopedHousehold==null){
                                            logger.warn("Household:" + hh.getId() + " does not exist in moped household list!");
                                            continue;
                                        }
                                        MopedPerson mopedPerson = mopedModel.getDataSet().getPersons().get(pp.getId());
                                        if(mopedPerson==null){
                                            logger.warn("Person:" + pp.getId() + " does not exist in moped person list!");
                                            continue;
                                        }
                                        MopedTrip mopedTrip = convertToMopedTt(tt);

                                        if (Purpose.getHomeBasedDiscretionaryPurposes().contains(mopedTrip.getTripPurpose())) {
                                            mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                                            tt.setTripOriginMopedZone(mopedHousehold.getHomeZone());
                                            tt.setTripOrigin(hh);
                                        }

                                        mopedPerson.addTrip(mopedTrip);
                                        if (mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).isEmpty()) {
                                            mopedHousehold.setTripsByPurpose(new ArrayList<>(), mopedTrip.getTripPurpose());
                                        }
                                        mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).add(mopedTrip);
                                        mopedModel.getDataSet().addTrip(mopedTrip);
                                        ttCounter.incrementAndGet();
                                    }
                                }
                            }
                            hhCounter++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();

        logger.info(ttCounter.get() + " trips have been converted");

        logger.info(mopedModel.getDataSet().getTrips().size() + " trips in Moped!");
        Map<Purpose,List<MopedTrip>> tripsByPurpose = mopedModel.getDataSet().getTrips().values().stream().collect(Collectors.groupingBy(tt -> tt.getTripPurpose()));
        for(Purpose purpose:tripsByPurpose.keySet()){
            logger.warn(tripsByPurpose.get(purpose).size() + "  trips for purpose " + purpose);

        }
    }

    private void updateNonHomeBasedTripList() {
        List<MitoTrip> nonHomeBasedMitoTrips = dataSet.getTrips().values().stream().filter(tt -> tt.getTripPurpose().equals(de.tum.bgu.msm.data.Purpose.NHBW)||tt.getTripPurpose().equals(de.tum.bgu.msm.data.Purpose.NHBO)).collect(Collectors.toList());
        final int partitionSize = (int) ((double) nonHomeBasedMitoTrips.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MitoTrip>> partitions = Iterables.partition(nonHomeBasedMitoTrips, partitionSize);
        logger.info(nonHomeBasedMitoTrips.size() + " non home based trips are going to be converted...");
        logger.info("partition size: " + partitionSize);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger partitionCounter = new AtomicInteger(0);
        AtomicInteger missingTrips = new AtomicInteger(0);
        AtomicInteger completeTrips = new AtomicInteger(0);

        for (final List<MitoTrip> partition : partitions) {
            executor.addTaskToQueue(() -> {
                try {
                    int id = partitionCounter.incrementAndGet();
                    int ttCounter = 0;

                    for (MitoTrip mitoTrip :partition){

                        if (LongMath.isPowerOfTwo(ttCounter)) {
                            logger.info(ttCounter + " trips done in " + id);
                        }

                        MopedTrip mopedTrip = mopedModel.getDataSet().getTrips().get(mitoTrip.getId());

                        if(mopedTrip==null){
                            missingTrips.incrementAndGet();
                            ttCounter++;
                            continue;
                        }

                        if(mopedTrip.getTripOrigin()==null&&mitoTrip.getTripOrigin()!=null){
                            if(mitoTrip.getTripOriginMopedZone()!=null){
                                mopedTrip.setTripOrigin(mitoTrip.getTripOriginMopedZone());
                            }else{
                                Coord originCoord;
                                if(mitoTrip.getTripOrigin() instanceof MicroLocation) {
                                    originCoord = CoordUtils.createCoord(((MicroLocation) mitoTrip.getTripOrigin()).getCoordinate());
                                } else {
                                    //TODO: random point? moped destination choice will also be random. Centriod of zone?
                                    originCoord = CoordUtils.createCoord(dataSet.getZones().get(mitoTrip.getTripOrigin().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                                }
                                MopedZone origin = locateMicrolationToMopedZone(new Coordinate(originCoord.getX(),originCoord.getY()));
                                mopedTrip.setTripOrigin(origin);
                                mitoTrip.setTripOriginMopedZone(origin);
                            }
                        }
                        ttCounter++;
                        completeTrips.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();

        logger.warn(missingTrips.get() + "non home based mito trips cannot be found in moped trip list. Maybe not in the study area");
        logger.info(completeTrips.get() + "non home based trips have been converted");

    }

    private void feedDataBackToMito(List<Purpose> purposes) {
        List<MopedTrip> mopedTripsByPurpose = mopedModel.getDataSet().getTrips().values().stream().filter(tt -> purposes.contains(tt.getTripPurpose())).collect(Collectors.toList());

        final int partitionSize = (int) ((double) mopedTripsByPurpose.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedTrip>> partitions = Iterables.partition(mopedTripsByPurpose, partitionSize);
        logger.info(mopedTripsByPurpose.size() + " Moped trips are going to be transferred to Mito...");
        logger.info("partition size: " + partitionSize);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger partitionCounter = new AtomicInteger(0);
        AtomicInteger countMopedWalkTrips = new AtomicInteger(0);

        for (final List<MopedTrip> partition : partitions) {
            executor.addTaskToQueue(() -> {
                try {
                    int id = partitionCounter.incrementAndGet();
                    int ttCounter = 0;

                    for(MopedTrip mopedTrip : partition){

                        if (LongMath.isPowerOfTwo(ttCounter)) {
                            logger.info(ttCounter + " trips done in " + id);
                        }

                        MitoTrip mitoTrip = dataSet.getTrips().get(mopedTrip.getId());


                        if(mopedTrip.isWalkMode()){
                            if(mopedTrip.getTripOrigin()!=null){
                                mitoTrip.setTripOriginMopedZone(mopedTrip.getTripOrigin());
                            }else{
                                logger.warn("trip id: " + mopedTrip.getTripId()+ " purpose: " + mopedTrip.getTripPurpose() + " has no origin, but is walk mode.");
                                continue;
                            }

                            if(mopedTrip.getTripDestination()!=null) {
                                mitoTrip.setTripDestination(dataSet.getZones().get(mopedTrip.getTripDestination().getMitoZoneId()));
                                mitoTrip.setTripDestinationMopedZone(mopedTrip.getTripDestination());
                                mitoTrip.setMopedTripDistance(mopedTrip.getTripDistance());
                                //TODO: travel time budget?
                                //double newTravelBudget = dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).getTravelTimeBudgetForPurpose(mitoTrip.getTripPurpose()) - mopedTrip.getTripDistance()/83.3;//average walk speed 5km/hr
                                //dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).setTravelTimeBudgetByPurpose(mitoTrip.getTripPurpose(),newTravelBudget);
                                mitoTrip.setTripMode(Mode.walk);
                                countMopedWalkTrips.incrementAndGet();
                            }else{
                                logger.warn("trip id: " + mopedTrip.getTripId()+ " purpose: " + mopedTrip.getTripPurpose() + " has no destination, but is walk mode. Origin zone: " + mopedTrip.getTripOrigin().getZoneId());
                                continue;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();

        logger.info(countMopedWalkTrips.get() + " moped walk trips have been fed back to MITO");
    }

    private void prepareMopedZoneSearchTree() {
        for(MopedZone mopedZone: mopedModel.getDataSet().getZones().values()){
            this.mopedZoneQuadTree.insert(((Geometry)(mopedZone.getShapeFeature().getDefaultGeometry())).getEnvelopeInternal(),mopedZone);
        }
    }

    private boolean hasTrip(MitoHousehold hh) {
        for(de.tum.bgu.msm.data.Purpose purpose : de.tum.bgu.msm.data.Purpose.values()){
            if(!hh.getTripsForPurpose(purpose).isEmpty()){
                return true;
            }
        }

        return false;
    }

    private boolean hasDiscretionaryTrip(MitoHousehold hh) {
        for(de.tum.bgu.msm.data.Purpose purpose : de.tum.bgu.msm.data.Purpose.getDiscretionaryPurposes()){
            if(!hh.getTripsForPurpose(purpose).isEmpty()){
                return true;
            }
        }

        return false;
    }

    private MopedTrip convertToMopedTt(MitoTrip tt) {
        Purpose mopedPurpose = Purpose.valueOf(tt.getTripPurpose().name());
        return new MopedTrip(tt.getTripId(),mopedPurpose);
    }

    private MopedPerson convertToMopedPp(MitoPerson pp) {
        Gender mopedGender = Gender.valueOf(pp.getMitoGender().name());
        Occupation mopedOccupation = Occupation.valueOf(pp.getMitoOccupationStatus().name());
        MopedPerson mopedPerson = new MopedPerson(pp.getId(),pp.getAge(),mopedGender,mopedOccupation,pp.hasDriversLicense(),pp.hasTransitPass(),pp.isDisable());
        if(pp.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)||pp.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)){
            MopedZone occupationZone = locateMicrolationToMopedZone(new Coordinate(pp.getOccupation().getCoordinate().x,pp.getOccupation().getCoordinate().y));
            mopedPerson.setOccupationZone(occupationZone);
        }
        return mopedPerson;
    }

    private MopedHousehold convertToMopedHh(MitoHousehold mitoHh) {
        int children = DataSet.getChildrenForHousehold(mitoHh);
        MopedZone homeZone = locateMicrolationToMopedZone(new Coordinate(mitoHh.getCoordinate().x,mitoHh.getCoordinate().y));
        return new MopedHousehold(mitoHh.getId(),mitoHh.getMonthlyIncome_EUR(),mitoHh.getAutos(),children,homeZone);
    }

    private MopedZone locateMicrolationToMopedZone(Coordinate coordinate){
        GeometryFactory gf = new GeometryFactory();
        Point point = gf.createPoint(coordinate);
        List<MopedZone> mopedZones = mopedZoneQuadTree.query(point.getEnvelopeInternal());

        for (MopedZone mopedZone : mopedZones){
            if(((Geometry)mopedZone.getShapeFeature().getDefaultGeometry()).contains(point)){
                return mopedZone;
            }
        }

        //TODO: how to deal with null?
        failedMatchingMopedZoneCounter.incrementAndGet();
        return null;
    }

    private void writeOutMoPeDTrips(DataSet dataSet, String scenarioName, String purpose) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        logger.info("  Writing moped trips file");
        String file = Resources.instance.getBaseDirectory().toString() + "/" + outputSubDirectory + dataSet.getYear() + "/microData/mopedTrips_" +purpose+ ".csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,originMoped,destination,destinationMoped,purpose,person,distance,mode");
        logger.info("total trip: " + dataSet.getTrips().values().size());
        for (MitoTrip trip : dataSet.getTrips().values()) {
            pwh.print(trip.getId());
            pwh.print(",");
            Location origin = trip.getTripOrigin();
            String originId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getZoneId());
            }
            pwh.print(originId);
            pwh.print(",");
            if(trip.getTripOriginMopedZone() != null) {
                pwh.print(trip.getTripOriginMopedZone().getZoneId());
            }else{
                pwh.print("null");
            }

            pwh.print(",");
            Location destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getZoneId());
            }
            pwh.print(destinationId);
            pwh.print(",");
            if(trip.getTripDestinationMopedZone() != null) {
                pwh.print(trip.getTripDestinationMopedZone().getZoneId());
            }else{
                pwh.print("null");
            }
            pwh.print(",");
            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            pwh.print(trip.getMopedTripDistance());
            pwh.print(",");
            pwh.println(trip.getTripMode());

        }
        pwh.close();
    }


}
