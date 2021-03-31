package de.tum.bgu.msm.modules;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.Location;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.MitoUtil;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.quadtree.*;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.moped.MoPeDModel;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.io.input.InputManager;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.FastMultiNodeDijkstraFactory;
import org.matsim.core.router.MultiNodePathCalculator;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.io.output.SummarizeData.writeOutTrips;


public class PedestrianModel {

    private static final Logger logger = Logger.getLogger( PedestrianModel.class );
    private static int failedMatchingMopedZoneCounter = 0;
    private MoPeDModel mopedModel;
    private final DataSet dataSet;
    private final String propertiesPath;
    private Map<Integer, MopedHousehold>  households = new HashMap<>();
    private Quadtree mopedZoneQuadTree = new Quadtree();
    private SparseFloatMatrix2D mopedTravelDistance;
    private int counter = 0;

    public PedestrianModel(DataSet dataSet) {
        this.dataSet = dataSet;
        this.propertiesPath = Resources.INSTANCE.getString(Properties.MOPED_PROPERTIES); //TODO: propoertiesPath MITO?
    }

    public void runMopedHomeBased() {
        this.mopedModel = MoPeDModel.initializeModelFromMito(propertiesPath);
        mopedModel.getManager().readZoneData();
        //TODO: getProperties and check if calc dist by mastsim or not
        //mopedModel.getManager().readDistanceData();
        updateData(dataSet.getYear());
        logger.info("  Running pedestrian model MITO for the year: " + dataSet.getYear());
        mopedModel.runAgentBasedModel();
        feedDataBackToMito();
        writeOutMoPeDTrips(dataSet, Resources.INSTANCE.getString(Properties.SCENARIO_NAME), "homeBased");
    }

    public void runMopedNonHomeBased() {
        updateMopedTripList();
        mopedModel.runAgentBasedModelForNonHomeBased();
        feedNonHomeBasedTripsToMito();
        writeOutMoPeDTrips(dataSet, Resources.INSTANCE.getString(Properties.SCENARIO_NAME), "nonHomeBased");
    }

