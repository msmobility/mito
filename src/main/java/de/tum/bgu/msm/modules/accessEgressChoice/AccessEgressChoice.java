package de.tum.bgu.msm.modules.accessEgressChoice;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.accessTimes.AccessAndEgressVariables;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import de.tum.bgu.msm.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.io.PrintWriter;

public class AccessEgressChoice extends Module {

    private final String scenarioName;
    private final static Logger logger = Logger.getLogger(AccessEgressChoice.class);

    public AccessEgressChoice (DataSet dataSet, String scenarioName) {
        super(dataSet);
        this.scenarioName = scenarioName;
    }

    @Override
    public void run() {
        accessEgressByPurpose();
    }

    private void accessEgressByPurpose() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        for (Purpose purpose : Purpose.values()) {
            executor.addTaskToQueue(new AccessEgressByPurpose(purpose, dataSet));
        }
        executor.execute();
        writeAccessEgressDetail();
    }

    static class AccessEgressByPurpose extends RandomizableConcurrentFunction<Void> {
        private final Purpose purpose;
        private final DataSet dataSet;
        private final TravelTimes travelTimes;
        private final AccessEgressJSCalculatorTransit transitCalculator;
        private final AccessEgressJSCalculatorUAM uamAccessCalculator;
        private final AccessEgressJSCalculatorUAM uamEgressCalculator;
        private int countTripsSkipped;

        AccessEgressByPurpose(Purpose purpose, DataSet dataSet) {
            super(MitoUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.travelTimes = dataSet.getTravelTimes();
            this.transitCalculator = new AccessEgressJSCalculatorTransit(new InputStreamReader(this.getClass().getResourceAsStream("SecondaryModeTransit")),purpose);
            this.uamAccessCalculator = new AccessEgressJSCalculatorUAM(new InputStreamReader(this.getClass().getResourceAsStream("AccessUAM")),purpose);
            this.uamEgressCalculator = new AccessEgressJSCalculatorUAM(new InputStreamReader(this.getClass().getResourceAsStream("EgressUAM")),purpose);
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            try {
                for (MitoHousehold household : dataSet.getHouseholds().values()) {
                    for (MitoTrip trip : household.getTripsForPurpose(purpose)) {
                        if(Mode.train.equals(trip.getTripMode()) || Mode.tramOrMetro.equals(trip.getTripMode()) || Mode.bus.equals(trip.getTripMode())) {
                            chooseTransitAccessAndEgressMode(trip, calculateSecondaryModeProbabilities(household, trip));
                        }
                        else if(Mode.uam.equals(trip.getTripMode())) {
                            chooseUamAccessAndEgressMode(trip,calculateUamAccessProbabilities(household,trip),calculateUamEgressProbabilities(household,trip));
                        }
                        else {
                            trip.setAccessMode(null);
                            trip.setEgressMode(null);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info(countTripsSkipped + " secondary mode calculations skipped for " + purpose);
            return null;
        }

        double[] calculateSecondaryModeProbabilities(MitoHousehold household, MitoTrip trip) {
            if (trip.getTripOrigin() == null) {
                countTripsSkipped++;
                return null;
            }
            final int originId = trip.getTripOrigin().getZoneId();
            final MitoZone origin = dataSet.getZones().get(originId);
            return transitCalculator.calculateSecondaryModeProbabilities(household, trip.getPerson(), trip.getTripMode(), origin);
        }

        double[] calculateUamAccessProbabilities (MitoHousehold household, MitoTrip trip) {
            if(trip.getTripOrigin() == null) {
                countTripsSkipped++;
                return null;
            }
            final int originId = trip.getTripOrigin().getZoneId();
            final int destinationId = trip.getTripDestination().getZoneId();
            final MitoZone origin = dataSet.getZones().get(originId);
            final MitoZone destination = dataSet.getZones().get(destinationId);
            final int accessVertiportId = (int) dataSet.getAccessAndEgressVariables().getAccessVariable(origin,destination, "uam", AccessAndEgressVariables.AccessVariable.ACCESS_VERTIPORT);
            final MitoZone accessVeriport = dataSet.getZones().get(accessVertiportId);
            if(accessVeriport == null) {
                logger.info("vertiport ID " + accessVertiportId + " for origin " + originId + " and destination " + destinationId + " could not be found");
                countTripsSkipped++;
                return null;
            }
            final double accessDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(originId,accessVertiportId);
            final double accessDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(originId,accessVertiportId);
            return uamAccessCalculator.calculateProbabilities(household,trip.getPerson(),origin,accessVeriport,travelTimes,accessDistanceAuto,accessDistanceNMT,dataSet.getPeakHour());
        }

        double[] calculateUamEgressProbabilities (MitoHousehold household, MitoTrip trip) {
            if(trip.getTripOrigin() == null) {
                countTripsSkipped++;
                return null;
            }
            final int originId = trip.getTripOrigin().getZoneId();
            final int destinationId = trip.getTripDestination().getZoneId();
            final MitoZone origin = dataSet.getZones().get(originId);
            final MitoZone destination = dataSet.getZones().get(destinationId);
            final int egressVertiportId = (int) dataSet.getAccessAndEgressVariables().getAccessVariable(origin,destination, "uam", AccessAndEgressVariables.AccessVariable.EGRESS_VERTIPORT);
            final MitoZone egressVeriport = dataSet.getZones().get(egressVertiportId);
            if(egressVeriport == null) {
                logger.info("vertiport ID " + egressVertiportId + " for origin " + originId + " and destination " + destinationId + " could not be found");
                countTripsSkipped++;
                return null;
            }
            final double egressDistanceAuto = dataSet.getTravelDistancesAuto().getTravelDistance(egressVertiportId,destinationId);
            final double egressDistanceNMT = dataSet.getTravelDistancesNMT().getTravelDistance(egressVertiportId,destinationId);
            return uamEgressCalculator.calculateProbabilities(household,trip.getPerson(),destination,egressVeriport,travelTimes,egressDistanceAuto,egressDistanceNMT,dataSet.getPeakHour());
        }

        private void chooseTransitAccessAndEgressMode(MitoTrip trip, double[] probabilities) {

            double sum = MitoUtil.getSum(probabilities);
            Mode secondaryMode = Mode.valueOf(MitoUtil.select(probabilities, random, sum));

            final int originId = trip.getTripOrigin().getZoneId();
            final int destinationId = trip.getTripDestination().getZoneId();

            if(trip.getTripPurpose().equals(Purpose.AIRPORT)) {
                if (destinationId == 1659 && originId == 1659) {
                    trip.setAccessMode(Mode.walk);
                    trip.setEgressMode(Mode.walk);
                }
                else if (destinationId == 1659) {
                    trip.setAccessMode(secondaryMode);
                    trip.setEgressMode(Mode.walk);
                }
                else if (originId == 1659) {
                    trip.setAccessMode(Mode.walk);
                    trip.setEgressMode(secondaryMode);
                }
                else {
                    logger.info("Airport Trip " + trip.getId() + "does not begin or end at the airport!");
                    trip.setAccessMode(null);
                    trip.setEgressMode(null);
                }
            }
            else {
                final double accessDistance = dataSet.getZones().get(originId).getDistanceToNearestRailStop();
                final double egressDistance = dataSet.getZones().get(destinationId).getDistanceToNearestRailStop();
                double distanceRatio = accessDistance / egressDistance;
                double cutoffRatio;
                if(trip.getTripPurpose().equals(Purpose.NHBW) || trip.getTripPurpose().equals(Purpose.NHBO)) {
                    cutoffRatio = 1;
                }
                else {
                    switch (secondaryMode) {
                        case autoDriver:        cutoffRatio = 0.436;  break;
                        case autoPassenger:     cutoffRatio = 0.768;  break;
                        case bicycle:           cutoffRatio = 0.419;  break;
                        default:                cutoffRatio = 0;
                    }
                }
                if (distanceRatio > cutoffRatio) {
                    trip.setAccessMode(secondaryMode);
                    trip.setEgressMode(Mode.walk);
                } else {
                    trip.setAccessMode(Mode.walk);
                    trip.setEgressMode(secondaryMode);
                }
            }
        }

        private void chooseUamAccessAndEgressMode (MitoTrip trip, double[] accessProbabilities, double[] egressProbabilities) {
            if(trip.getTripPurpose().equals(Purpose.AIRPORT) && trip.getTripOrigin().getZoneId() == 1659) {
                trip.setAccessMode(Mode.walk);
            }
            else {
                if(accessProbabilities != null) {
                    double accessSum = MitoUtil.getSum(accessProbabilities);
                    Mode accessMode = Mode.valueOf(MitoUtil.select(accessProbabilities, random, accessSum));
                    trip.setAccessMode(accessMode);
                } else {
                    trip.setAccessMode(null);
                }
            }

            if(trip.getTripPurpose().equals(Purpose.AIRPORT) && trip.getTripDestination().getZoneId() == 1659) {
                trip.setEgressMode(Mode.walk);
            }
            else {
                if(egressProbabilities != null) {
                    double egressSum = MitoUtil.getSum(egressProbabilities);
                    Mode egressMode = Mode.valueOf(MitoUtil.select(egressProbabilities, random, egressSum));
                    trip.setEgressMode(egressMode);
                } else {
                    trip.setEgressMode(null);
                }
            }
        }
    }

    private void writeAccessEgressDetail() {
        String outputSubDirectory = "scenOutput/" + scenarioName + "/";
        logger.info(" Writing Detailed Access Egress Information");
        String file = Resources.INSTANCE.getString(Properties.BASE_DIRECTORY) + "/" + outputSubDirectory + dataSet.getYear() + "/microData/SecondaryModeChoiceDetail.csv";
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(file, false);
        pwh.println("tripID,purpose,originZone,destinationZone,accessDistance,egressDistance,MODE,SECONDARYMODE,ACCESSMODE,EGRESSMODE");
        for(MitoTrip trip : dataSet.getTrips().values()) {
            if(trip != null) {
                if(Mode.train.equals(trip.getTripMode()) || Mode.tramOrMetro.equals(trip.getTripMode()) || Mode.bus.equals(trip.getTripMode())) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(trip.getId());
                    builder.append(",");
                    builder.append(trip.getTripPurpose());
                    builder.append(",");
                    builder.append(trip.getTripOrigin().getZoneId());
                    builder.append(",");
                    builder.append(trip.getTripDestination().getZoneId());
                    builder.append(",");
                    builder.append(dataSet.getZones().get(trip.getTripOrigin().getZoneId()).getDistanceToNearestRailStop());
                    builder.append(",");
                    builder.append(dataSet.getZones().get(trip.getTripDestination().getZoneId()).getDistanceToNearestRailStop());
                    builder.append(",");
                    builder.append(trip.getTripMode());
                    builder.append(",");
                    builder.append(trip.getAccessMode().equals(Mode.walk) ? trip.getEgressMode() : trip.getAccessMode());
                    builder.append(",");
                    builder.append(trip.getAccessMode());
                    builder.append(",");
                    builder.append(trip.getEgressMode());
                    pwh.println(builder.toString());
                    }
                if(Mode.uam.equals(trip.getTripMode())) {
                    int accessVertiportId = (int) dataSet.getAccessAndEgressVariables().getAccessVariable(trip.getTripOrigin(),trip.getTripDestination(), "uam", AccessAndEgressVariables.AccessVariable.ACCESS_VERTIPORT);
                    int egressVertiportId = (int) dataSet.getAccessAndEgressVariables().getAccessVariable(trip.getTripOrigin(),trip.getTripDestination(), "uam", AccessAndEgressVariables.AccessVariable.EGRESS_VERTIPORT);
                    StringBuilder builder = new StringBuilder();
                    builder.append(trip.getId());
                    builder.append(",");
                    builder.append(trip.getTripPurpose());
                    builder.append(",");
                    builder.append(trip.getTripOrigin().getZoneId());
                    builder.append(",");
                    builder.append(trip.getTripDestination().getZoneId());
                    builder.append(",");
                    builder.append(dataSet.getTravelDistancesAuto().getTravelDistance(trip.getTripOrigin().getZoneId(),accessVertiportId));
                    builder.append(",");
                    builder.append(dataSet.getTravelDistancesAuto().getTravelDistance(egressVertiportId,trip.getTripDestination().getZoneId()));
                    builder.append(",");
                    builder.append(trip.getTripMode());
                    builder.append(",,");
                    builder.append(trip.getAccessMode());
                    builder.append(",");
                    builder.append(trip.getEgressMode());
                    pwh.println(builder.toString());
                    }
                }
            }
        pwh.close();
    }



}
