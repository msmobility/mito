package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.data.jobTypes.JobTypeFactory;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


public class SyntheticPopulationReaderGermany extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(SyntheticPopulationReaderGermany.class);
    private final JobTypeFactory factory;

    // Person
    private int posId = -1;
    private int posHhId = -1;
    private int posAge = -1;
    private int posSex = -1;
    private int posOccupation = -1;
    private int posWorkplaceId = -1;
    private int posLicence = -1;
    private int posIncome = -1;
    private int posType = -1;
    private int posSchoolId = -1;
    private int posJobId = -1;
    private int posZone = -1;
    private int posJobCoordX = -1;
    private int posJobCoordY = -1;
    //private int posWorker = -1; // question

    private int occupationCounter = 0;
    private int tooLongCommuteCounter;


    // constructor
    public SyntheticPopulationReaderGermany(DataSet dataSet, JobTypeFactory factory) {
        super(dataSet);
        this.factory = factory;
    }


    @Override
    public void read() {
        logger.info("  Reading person micro data from ascii file");
        Path filePath = Resources.instance.getPersonsFilePath();
        super.read(filePath, ",");
        int noIncomeHouseholds = 0;
        for(MitoHousehold household: dataSet.getHouseholds().values()) {
            if(household.getMonthlyIncome_EUR() == 0) {
                noIncomeHouseholds++;
            }
        }
        if(noIncomeHouseholds > 0) {
            logger.warn("There are " + noIncomeHouseholds + " households with no income after reading all persons.");
        }
        logger.info("There are " + occupationCounter + " persons without occupation (student or worker).");
        logger.warn("Remove occupatioon of too long trips of " + tooLongCommuteCounter + " persons");
    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posId = headerList.indexOf("id");
        //posWorker = headerList.indexOf("id"); //
        posHhId = headerList.indexOf("hhid");
        posAge = headerList.indexOf("age");
        posSex = headerList.indexOf("gender");
        posOccupation = headerList.indexOf("occupation");
        posLicence = headerList.indexOf("driversLicense");
        posWorkplaceId = headerList.indexOf("JobId");
        posIncome = headerList.indexOf("income");
        //jobType
        posType = headerList.indexOf("jobType");
        posSchoolId = headerList.indexOf("schoolId");
        //jobId
        posJobId = headerList.indexOf("JobId");
        //jobZone
        posZone = headerList.indexOf("zone");
        //jobCoordX & jobCoordY
        posJobCoordX = headerList.indexOf("jobCoordX");
        posJobCoordY = headerList.indexOf("jobCoordY");


    }

    @Override
    public void processRecord(String[] record) {

        final int id = Integer.parseInt(record[posId]);
        final int hhid = Integer.parseInt(record[posHhId]);

        if(!dataSet.getHouseholds().containsKey(hhid)) {
            //logger.warn("Person " + id + " refers to non-existing household " + hhid + ". Ignoring this person.");
        } else {
            MitoHousehold hh = dataSet.getHouseholds().get(hhid);

            final int age = Integer.parseInt(record[posAge]);

            final int genderCode = Integer.parseInt(record[posSex]);
            MitoGender mitoGender = MitoGender.valueOf(genderCode);

            final int occupationCode = Integer.parseInt(record[posOccupation]);
            MitoOccupationStatus mitoOccupationStatus = MitoOccupationStatus.valueOf(occupationCode);


            //int workplace = -1;
            //try{
            //    workplace = Integer.parseInt(record[posWorkplaceId]);
            // } catch (Exception e ){
            //logger.warn("No Work ID");
            //}

            //final int workplace = Integer.parseInt(record[posWorkplaceId]);
            final int school = Integer.parseInt(record[posSchoolId]);

            final boolean driversLicense = Boolean.parseBoolean(record[posLicence]);
            //final boolean driversLicense = MitoGender.obtainLicense(mitoGender, age); // new, added by Alona, Quick fix for drivers license


            //the SP of Germany has monthly_income
            int monthlyIncome_EUR = Integer.parseInt(record[posIncome]);
            hh.addIncome(monthlyIncome_EUR);

            MitoOccupation occupation = null;

            switch (mitoOccupationStatus) {
                case WORKER:
                    //if(dataSet.getJobs().containsKey(workplace)) {
                    int jobId = Integer.parseInt(record[posJobId]);
                    int zoneId = Integer.parseInt(record[posZone]);
                    String type = record[posType];
                    MitoZone zone = dataSet.getZones().get(zoneId);
                    if (zone == null) {
                        logger.warn(String.format("Job %d refers to non-existing zone %d! Ignoring it.",jobId, zoneId));
                        //return null;
                    }
                    try {
                        zone.addEmployeeForType(factory.getType(type.toUpperCase().replaceAll("\"","")));
                    } catch (IllegalArgumentException e) {
                        //logger.error("Job Type " + type + " used in job microdata but is not defined");
                    }

                    //Coordinate coordinate = zone.getRandomCoord(MitoUtil.getRandomObject());
                    Coordinate coordinate = (new Coordinate(Double.parseDouble(record[posJobCoordX]),
                            Double.parseDouble(record[posJobCoordY])));

                    //Coordinate coordinate = new Coordinate(Double.parseDouble(record[posJobCoordX]), Double.parseDouble(record[posJobCoordY]));

                    MitoJob job = new MitoJob(zone, coordinate, jobId);
                    dataSet.addJob(job);

                    int workplace = Integer.parseInt(record[posWorkplaceId]);
                    occupation = (dataSet.getJobs().get(workplace));

                /*} else {
                    logger.warn("Person " + id + " declared as worker does not have a valid job!");
                }*/
                    break;
                case STUDENT:
                    if(dataSet.getSchools().containsKey(school)) {
                        occupation = (dataSet.getSchools().get(school));
                    } else {
                        //logger.warn("Person " + id + " declared as student does not have a valid school!");
                    }
                    break;
                case UNEMPLOYED:
                default:
                    logger.debug("Person " + id + " does not have an occupation.");
                    occupationCounter++;
                    break;
            }

            if (occupation != null){
                if (dataSet.getTravelDistancesAuto().getTravelDistance(hh.getHomeZone().getZoneId(), occupation.getZoneId()) > 200) {
                    occupation = null;
                    tooLongCommuteCounter++;
                }
            }

            MitoPerson pp = new MitoPerson(id, hh, mitoOccupationStatus, occupation, age, mitoGender, driversLicense);

            //int worker = Integer.parseInt(record[posOccupation]); //int worker = Integer.parseInt(record[posWorker]);

            //if (worker == 1) {


            //MitoPerson pp = new MitoPerson(id, mitoOccupationStatus, occupation, age, mitoGender, driversLicense);

            hh.addPerson(pp);
            dataSet.addPerson(pp);
        }



    }
}
