package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;


public class PersonsReader extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(PersonsReader.class);

    private int posId = -1;
    private int posHhId = -1;
    private int posAge = -1;
    private int posSex = -1;
    private int posOccupation = -1;
    private int posWorkplaceId = -1;
    private int posLicence = -1;
    private int posIncome = -1;
    private int posSchoolId = -1;
    private int posDisability = -1;

    private int occupationCounter = 0;
    private int mentalDisabilityCounter = 0;
    private int physicalDisabilityCounter = 0;
    private int withoutDisabilityCounter = 0;

    public PersonsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading person micro data from ascii file");
        String fileName = Resources.INSTANCE.getString(Properties.PERSONS);
        super.read(fileName, ",");
        int noIncomeHouseholds = 0;
        for(MitoHousehold household: dataSet.getHouseholds().values()) {
            if(household.getIncome() == 0) {
                noIncomeHouseholds++;
            }
        }
        if(noIncomeHouseholds > 0) {
            logger.warn("There are " + noIncomeHouseholds + " households with no income after reading all persons.");
        }
        logger.info("There are " + occupationCounter + " persons without occupation (student or worker).");
        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
            logger.info("There are " + withoutDisabilityCounter + " persons without severe disabilities.");
            logger.info("There are " + mentalDisabilityCounter + " persons with severe mental disability.");
            logger.info("There are " + physicalDisabilityCounter + " persons with severe physical disability.");
        }
    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posId = headerList.indexOf("id");
        posHhId = headerList.indexOf("hhid");
        posAge = headerList.indexOf("age");
        posSex = headerList.indexOf("gender");
        posOccupation = headerList.indexOf("occupation");
        posWorkplaceId = headerList.indexOf("workplace");
        posSchoolId = headerList.indexOf("schoolId");
        posLicence = headerList.indexOf("driversLicense");
        posIncome = headerList.indexOf("income");
        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
            posDisability = headerList.indexOf("disability");
        }
    }

    @Override
    public void processRecord(String[] record) {

        final int id = Integer.parseInt(record[posId]);
        final int hhid = Integer.parseInt(record[posHhId]);

        if(!dataSet.getHouseholds().containsKey(hhid)) {
            //logger.warn("Person " + id + " refers to non-existing household " + hhid + ". Ignoring this person.");
            return;
        }
        MitoHousehold hh = dataSet.getHouseholds().get(hhid);

        final int age = Integer.parseInt(record[posAge]);

        final int genderCode = Integer.parseInt(record[posSex]);
        MitoGender mitoGender = MitoGender.valueOf(genderCode);

        final int occupationCode = Integer.parseInt(record[posOccupation]);
        MitoOccupationStatus mitoOccupationStatus = MitoOccupationStatus.valueOf(occupationCode);

        final int workplace = Integer.parseInt(record[posWorkplaceId]);
        final int school = Integer.parseInt(record[posSchoolId]);

        final boolean driversLicense = Boolean.parseBoolean(record[posLicence]);

        int income = Integer.parseInt(record[posIncome]);
        hh.addIncome(income);

        MitoOccupation occupation = null;

        switch (mitoOccupationStatus) {
            case WORKER:
                if(dataSet.getJobs().containsKey(workplace)) {
                    occupation = (dataSet.getJobs().get(workplace));
                } else {
                    logger.warn("Person " + id + " declared as student does not have a valid school!");
                }
                break;
            case STUDENT:
                if(dataSet.getSchools().containsKey(school)) {
                    occupation = (dataSet.getSchools().get(school));
                } else {
                    logger.warn("Person " + id + " declared as student does not have a valid school!");
                }
                break;
            case UNEMPLOYED:
            default:
                logger.debug("Person " + id + " does not have an occupation.");
                occupationCounter++;
                break;
        }

        MitoPerson pp = new MitoPerson(id, mitoOccupationStatus, occupation, age, mitoGender, driversLicense);
        if (Resources.INSTANCE.getBoolean(Properties.RUN_DISABILITY)) {
            Disability disability = Disability.valueOf(record[posDisability]);
            pp.setDisability(disability);
            switch (disability){
                case WITHOUT:
                    withoutDisabilityCounter++;
                    break;
                case PHYSICAL:
                    physicalDisabilityCounter++;
                    break;
                case MENTAL:
                    mentalDisabilityCounter++;
                    break;
                default:
                    break;
            }
        }

        hh.addPerson(pp);
        dataSet.addPerson(pp);
    }
}
