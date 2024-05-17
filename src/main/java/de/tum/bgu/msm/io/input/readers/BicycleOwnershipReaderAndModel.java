package de.tum.bgu.msm.io.input.readers;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.io.input.AbstractCsvReader;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BicycleOwnershipReaderAndModel extends AbstractCsvReader {

    private final Logger logger = Logger.getLogger(BicycleOwnershipReaderAndModel.class);
    private final Map<String, Double> coefficients = new HashMap<>();
    private int variableIndex;
    private int valueIndex;

    private Map<AgeBin, Integer> bikesPerAgeBin = new HashMap<>();
    private Map<AgeBin, Integer> personsPerAgeBin = new HashMap<>();

    public BicycleOwnershipReaderAndModel(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        variableIndex = MitoUtil.findPositionInArray("coefficients", header);
        valueIndex = MitoUtil.findPositionInArray("yes", header);
    }

    @Override
    protected void processRecord(String[] record) {
        coefficients.put(record[variableIndex], Double.parseDouble(record[valueIndex]));
    }

    @Override
    public void read() {
        super.read(Resources.instance.getBicycleOwnershipInputFile(), ",");
        assignBicycleOwnership();
    }

    private void assignBicycleOwnership() {
        for (MitoHousehold hh : dataSet.getHouseholds().values()){
            int economicStatus = hh.getEconomicStatus();


            AreaTypes.SGType areaTypeSG = hh.getHomeZone().getAreaTypeSG();
            int cars = hh.getAutos();
            double distanceToTransit = hh.getHomeZone().getDistanceToNearestRailStop() * 1000; //the model says distance to transit!
            final Map<Integer, MitoPerson> persons = hh.getPersons();
            int hhSize = persons.size();
            for (MitoPerson pp : persons.values()){
                double utility = coefficients.get("asc");
                switch(areaTypeSG){
                    case CORE_CITY:
                        utility += coefficients.get("bbsrCoreCity");
                        break;
                    case MEDIUM_SIZED_CITY:
                        utility += coefficients.get("bbsrMiddleCity");
                        break;
                    case TOWN:
                        utility += coefficients.get("bbsrTown");
                        break;
                    case RURAL:
                        utility += coefficients.get("bbsrRural");
                        break;
                }
                if (distanceToTransit < 250){
                    utility += coefficients.get("distTransit_0_250m");
                } else if (distanceToTransit < 500){
                    utility += coefficients.get("distTransit_250_500m");
                } else if (distanceToTransit < 1000) {
                    utility += coefficients.get("distTransit_500_1000m");
                } else if (distanceToTransit < 2500) {
                    utility += coefficients.get("distTransit_1000_2500m");
                } else if (distanceToTransit < 5000) {
                    utility += coefficients.get("distTransit_2500_5000m");
                } else {
                    utility += coefficients.get("distTransit_5000mUp");
                }
                switch (cars){
                    case 0:
                        utility += coefficients.get("hhCar0");
                        break;
                    case 1:
                        utility += coefficients.get("hhCar1");
                        break;
                    case 2:
                        utility += coefficients.get("hhCar2");
                        break;
                    case 3:
                        utility += coefficients.get("hhCar3");
                        break;
                    default:
                        utility += coefficients.get("hhCar4up");
                        break;
                }

                switch (hhSize){
                    case 1:
                        utility += coefficients.get("hhSize1");
                        break;
                    case 2:
                        utility += coefficients.get("hhSize2");
                        break;
                    case 3:
                        utility += coefficients.get("hhSize3");
                        break;
                    default:
                        utility += coefficients.get("hhSize4up");
                        break;
                }
                switch (economicStatus){
                    case 0:
                        utility += coefficients.get("incVeryLow");
                        break;
                    case 1:
                        utility += coefficients.get("incLow");
                        break;
                    case 2:
                        utility += coefficients.get("incMiddle");
                        break;
                    case 3:
                        utility += coefficients.get("incHigh");
                        break;
                    case 4:
                        utility += coefficients.get("incVeryHigh");
                        break;
                }

                int age = pp.getAge();
                AgeBin ageBin = AgeBin.getAgeBinFromAge(age);
                personsPerAgeBin.putIfAbsent(ageBin, 1);
                personsPerAgeBin.put(ageBin, personsPerAgeBin.get(ageBin)+ 1);
                switch (ageBin){
                    case from0to17:
                        utility += coefficients.get("age0_17");
                        break;
                    case from18to29:
                        utility += coefficients.get("age18_29");
                        break;
                    case from30to39:
                        utility += coefficients.get("age30_39");
                        break;
                    case from40to49:
                        utility += coefficients.get("age40_49");
                        break;
                    case from50to59:
                        utility += coefficients.get("age50_59");
                        break;
                    case from60to69:
                        utility += coefficients.get("age60_69");
                        break;
                    case from70to79:
                        utility += coefficients.get("age70_79");
                        break;
                    case from80to100:
                        utility += coefficients.get("age80_105");
                        break;
                }
                MitoOccupationStatus occupation = pp.getMitoOccupationStatus();
                switch (occupation){
                    case WORKER:
                        utility += coefficients.get("occupationEmployed");
                        break;
                    case STUDENT:
                        utility += coefficients.get("occupationStudent");
                        break;
                    default:
                        utility += coefficients.get("occupationOther");
                        break;
                }

                final double exp = Math.exp(utility);
                double probability = exp / (1 + exp);

                boolean hasBicycle = MitoUtil.getRandomObject().nextDouble() < probability? true : false;
                if (hasBicycle){
                    bikesPerAgeBin.putIfAbsent(ageBin, 1);
                    bikesPerAgeBin.put(ageBin, bikesPerAgeBin.get(ageBin)+ 1);
                }

                pp.setHasBicycle(hasBicycle);
            }
        }
        //logger.info("Age bin" + "\t" + "Share of bicycles");
        int totalPersons = 0;
        int totalBikes = 0;
        for (AgeBin ab : AgeBin.values()){
            //logger.info(ab.toString() + "\t" + Double.valueOf(bikesPerAgeBin.get(ab)) / personsPerAgeBin.get(ab));
            totalPersons += personsPerAgeBin.get(ab);
            totalBikes += bikesPerAgeBin.get(ab);
        }
        logger.info("Bicycle ownership model completed with " + totalPersons + " persons and " + totalBikes +
                " bicycles (" + Double.valueOf(totalBikes)/totalPersons*100 + "%)");

    }

    public enum AgeBin {
        from0to17, from18to29, from30to39, from40to49, from50to59, from60to69, from70to79, from80to100;

        static AgeBin getAgeBinFromAge(int age){
            if (age < 18){
                return from0to17;
            } else if (age < 30){
                return from18to29;
            } else if (age < 40){
                return from30to39;
            } else if (age < 50){
                return from40to49;
            }else if (age < 60){
                return from50to59;
            } else if (age < 70){
                return from60to69;
            } else if (age < 80){
                return from70to79;
            } else {
                return from80to100;
            }
        }

    }
}
