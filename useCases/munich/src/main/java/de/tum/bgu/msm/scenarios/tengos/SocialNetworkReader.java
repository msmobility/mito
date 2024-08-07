package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialNetworkReader extends AbstractCsvReader{
    private static final Logger logger = Logger.getLogger(SocialNetworkReader.class);

    private int posEgo = -1;
    private int posAlter = -1;
    private int countError;
    //private SocialNetworkType socialNetworkType;

    public SocialNetworkReader(DataSet dataSet) {
        super(dataSet);
    }

    public void read() {
//		logger.info("  Reading ego-alter household data from csv file");
//		Path filePath = Paths.get("C:\\models\\tengos_episim\\input\\egoAlterHousehold5pct.csv");
//		socialNetworkType = SocialNetworkType.HOUSEHOLD;
//		super.read(filePath, ",");
//		logger.info(countError + " Egos are not existed in the trip person map.");
//		logger.info("  Reading ego-alter job data from csv file");
//		filePath = Paths.get("C:\\models\\tengos_episim\\input\\egoAlterJob5pct.csv");
//		socialNetworkType = SocialNetworkType.COWORKER;
//		super.read(filePath, ",");
//		logger.info(countError + " Egos are not existed in the trip person map.");
//		logger.info("  Reading ego-alter nursing home data from csv file");
//		filePath = Paths.get("C:\\models\\tengos_episim\\input\\egoAlterNursingHome5pct.csv");
//		socialNetworkType = SocialNetworkType.NURSINGHOME;
//		super.read(filePath, ",");
//		logger.info(countError + " Egos are not existed in the trip person map.");
//		logger.info("  Reading ego-alter school data from csv file");
//		filePath = Paths.get("C:\\models\\tengos_episim\\input\\egoAlterSchool5pct.csv");
//		socialNetworkType = SocialNetworkType.SCHOOLMATE;
//		super.read(filePath, ",");
//		logger.info(countError + " Egos are not existed in the trip person map.");
//		logger.info("  Reading ego-alter dwelling data from csv filemum");
//		filePath = Paths.get("C:\\models\\tengos_episim\\input\\egoAlterDwelling5pct.csv");
//		socialNetworkType = SocialNetworkType.NEIGHBOR;
//		super.read(filePath, ",");
//		logger.info(countError + " Egos are not existed in the trip person map.");
        logger.info("  Reading ego-alter friend data from csv file");
        Path filePath = Paths.get("C:\\models\\tengos_episim\\input\\social_net_edge_list_v_3.0\\egoAlterFriends5pct_reflected.csv");
        //socialNetworkType = SocialNetworkType.FRIEND;
        super.read(filePath, ",");
        logger.info(countError + " Egos are not existed in the trip person map.");
    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posEgo = headerList.indexOf("ego");
        posAlter = headerList.indexOf("alter");
    }

    @Override
    public void processRecord(String[] record) {
        final int ego = Integer.parseInt(record[posEgo]);
        final int alter = Integer.parseInt(record[posAlter]);

        MitoPerson egoPerson = dataSet.getPersons().get(ego);

        if(egoPerson.getId()==877961){
            System.out.println("problematic person without alter list");
        }

        if(egoPerson ==null){
            countError++;
            logger.error("Ego: " + ego + " is not in the person map!");
        }else {
            if(egoPerson instanceof MitoPersonTengos){
                MitoPersonTengos egoPersonTengos = (MitoPersonTengos) egoPerson;
                if(egoPersonTengos.getAlterLists()==null){
                    egoPersonTengos.setAlterLists(new ArrayList<>());
                }
                egoPersonTengos.getAlterLists().add(alter);
            }else{
                countError++;
                logger.error("Ego: " + ego + " is not an instance of MitoPersonTengos!");
            }

        }

    }
}
