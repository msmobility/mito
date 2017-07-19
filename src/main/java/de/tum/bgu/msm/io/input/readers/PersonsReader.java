package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.MitoUtil;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.CSVReader;
import de.tum.bgu.msm.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 17.07.2017.
 */
public class PersonsReader extends CSVReader {

    private static Logger logger = Logger.getLogger(PersonsReader.class);

    private int posId = -1;
    private int posHhId = -1;
    private int posAge = -1;
    private int posSex = -1;
    private int posOccupation = -1;
    private int posWorkplace = -1;
    private int posLicence = -1;
    private int posIncome = -1;

    private final Map<Integer, MitoPerson> persons = new HashMap<>();

    public PersonsReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading person micro data from ascii file");
        String fileName = Resources.INSTANCE.getString(Properties.PERSONS);
        super.readLineByLine(fileName, ",");
        dataSet.getPersons().putAll(persons);
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
        MitoHousehold hh = null;
        if(dataSet.getHouseholds().containsKey(hhid)) {
            hh = dataSet.getHouseholds().get(hhid);
        } else {
            logger.warn("Person " + id + " refers to non-existing household " + hhid + ". Ignoring this person.");
            return;
        }
        int age = Integer.parseInt(record[posAge]);
        if (age < 18) {
            hh.setChildren(hh.getChildren() + 1);
        } else if (age >= 18 && age <= 25) {
            hh.setYoungAdults(hh.getYoungAdults() + 1);
        } else if (age >= 65) {
            hh.setRetirees(hh.getRetirees() + 1);
        }
        if (Integer.parseInt(record[posSex]) == 2) {
            hh.setFemales(hh.getFemales() + 1);
        }
        int occupation = Integer.parseInt(record[posOccupation]);
        if (occupation == 1) {
            hh.setNumberOfWorkers(hh.getNumberOfWorkers() + 1);
        } else if (occupation == 3) { //students have occupation equal to 3
            hh.setStudents(hh.getStudents() + 1);
        }
        int workplace = Integer.parseInt(record[posWorkplace]);
        if (Integer.parseInt(record[posLicence]) == 1) {
            hh.setLicenseHolders(hh.getLicenseHolders() + 1);
        }
        int income = Integer.parseInt(record[posIncome]);
        hh.setIncome(hh.getIncome() + income);
        MitoPerson pp = new MitoPerson(id, hh, occupation, workplace);
        persons.put(pp.getId(), pp);
    }
}
