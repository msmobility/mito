package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.data.Gender;
import de.tum.bgu.msm.data.Occupation;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;


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
        posId = MitoUtil.findPositionInArray("id", header);
        posHhId = MitoUtil.findPositionInArray("hhid", header);
        posAge = MitoUtil.findPositionInArray("age", header);
        posSex = MitoUtil.findPositionInArray("gender", header);
        posOccupation = MitoUtil.findPositionInArray("occupation", header);
        posWorkplace = MitoUtil.findPositionInArray("workplace", header);
        posLicence = MitoUtil.findPositionInArray("driversLicense", header);
        posIncome = MitoUtil.findPositionInArray("income", header);
    }

    @Override
    public void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        int hhid = Integer.parseInt(record[posHhId]);
        MitoHousehold hh;
        if(dataSet.getHouseholds().containsKey(hhid)) {
            hh = dataSet.getHouseholds().get(hhid);
        } else {
            logger.warn("Person " + id + " refers to non-existing household " + hhid + ". Ignoring this person.");
            return;
        }
        int age = Integer.parseInt(record[posAge]);

        int genderCode = Integer.parseInt(record[posSex]);
        Gender gender = Gender.valueOf(genderCode);

        int occupationCode = Integer.parseInt(record[posOccupation]);
        Occupation occupation = Occupation.valueOf(occupationCode);
        int workplace = Integer.parseInt(record[posWorkplace]);

        boolean driversLicense = false;
        if (Integer.parseInt(record[posLicence]) == 1) {
            driversLicense = true;
        }
        int income = Integer.parseInt(record[posIncome]);
        hh.addIncome(income);
        MitoPerson pp = new MitoPerson(id, occupation, workplace, age, gender, driversLicense);
        hh.addPerson(pp);
        dataSet.addPerson(pp);
    }
}
