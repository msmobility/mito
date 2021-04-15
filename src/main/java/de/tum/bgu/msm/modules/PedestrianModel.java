package de.tum.bgu.msm.modules;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.moped.MoPeDModel;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.io.input.InputManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
        this.propertiesPath = Resources.instance.getString(Properties.MOPED_PROPERTIES); //TODO: propoertiesPath MITO?
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
        writeOutMoPeDTrips(dataSet, Resources.instance.getString(Properties.SCENARIO_NAME), "homeBased");
    }

    public void runMopedNonHomeBased() {
        updateMopedTripList();
        mopedModel.runAgentBasedModelForNonHomeBased();
        feedNonHomeBasedTripsToMito();
        writeOutMoPeDTrips(dataSet, Resources.instance.getString(Properties.SCENARIO_NAME), "nonHomeBased");
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
                    mitoTrip.setMopedTripDistance(mopedTrip.getTripDistance());
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
                    destinationCoord = CoordUtils.createCoord(dataSet.getZones().get(mitoTrip.getTripDestination().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                }
                MopedZone destination = locateMicrolationToMopedZone(new Coordinate(destinationCoord.getX(),destinationCoord.getY()));
                mopedTrip.setTripDestination(destination);
            }
        }
    }

    private void feedDataBackToMito() {
        int countMopedWalkTrips = 0;
        for(MopedTrip mopedTrip : mopedModel.getDataSet().getTrips().values()){
            MitoTrip mitoTrip = dataSet.getTrips().get(mopedTrip.getId());
            if(mopedTrip.getTripOrigin()!=null){
                mitoTrip.setTripOrigin(dataSet.getZones().get(mopedTrip.getTripOrigin().getMitoZoneId()));
                mitoTrip.setTripOriginMopedZoneId(mopedTrip.getTripOrigin().getZoneId());
            }
            if(mopedTrip.isWalkMode()){
                countMopedWalkTrips++;
                mitoTrip.setTripMode(Mode.walk);
                if(mopedTrip.getTripOrigin()!=null&&mopedTrip.getTripDestination()!=null) {
                    mitoTrip.setTripDestination(dataSet.getZones().get(mopedTrip.getTripDestination().getMitoZoneId()));
                    mitoTrip.setTripDestinationMopedZoneId(mopedTrip.getTripDestination().getZoneId());
                    mitoTrip.setMopedTripDistance(mopedTrip.getTripDistance());
                    //TODO: travel time budget?
                    double newTravelBudget = dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).getTravelTimeBudgetForPurpose(mitoTrip.getTripPurpose()) - mopedTrip.getTripDistance()/83.3;//average walk speed 5km/hr
                    dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).setTravelTimeBudgetByPurpose(mitoTrip.getTripPurpose(),newTravelBudget);
                }else{
                    logger.warn("trip id: " + mitoTrip.getTripId()+ " purpose: " + mitoTrip.getTripPurpose() + " has no origin or destination: " + mopedTrip.getTripOrigin() + "," + mopedTrip.getTripDestination());
                }
            }
        }
        logger.info(countMopedWalkTrips + " moped walk trips have been fed back to MITO");
    }

    private void updateData(int year) {

        prepareMopedZoneSearchTree();
        logger.info("  Converting mito household to moped");
        boolean mucScenario = Resources.instance.getBoolean(Properties.SCENARIO_MUC, false);;
        if(mucScenario){
            convertMUCHhs();
        }else{
            convertHhs();
        }

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
        int counter = 0;
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {

            if (hasTrip(hh)) {
                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " households done ");
                }
                MopedHousehold mopedHousehold = convertToMopedHh(hh);
                for (MitoPerson pp : hh.getPersons().values()) {
                    MopedPerson mopedPerson = convertToMopedPp(pp);
                    mopedPerson.setMopedHousehold(mopedHousehold);
                    for (MitoTrip tt : pp.getTrips()) {
                        MopedTrip mopedTrip = convertToMopedTt(tt);
                        if (mopedTrip.getTripPurpose().equals(Purpose.HBW) || mopedTrip.getTripPurpose().equals(Purpose.HBE)) {
                            //if(mopedPerson.getOccupation().equals(Occupation.STUDENT)||mopedPerson.getOccupation().equals(Occupation.WORKER)){
                            mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                            mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                        }

                        if (mopedTrip.getTripPurpose().equals(Purpose.HBS) || mopedTrip.getTripPurpose().equals(Purpose.HBO)) {
                            mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                        }

                        if (mopedTrip.getTripPurpose().equals(Purpose.NHBW)) {
                            mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                        }

                        mopedPerson.addTrip(mopedTrip);
                        if (mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).isEmpty()) {
                            mopedHousehold.setTripsByPurpose(new ArrayList<MopedTrip>(), mopedTrip.getTripPurpose());
                        }
                        mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).add(mopedTrip);
                    }
                    mopedHousehold.addPerson(mopedPerson);
                }
                households.put(mopedHousehold.getId(), mopedHousehold);
                counter++;
            }
        }

        logger.warn(failedMatchingMopedZoneCounter + " home/job locations failed to be located to a moped zone!");
    }

    private void convertMUCHhs() {
        int counter = 0;
        for (MitoHousehold hh : dataSet.getHouseholds().values()) {

            //TODO:need to decide how to narrow down the application area, now only run for munich city area
            if(!hh.getHomeZone().isMunichZone()){
                continue;
            }

            if (hasTrip(hh)) {
                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " households done ");
                }
                MopedHousehold mopedHousehold = convertToMopedHh(hh);
                for (MitoPerson pp : hh.getPersons().values()) {
                    MopedPerson mopedPerson = convertToMopedPp(pp);
                    mopedPerson.setMopedHousehold(mopedHousehold);
                    for (MitoTrip tt : pp.getTrips()) {
                        MopedTrip mopedTrip = convertToMopedTt(tt);
                        if (mopedTrip.getTripPurpose().equals(Purpose.HBW) || mopedTrip.getTripPurpose().equals(Purpose.HBE)) {
                            //if(mopedPerson.getOccupation().equals(Occupation.STUDENT)||mopedPerson.getOccupation().equals(Occupation.WORKER)){
                            mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                            mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                        }

                        if (mopedTrip.getTripPurpose().equals(Purpose.HBS) || mopedTrip.getTripPurpose().equals(Purpose.HBO)) {
                            mopedTrip.setTripOrigin(mopedHousehold.getHomeZone());
                        }

                        if (mopedTrip.getTripPurpose().equals(Purpose.NHBW)) {
                            mopedTrip.setTripDestination(mopedPerson.getOccupationZone());
                        }

                        mopedPerson.addTrip(mopedTrip);
                        if (mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).isEmpty()) {
                            mopedHousehold.setTripsByPurpose(new ArrayList<MopedTrip>(), mopedTrip.getTripPurpose());
                        }
                        mopedHousehold.getTripsForPurpose(mopedTrip.getTripPurpose()).add(mopedTrip);
                    }
                    mopedHousehold.addPerson(mopedPerson);
                }
                households.put(mopedHousehold.getId(), mopedHousehold);
                counter++;
            }
        }

        logger.warn(failedMatchingMopedZoneCounter + " home/job locations failed to be located to a moped zone!");
    }

    private boolean hasTrip(MitoHousehold hh) {
        for(de.tum.bgu.msm.data.Purpose purpose : de.tum.bgu.msm.data.Purpose.values()){
            if(!hh.getTripsForPurpose(purpose).isEmpty()){
                return true;
            };
        }

        return false;
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
        String file = Resources.instance.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + dataSet.getYear() + "/microData/mopedTrips_" + purpose + ".csv";
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
            pwh.print(trip.getMopedTripDistance());
            pwh.print(",");
            pwh.println(trip.getTripMode());

        }
        pwh.close();
    }


}
