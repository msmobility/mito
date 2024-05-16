package de.tum.bgu.msm.modules.aggregate;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Runs aggregation into personas for the Transport in Microsimulation Orchestrator (MITO)
 * @author Ana Moreno
 * Created on May 8, 2024 in Valencia, Spain
 *
 */

public class PersonaAggregation extends Module {

    private static final Logger logger = Logger.getLogger(PersonaAggregation.class);


    public PersonaAggregation(DataSet dataSet, List<Purpose> purposes) {
        super(dataSet, purposes);
    }

    Map<Boolean, Map<String, Double>> personaCounters = new LinkedHashMap<>();

    @Override
    public void run() {
        logger.info("  Started aggregation to personas.");
        //aggregateHouseholdAttributes();
        aggregatePersonAttributes();
        addToDataSet();
        logger.info("  Completed aggregation to personas.");
    }

    private void addToDataSet() {
        MitoAggregatePersona personaWithEV = new MitoAggregatePersona(1, "EV");
        personaWithEV.setAggregateAttributes(personaCounters.get(Boolean.TRUE));
        dataSet.addPersona(personaWithEV);

        MitoAggregatePersona personaWithoutEV = new MitoAggregatePersona(2, "NoEV");
        personaWithoutEV.setAggregateAttributes(personaCounters.get(Boolean.FALSE));
        dataSet.addPersona(personaWithoutEV);

        Map<MitoAggregatePersona, Map<AreaTypes.SGType, Double>> map = new LinkedHashMap<>();
        for (MitoAggregatePersona persona : dataSet.getAggregatePersonas().values()){
            if (persona.getId() ==1){
                Map<AreaTypes.SGType, Double> mapEV = new LinkedHashMap<>();
                mapEV.put(AreaTypes.SGType.CORE_CITY, personaWithEV.getAggregateAttributes().get("hh.BBSR_count_10"));
                mapEV.put(AreaTypes.SGType.MEDIUM_SIZED_CITY, personaWithEV.getAggregateAttributes().get("hh.BBSR_count_20"));
                mapEV.put(AreaTypes.SGType.TOWN, personaWithEV.getAggregateAttributes().get("hh.BBSR_count_30"));
                mapEV.put(AreaTypes.SGType.RURAL, personaWithEV.getAggregateAttributes().get("hh.BBSR_count_40"));
                map.put(persona, mapEV);
            } else {
                Map<AreaTypes.SGType, Double> mapnoEV = new LinkedHashMap<>();
                mapnoEV.put(AreaTypes.SGType.CORE_CITY, personaWithoutEV.getAggregateAttributes().get("hh.BBSR_count_10"));
                mapnoEV.put(AreaTypes.SGType.MEDIUM_SIZED_CITY, personaWithoutEV.getAggregateAttributes().get("hh.BBSR_count_20"));
                mapnoEV.put(AreaTypes.SGType.TOWN, personaWithoutEV.getAggregateAttributes().get("hh.BBSR_count_30"));
                mapnoEV.put(AreaTypes.SGType.RURAL, personaWithoutEV.getAggregateAttributes().get("hh.BBSR_count_40"));
                map.put(persona, mapnoEV);
            }
        }
        dataSet.setPersonsByAreaType(map);

        Path filePersona = Resources.instance.getPersonaFilePath();
        PrintWriter pwh = MitoUtil.openFileForSequentialWriting(filePersona.toAbsolutePath().toString(), false);

        pwh.print("id");
        ArrayList<String> attributes = new ArrayList<String>();
        ArrayList<String> attributesString = new ArrayList<String>();
        for (String attribute : dataSet.getAggregatePersonas().get(1).getAggregateAttributes().keySet()){
            pwh.print(",");
            pwh.print(attribute);
            attributes.add(attribute);
        }
        for (String attribute: dataSet.getAggregatePersonas().get(1).getAggregateStringAttributes().keySet()){
            pwh.println(",");
            pwh.print(attribute);
            attributesString.add(attribute);
        }
        pwh.println();
        for (MitoAggregatePersona pp : dataSet.getAggregatePersonas().values()) {
            pwh.print(pp.getId());
            for (String attribute : attributes){
                pwh.print(",");
                pwh.print(pp.getAggregateAttributes().get(attribute));
            }
            for (String attribute : attributesString){
                pwh.print(",");
                pwh.print(pp.getAggregateStringAttributes().get(attribute));
            }
            pwh.print(",");
            pwh.println();
        }
        pwh.close();
    }