    private void feedNonHomeBasedTripsToMito() {
        for(MopedTrip mopedTrip : mopedModel.getDataSet().getTrips().values().stream().filter(tt->tt.getTripPurpose().equals(Purpose.NHBW)||tt.getTripPurpose().equals(Purpose.NHBO)).collect(Collectors.toList())){
            MitoTrip mitoTrip = dataSet.getTrips().get(mopedTrip.getId());
            if(mopedTrip.getTripOrigin()!=null){
                mitoTrip.setTripOrigin(dataSet.getZones().get(mopedTrip.getTripOrigin().getMitoZoneId()));
                mitoTrip.setTripOriginMopedZoneId(mopedTrip.getTripOrigin().getZoneId());
            }

            if(mopedTrip.isWalkMode()){
                mitoTrip.setTripMode(Mode.walk);
                if(mopedTrip.getTripOrigin()!=null&&mopedTrip.getTripDestination()!=null) {
                    mitoTrip.setTripDestination(dataSet.getZones().get(mopedTrip.getTripDestination().getMitoZoneId()));
                    mitoTrip.setTripDestinationMopedZoneId(mopedTrip.getTripDestination().getZoneId());
                    //TODO: travel time budget?
                    double newTravelBudget = dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).getTravelTimeBudgetForPurpose(mitoTrip.getTripPurpose()) - mopedTrip.getTripDistance()/83.3;//average walk speed 5km/hr
                    dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).setTravelTimeBudgetByPurpose(mitoTrip.getTripPurpose(),newTravelBudget);
                }else{
                    logger.warn("trip id: " + mitoTrip.getTripId()+ " purpose: " + mitoTrip.getTripPurpose() + " has no origin or destination: " + mopedTrip.getTripOrigin() + "," + mopedTrip.getTripDestination());
                }
            }
        }
    }

    private void updateMopedTripList() {
        List<MitoTrip> homeBasedMitoTrips = dataSet.getTrips().values().stream().filter(tt -> !tt.getTripPurpose().equals(de.tum.bgu.msm.data.Purpose.NHBW)&&!tt.getTripPurpose().equals(de.tum.bgu.msm.data.Purpose.NHBO)).collect(Collectors.toList());
        int missingTrips = 0;
        for (MitoTrip mitoTrip :homeBasedMitoTrips){
            MopedTrip mopedTrip = mopedModel.getDataSet().getTrips().get(mitoTrip.getId());
            if(mopedTrip==null){
                //logger.warn("trip " + mitoTrip.getId() + " cannot be found in moped trip list.");
                missingTrips++;
                continue;
            }
            if(mopedTrip.getTripDestination()==null&&mitoTrip.getTripDestination()!=null){
                Coord destinationCoord;
                if(mitoTrip.getTripDestination() instanceof MicroLocation) {
                    destinationCoord = CoordUtils.createCoord(((MicroLocation) mitoTrip.getTripDestination()).getCoordinate());
                } else {
                    //TODO: random point? moped destination choice will also be random. Centriod of zone?
                    destinationCoord = CoordUtils.createCoord(dataSet.getZones().get(mitoTrip.getTripDestination().getZoneId()).getRandomCoord());
                }
                MopedZone destination = locateMicrolationToMopedZone(new Coordinate(destinationCoord.getX(),destinationCoord.getY()));
                mopedTrip.setTripDestination(destination);
            }
        }
    }

    private void feedDataBackToMito() {
        for(MopedTrip mopedTrip : mopedModel.getDataSet().getTrips().values()){
            MitoTrip mitoTrip = dataSet.getTrips().get(mopedTrip.getId());
            if(mopedTrip.getTripOrigin()!=null){
                mitoTrip.setTripOrigin(dataSet.getZones().get(mopedTrip.getTripOrigin().getMitoZoneId()));
                mitoTrip.setTripOriginMopedZoneId(mopedTrip.getTripOrigin().getZoneId());
            }
            if(mopedTrip.isWalkMode()){
                mitoTrip.setTripMode(Mode.walk);
                if(mopedTrip.getTripOrigin()!=null&&mopedTrip.getTripDestination()!=null) {
                    mitoTrip.setTripDestination(dataSet.getZones().get(mopedTrip.getTripDestination().getMitoZoneId()));
                    mitoTrip.setTripDestinationMopedZoneId(mopedTrip.getTripDestination().getZoneId());
                    //TODO: travel time budget?
                    double newTravelBudget = dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).getTravelTimeBudgetForPurpose(mitoTrip.getTripPurpose()) - mopedTrip.getTripDistance()/83.3;//average walk speed 5km/hr
                    dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).setTravelTimeBudgetByPurpose(mitoTrip.getTripPurpose(),newTravelBudget);
                }else{
                    logger.warn("trip id: " + mitoTrip.getTripId()+ " purpose: " + mitoTrip.getTripPurpose() + " has no origin or destination: " + mopedTrip.getTripOrigin() + "," + mopedTrip.getTripDestination());
                }
            }
        }
    }

    private void updateData(int year) {

        prepareMopedZoneSearchTree();
        logger.info("  Converting mito household to moped");
        convertHhs();
        logger.info(counter + " trips has been converted to moped");
        logger.info("  MITO data being sent to MoPeD");
        InputManager.InputFeed feed = new InputManager.InputFeed(households, year);
        mopedModel.feedDataFromMITO(feed);
    }

    private void prepareMopedZoneSearchTree() {
        for(MopedZone mopedZone: mopedModel.getDataSet().getZones().values()){
            this.mopedZoneQuadTree.insert(((Geometry)(mopedZone.getShapeFeature().getDefaultGeometry())).getEnvelopeInternal(),mopedZone);
        }
    }

    private void convertHhs() {

        for (MitoHousehold hh : dataSet.getHouseholds().values()){
            MopedHousehold mopedHousehold = convertToMopedHh(hh);
            for (MitoPerson pp : hh.getPersons().values()){
                MopedPerson mopedPerson = convertToMopedPp(pp);
                mopedPerson.setMopedHousehold(mopedHousehold);
                for (MitoTrip tt : pp.getTrips()){
                    MopedTrip mopedTrip = convertToMopedTt(tt);
                    if(mopedTrip.getTripPurpose().equals(Purpose.HBW)||mopedTrip.getTripPurpose().equals(Purpose.HBE)){
                    //if(mopedPerson.getOccupation().equals(Occupation.STUDENT)||mopedPerson.getOccupation().equals(Occupation.WORKER)){
                        mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                        mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                    }
                    mopedPerson.addTrip(mopedTrip);
                    if(mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).isEmpty()){
                       mopedHousehold.setTripsByPurpose(new ArrayList<MopedTrip>(),  mopedTrip.getTripPurpose());
                    }
                    mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).add(mopedTrip);
                }
                mopedHousehold.addPerson(mopedPerson);
            }
            households.put(mopedHousehold.getId(),mopedHousehold);
        }

        logger.warn(failedMatchingMopedZoneCounter + " home/job locations failed to be located to a moped zone!");
    }

    private MopedTrip convertToMopedTt(MitoTrip tt) {
        Purpose mopedPurpose = Purpose.valueOf(tt.getTripPurpose().name());
        counter++;
        return new MopedTrip(tt.getTripId(),mopedPurpose);
    }

    private MopedPerson convertToMopedPp(MitoPerson pp) {
        Gender mopedGender = Gender.valueOf(pp.getMitoGender().name());
        Occupation mopedOccupation = Occupation.valueOf(pp.getMitoOccupationStatus().name());
        MopedPerson mopedPerson = new MopedPerson(pp.getId(),pp.getAge(),mopedGender,mopedOccupation,pp.hasDriversLicense(),pp.hasTransitPass(),pp.isDisable());
        if(pp.getMitoOccupationStatus().equals(MitoOccupationStatus.WORKER)||pp.getMitoOccupationStatus().equals(MitoOccupationStatus.STUDENT)){
            //TODO: check for those who are employed but no occupation location
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
        //logger.warn("Coordinate x: " + coordinate.x + ", y: " + coordinate.y + " can not be located to a moped zone.");
        failedMatchingMopedZoneCounter++;
        return null;
    }


    private void writeOutMoPeDTrips(DataSet dataSet, String scenarioName, String purpose) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        logger.info("  Writing moped trips file");
        String file = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + dataSet.getYear() + "/microData/mopedTrips_" + purpose + ".csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,originMoped,destination,destinationMoped,purpose,person,distance,mode");
        logger.info("total trip: " + dataSet.getTrips().values().size());
        for (MitoTrip trip : dataSet.getTrips().values()) {
            pwh.print(trip.getId());
            pwh.print(",");
            Location origin = trip.getTripOrigin();
            String originId = "null";
            String originMopedId = "null";
            if(origin != null) {
                originId = String.valueOf(origin.getZoneId());
            }
            pwh.print(originId);
            pwh.print(",");
            pwh.print(trip.getTripOriginMopedZoneId());
            pwh.print(",");
            Location destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getZoneId());
            }
            pwh.print(destinationId);
            pwh.print(",");
            pwh.print(trip.getTripDestinationMopedZoneId());
            pwh.print(",");
            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            if(Mode.walk.equals(trip.getTripMode())) {
                double distance = mopedModel.getDataSet().getTrips().get(trip.getId()).getTripDistance();
                pwh.print(distance);
            } else {
                pwh.print("NA");
            }
            pwh.print(",");
            pwh.println(trip.getTripMode());

        }
        pwh.close();
    }


}
