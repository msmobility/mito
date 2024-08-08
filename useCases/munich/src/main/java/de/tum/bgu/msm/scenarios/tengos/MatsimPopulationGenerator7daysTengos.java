package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.modules.plansConverter.MatsimPopulationGenerator;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class MatsimPopulationGenerator7daysTengos extends Module {

    private static final Logger logger = Logger.getLogger(MatsimPopulationGenerator.class);

    private final Set<Mode> modeSet = new HashSet<>();

    public MatsimPopulationGenerator7daysTengos(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
        String[] networkModes = Resources.instance.getArray(Properties.MATSIM_NETWORK_MODES, new String[]{"autoDriver"});
        String[] teleportedModes = Resources.instance.getArray(Properties.MATSIM_TELEPORTED_MODES, new String[]{});
        for (String mode : networkModes){
            modeSet.add(Mode.valueOf(mode));
        }
        for (String mode : teleportedModes){
            modeSet.add(Mode.valueOf(mode));
        }
    }

    @Override
    public void run() {
        Population population = generateMatsimPopulation();
        dataSet.setPopulation(population);
    }

    private Population generateMatsimPopulation(){
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory factory = population.getFactory();
        AtomicInteger assignedTripCounter = new AtomicInteger(0);
        AtomicInteger nonAssignedTripCounter = new AtomicInteger(0);
        dataSet.getTripSubsample().values().forEach(trip ->{
            try {
                if (modeSet.contains(trip.getTripMode()) && !trip.getTripPurpose().equals(Purpose.RRT)) {
                    Person person = factory.createPerson(Id.createPersonId(trip.getId()));
                    person.getAttributes().putAttribute("age", Math.min(trip.getPerson().getAge(), 100));
                    person.getAttributes().putAttribute("sex",trip.getPerson().getMitoGender());
                    //use for 7 day extension
                    person.getAttributes().putAttribute("day",((MitoTrip7days)trip).getDepartureDay().toString());
                    trip.setMatsimPerson(person);

                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    String activityTypeAtOrigin = getOriginActivity(trip);

                    //For all HB trips that person lives in a nursing_home, home end activity is “nursing_home”
                    if (trip.isHomeBased()) {
                        if(((MitoHouseholdTengos)trip.getPerson().getHousehold()).isNursingHome()){
                            activityTypeAtOrigin = "nursing_home";
                        }
                    }

                    Coord originCoord;
                    if(trip.getTripOrigin() instanceof MicroLocation) {
                        originCoord = CoordUtils.createCoord(((MicroLocation) trip.getTripOrigin()).getCoordinate());
                    } else {
                        originCoord =
                                CoordUtils.createCoord(dataSet.getZones().get(trip.getTripOrigin().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                    }

                    Activity originActivity = factory.createActivityFromCoord(activityTypeAtOrigin, originCoord);
                    originActivity.setEndTime(trip.getDepartureInMinutes() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(Mode.getMatsimMode(trip.getTripMode())));

                    String activityTypeAtDestination = getDestinationActivity(trip);

                    //For work trips that worker’s job type is NURSING, destination end activity is “nursing_home”
                    if(activityTypeAtDestination.equals("work")){
                        MitoOccupationImpl occupation = (MitoOccupationImpl)trip.getPerson().getOccupation();
                        if(occupation!=null){
                            if(MunichJobTypeTengos.NURSINGHOME.equals(((MitoOccupationImplTengos)occupation).getJobType())){
                                activityTypeAtDestination = "nursing_work";
                            }
                        }
                    }

                    Coord destinationCoord;
                    if(trip.getTripDestination() instanceof MicroLocation) {
                        destinationCoord = CoordUtils.createCoord(((MicroLocation) trip.getTripDestination()).getCoordinate());
                    } else {
                        destinationCoord = CoordUtils.createCoord(dataSet.getZones().get(trip.getTripDestination().getZoneId()).getRandomCoord(MitoUtil.getRandomObject()));
                        ((MitoTripTengos)trip).setDestinationCoord(destinationCoord);
                    }
                    Activity destinationActivity = factory.createActivityFromCoord(activityTypeAtDestination, destinationCoord);

                    if (trip.isHomeBased()) {
                        destinationActivity.setEndTime(trip.getDepartureInMinutesReturnTrip() * 60 + MitoUtil.getRandomObject().nextDouble() * 60);
                        plan.addActivity(destinationActivity);
                        plan.addLeg(factory.createLeg(Mode.getMatsimMode(trip.getTripMode())));
                        plan.addActivity(factory.createActivityFromCoord(activityTypeAtOrigin, originCoord));
                    } else {
                        plan.addActivity(destinationActivity);
                    }

                }
            } catch (Exception e){
                nonAssignedTripCounter.incrementAndGet();
            }

            if (ConcurrencyUtils.isPowerOf2(assignedTripCounter.incrementAndGet())){
                logger.warn( assignedTripCounter.get()  + " MATSim agents created");
            }

        });
        logger.warn( nonAssignedTripCounter.get()  + " trips do not have trip origin, destination or mode and cannot be assigned in MATSim");
        return population;
    }


    private static String getOriginActivity(MitoTrip trip){
        Purpose purpose = trip.getTripPurpose();
        if (purpose.equals(Purpose.NHBW)){
            return "work";
        } else if (purpose.equals(Purpose.NHBO)){
            return "other";
        } else if (purpose.equals(Purpose.AIRPORT)) {
            if (trip.getTripOrigin().getZoneId() == Resources.instance.getInt(Properties.AIRPORT_ZONE)){
                return "airport";
            } else {
                return "home";
            }
        } else {
            return "home";
        }
    }

    private static String getDestinationActivity(MitoTrip trip){
        Purpose purpose = trip.getTripPurpose();
        if (purpose.equals(Purpose.HBW)){
            return "work";
        } else if (purpose.equals(Purpose.HBE)){
            return "education";
        } else if (purpose.equals(Purpose.HBS)){
            return "shopping";
        } else if (purpose.equals(Purpose.HBR)) {
            return "recreation";
        } else if (purpose.equals(Purpose.AIRPORT)) {
            if (trip.getTripDestination().getZoneId() == Resources.instance.getInt(Properties.AIRPORT_ZONE)) {
                return "airport";
            } else {
                return "home";
            }
        } else {
            return "other";
        }
    }
}