    private void aggregateHouseholdAttributes() {

        //Separate households by EV ownership
        Map<Boolean, List<MitoHousehold>> householdsByEV = dataSet.getHouseholds().values().stream()
                .collect(Collectors.groupingBy(MitoHousehold::isHasEV));

        //count total households by EV ownership
        final Map<Boolean, Long> totalHouseholds = new HashMap<>();
        householdsByEV.forEach((ev, households) -> {
            long count = households.parallelStream().count();
            totalHouseholds.put(ev,count);
        });

        //initialize personCounters
        Map<String, Double> attributeBlankTrue = new HashMap<>();
        personaCounters.put(Boolean.TRUE,attributeBlankTrue);
        Map<String, Double> attributeBlankFalse = new HashMap<>();
        personaCounters.put(Boolean.FALSE,attributeBlankFalse);

        //count households by size (1 to 5+) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getSize5, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.size_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });

        //count households by children (0 to 3+) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getChildren, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.children_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });


        //count households by cars (0 to 3+) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getAutos3, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.cars_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });

        //count households by bikes (0 or 1+) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getBikes1, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.bikes_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });

        //count households by economic status (1 to 5) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getEconomicStatus, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.econStatus_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });

        //count households by income (less than 1500, betweem 1500 and 5600, more than 5600) and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getIncomeClass, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.income_".concat(size),(double) count/totalHouseholds.get(ev));
                    });
        });

        //count households by BBSR and EV ownership
        householdsByEV.forEach((ev, households) -> {
            households.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoHousehold::getBBSR, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.BBSR_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
                    });
        });

        //average car per adults by EV ownership
        personaCounters.get(Boolean.TRUE).put("hh.carsPerAdult",
                householdsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoHousehold::getCarsPerAdult)));
        personaCounters.get(Boolean.FALSE).put("hh.carsPerAdult",
                householdsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoHousehold::getCarsPerAdult)));

    }


    private void aggregatePersonAttributes() {

        //Separate PERSONS by EV ownership
        Map<Boolean, List<MitoPerson>> personsByEV = dataSet.getPersons().values().stream()
                .collect(Collectors.groupingBy(MitoPerson::hasEV));

        //count total persons by EV ownership
        final Map<Boolean, Long> totalPersons = new HashMap<>();
        personsByEV.forEach((ev, persons) -> {
            long count = persons.parallelStream().count();
            totalPersons.put(ev,count);
        });

        //initialize personCounters
        Map<String, Double> attributeBlankTrue = new HashMap<>();
        personaCounters.put(Boolean.TRUE,attributeBlankTrue);
        Map<String, Double> attributeBlankFalse = new HashMap<>();
        personaCounters.put(Boolean.FALSE,attributeBlankFalse);

        //personaCounters.get(Boolean.TRUE).put("p.total",
         //       personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.counting());

        personaCounters.get(Boolean.TRUE).put("totalPersons", totalPersons.get(Boolean.TRUE).doubleValue());
        personaCounters.get(Boolean.FALSE).put("totalPersons", totalPersons.get(Boolean.FALSE).doubleValue());

        //count households by size (1 to 5+) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getSize5, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.size_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count households by children (0 to 3+) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getChildren, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.children_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });


        //count households by cars (0 to 3+) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getAutos3, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.cars_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count households by bikes (0 or 1+) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getBikes1, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.bikes_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count households by economic status (1 to 5) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getEconomicStatus, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.econStatus_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count households by income (less than 1500, betweem 1500 and 5600, more than 5600) and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIncomeClass, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.income_".concat(size),(double) count/totalPersons.get(ev));
                    });
        });

        //count households by BBSR and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getBBSR, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.BBSR_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getBBSR, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("hh.BBSR_count_".concat(String.valueOf(size)),(double) count);
                    });
        });


        //average car per adults by EV ownership
        personaCounters.get(Boolean.TRUE).put("hh.carsPerAdult",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getCarsPerAdult)));
        personaCounters.get(Boolean.FALSE).put("hh.carsPerAdult",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getCarsPerAdult)));



        //count persons by age group *trip gen and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::personAgeGroupTripGen, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("tripGen" + size,(double) count/totalPersons.get(ev));
                    });
        });

        //count persons by age group *mode choice and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::personAgeGroupModeChoice, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("modeChoice_" + size,(double) count/totalPersons.get(ev));
                    });
        });

        //count persons by gender and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getMitoGender, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count persons by license and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::hasDriversLicenseString, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put(size,(double) count/totalPersons.get(ev));
                    });
        });

        //count persons by occupation and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getMitoOccupationStatus, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.occupation_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });


        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBW_car, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBW_car_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBW_PT, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBW_PT_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBW_cycle, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBW_cycle_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBW_walk, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBW_walk_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });


        personaCounters.get(Boolean.TRUE).put("p.TTB_HBW_car",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_car)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBW_PT",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_PT)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBW_cycle",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_cycle)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBW_walk",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_walk)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBW_car",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_car)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBW_PT",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_PT)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBW_cycle",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_cycle)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBW_walk",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBW_walk)));

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBE_car, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBE_car_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBE_PT, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBE_PT_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBE_cycle, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBE_cycle_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getIsMobile_HBE_walk, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.isMobile_HBE_walk_".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBE_car",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_car)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBE_PT",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_PT)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBE_cycle",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_cycle)));

        personaCounters.get(Boolean.TRUE).put("p.TTB_HBE_walk",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_walk)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBE_car",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_car)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBE_PT",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_PT)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBE_cycle",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_cycle)));

        personaCounters.get(Boolean.FALSE).put("p.TTB_HBE_walk",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getTTB_HBE_walk)));

        personaCounters.get(Boolean.TRUE).put("p.HBW_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBW_trips)));
        personaCounters.get(Boolean.TRUE).put("p.HBE_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBE_trips)));
        personaCounters.get(Boolean.TRUE).put("p.HBS_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBS_trips)));
        personaCounters.get(Boolean.TRUE).put("p.HBO_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBO_trips)));
        personaCounters.get(Boolean.TRUE).put("p.HBR_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBR_trips)));
        personaCounters.get(Boolean.TRUE).put("p.NHBW_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getNHBW_trips)));
        personaCounters.get(Boolean.TRUE).put("p.NHBO_trips",
                personsByEV.get(Boolean.TRUE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getNHBO_trips)));

        personaCounters.get(Boolean.FALSE).put("p.HBW_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBW_trips)));
        personaCounters.get(Boolean.FALSE).put("p.HBE_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBE_trips)));
        personaCounters.get(Boolean.FALSE).put("p.HBS_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBS_trips)));
        personaCounters.get(Boolean.FALSE).put("p.HBO_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBO_trips)));
        personaCounters.get(Boolean.FALSE).put("p.HBR_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getHBR_trips)));
        personaCounters.get(Boolean.FALSE).put("p.NHBW_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getNHBW_trips)));
        personaCounters.get(Boolean.FALSE).put("p.NHBO_trips",
                personsByEV.get(Boolean.FALSE).parallelStream().collect(Collectors.averagingDouble(MitoPerson::getNHBO_trips)));

    }





}
