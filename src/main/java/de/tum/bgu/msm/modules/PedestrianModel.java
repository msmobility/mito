package de.tum.bgu.msm.modules;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.data.Location;
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
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tum.bgu.msm.io.output.SummarizeData.writeOutTrips;


public class PedestrianModel {

    private static final Logger logger = Logger.getLogger( PedestrianModel.class );
    private MoPeDModel mopedModel;
    private final DataSet dataSet;
    private final String propertiesPath;
    private Map<Integer, MopedHousehold>  households = new HashMap<>();
    private Quadtree mopedZoneQuadTree = new Quadtree();
    private SparseFloatMatrix2D mopedTravelDistance;

    public PedestrianModel(DataSet dataSet) {
        this.dataSet = dataSet;
        this.propertiesPath = Resources.INSTANCE.getString(Properties.MOPED_PROPERTIES); //TODO: propoertiesPath MITO?
    }

    public void runMoped() {
        this.mopedModel = MoPeDModel.initializeModelFromMito(propertiesPath);
        mopedModel.getManager().readZoneData();
        mopedModel.getManager().readDistanceData();
        updateData(dataSet.getYear());
        logger.info("  Running pedestrian model MITO for the year ");
        mopedModel.runAgentBasedModel();
        feedDataBackToMito();
        writeOutMoPeDTrips(dataSet, Resources.INSTANCE.getString(Properties.SCENARIO_NAME));
    }

    private void feedDataBackToMito() {
        for(MopedTrip mopedTrip : mopedModel.getDataSet().getTrips().values()){
            MitoTrip mitoTrip = dataSet.getTrips().get(mopedTrip.getId());
            if(mopedTrip.isWalkMode()){
                mitoTrip.setTripMode(Mode.walk);
                if(mopedTrip.getTripOrigin()!=null&&mopedTrip.getTripDestination()!=null) {
                    mitoTrip.setTripDestination(dataSet.getZones().get(mopedTrip.getTripOrigin().getMitoZoneId()));
                    mitoTrip.setTripOrigin(dataSet.getZones().get(mopedTrip.getTripDestination().getMitoZoneId()));
                }else{
                    //logger.warn("trip id: " + mitoTrip.getTripId()+ " purpose: " + mitoTrip.getTripPurpose() + " has no origin or destination: " + mopedTrip.getTripOrigin() + "," + mopedTrip.getTripDestination());
                }
                //TODO: travel time budget?
                //double newTravelBudget = dataSet.getHouseholds().get(mopedTrip.getPerson().getMopedHousehold().getId()).getTravelTimeBudgetForPurpose(mitoTrip.getTripPurpose()) - mopedTrip.getTripDistance();
            }
        }
    }

    private void updateData(int year) {
        prepareMopedZoneSearchTree();
        convertHhs();
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
            GeometryFactory gf = new GeometryFactory();
            MopedZone occupationZone = locateMicrolationToMopedZone(gf,new Coordinate(pp.getOccupation().getCoordinate().x,pp.getOccupation().getCoordinate().y));
            mopedPerson.setOccupationZone(occupationZone);
        }
        return mopedPerson;
    }

    private MopedHousehold convertToMopedHh(MitoHousehold mitoHh) {
        int children = DataSet.getChildrenForHousehold(mitoHh);
        GeometryFactory gf = new GeometryFactory();
        MopedZone homeZone = locateMicrolationToMopedZone(gf,new Coordinate(mitoHh.getCoordinate().x,mitoHh.getCoordinate().y));
        return new MopedHousehold(mitoHh.getId(),mitoHh.getMonthlyIncome_EUR(),mitoHh.getAutos(),children,homeZone);
    }


    private MopedZone locateMicrolationToMopedZone(GeometryFactory gf, Coordinate coordinate){
        Point point = gf.createPoint(coordinate);
        List<MopedZone> mopedZones = mopedZoneQuadTree.query(point.getEnvelopeInternal());

        for (MopedZone mopedZone : mopedZones){
            if(((Geometry)mopedZone.getShapeFeature().getDefaultGeometry()).contains(point)){
                return mopedZone;
            }
        }

        //TODO: how to deal with null?
        return null;
    }


    private void writeOutMoPeDTrips(DataSet dataSet, String scenarioName) {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";

        logger.info("  Writing moped trips file");
        String file = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + dataSet.getYear() + "/microData/mopedTrips.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("id,origin,destination,purpose,person,distance,mode");
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
            Location destination = trip.getTripDestination();
            String destinationId = "null";
            if(destination != null) {
                destinationId = String.valueOf(destination.getZoneId());
            }
            pwh.print(destinationId);
            pwh.print(",");
            pwh.print(trip.getTripPurpose());
            pwh.print(",");
            pwh.print(trip.getPerson().getId());
            pwh.print(",");
            if(origin != null && destination != null) {
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
