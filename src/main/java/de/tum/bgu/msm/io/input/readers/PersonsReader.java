package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

import java.util.Arrays;
import java.util.List;


public class PersonsReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(PersonsReader.class);

    private int posId = -1;
    private int posHhId = -1;
    private int posAge = -1;
    private int posSex = -1;
    private int posOccupation = -1;
    private int posWorkplace = -1;
    private int posLicence = -1;
    private int posIncome = -1;
    private int posSchool = -1;
    private int posSchoolCoordX = -1;
    private int posSchoolCoordY = -1;

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
    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posId = headerList.indexOf("id");
        posHhId = headerList.indexOf("hhid");
        posAge = headerList.indexOf("age");
        posSex = headerList.indexOf("gender");
        posOccupation = headerList.indexOf("occupation");
        posWorkplace = headerList.indexOf("workplace");
        posSchoolCoordX = headerList.indexOf("schoolCoordX");
        posSchoolCoordY = headerList.indexOf("schoolCoordY");
        posSchool = headerList.indexOf("schoolTAZ");
        posLicence = headerList.indexOf("driversLicense");
        posIncome = headerList.indexOf("income");
        posSchool = headerList.indexOf("schoolTAZ");
    }

    @Override
    public void processRecord(String[] record) {

        final int id = Integer.parseInt(record[posId]);
        final int hhid = Integer.parseInt(record[posHhId]);

        if(!dataSet.getHouseholds().containsKey(hhid)) {
            logger.warn("Person " + id + " refers to non-existing household " + hhid + ". Ignoring this person.");
            return;
        }
        MitoHousehold hh = dataSet.getHouseholds().get(hhid);

        final int age = Integer.parseInt(record[posAge]);

        final int genderCode = Integer.parseInt(record[posSex]);
        Gender gender = Gender.valueOf(genderCode);

        final int occupationCode = Integer.parseInt(record[posOccupation]);
        Occupation occupation = Occupation.valueOf(occupationCode);

        final int workplace = Integer.parseInt(record[posWorkplace]);

        final boolean driversLicense = Boolean.parseBoolean(record[posLicence]);

        int income = Integer.parseInt(record[posIncome]);
        hh.addIncome(income);

        MitoPerson pp = new MitoPerson(id, occupation, workplace, age, gender, driversLicense);
        if(occupation == Occupation.STUDENT) {
            final int schoolZone = Integer.parseInt(record[posSchool]);
            if(dataSet.getZones().containsKey(schoolZone)) {
                pp.setOccupationZone(dataSet.getZones().get(schoolZone));
                pp.setOccupationLocation(new MicroLocation(Double.parseDouble(record[posSchoolCoordX]),Double.parseDouble(record[posSchoolCoordY]), null));
            } else {
                logger.warn("Person " + id + " declared as student does not have a school TAZ!");
            }
        }

        hh.addPerson(pp);
        dataSet.addPerson(pp);
    }
}
