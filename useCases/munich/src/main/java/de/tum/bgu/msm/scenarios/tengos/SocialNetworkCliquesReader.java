package de.tum.bgu.msm.scenarios.tengos;

import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SocialNetworkCliquesReader extends AbstractCsvReader {

    private static final Logger logger = Logger.getLogger(SocialNetworkCliquesReader.class);

    private int posCliqueId = -1;
    private int posEgo = -1;
    private int countError;
    private Map<Integer, List<Integer>> cliqueList = new HashMap<>();

    public Map<Integer, List<Integer>> getCliqueList() {
        return cliqueList;
    }

    public  SocialNetworkCliquesReader(DataSet dataSet) {
        super(dataSet);
    }

    public void read() {
        logger.info("  Reading clique data from csv file");
        Path filePath = Paths.get("C:\\models\\tengos_episim\\input\\social_net_edge_list_v_3.0\\cliqueListFriends5pct.csv");
        super.read(filePath, ",");
        logger.info(countError + "Egos do not exist in the trip person map.");
    }

    @Override
    public void processHeader(String[] header) {
        List<String> headerList = Arrays.asList(header);
        posCliqueId = headerList.indexOf("clique_id");
        posEgo = headerList.indexOf("ego");
    }

    @Override
    public void processRecord(String[] record) {
        final int clique = Integer.parseInt(record[posCliqueId]);
        final int ego = Integer.parseInt(record[posEgo]);

        if(dataSet.getPersons().get(ego)==null){
            countError++;
            //logger.error("Ego: " + ego + " is not in the person map!");
        }else if(cliqueList.containsKey(clique)) {
            List<Integer> egoList = cliqueList.get(clique);
            egoList.add(ego);
        }else{
            List<Integer> egoList = new ArrayList<>();
            egoList.add(ego);
            cliqueList.put(clique, egoList);
        }
    }
}
