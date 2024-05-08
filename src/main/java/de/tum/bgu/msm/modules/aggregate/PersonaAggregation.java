package de.tum.bgu.msm.modules.aggregate;

import de.tum.bgu.msm.data.*;
import de.tum.bgu.msm.modules.Module;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Map<Boolean, Map<String, Double>> personaCounters = new HashedMap();

    @Override
    public void run() {
        logger.info("  Started aggregation to personas.");
        aggregateHouseholdAttributes();
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
                        personaCounters.get(ev).put("hh.income_".concat(String.valueOf(size)),(double) count/totalHouseholds.get(ev));
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

        //count persons by age group *trip gen and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::personAgeGroupTripGen, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put(size,(double) count/totalPersons.get(ev));
                    });
        });

        //count persons by age group *mode choice and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::personAgeGroupModeChoice, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put(size,(double) count/totalPersons.get(ev));
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
                        personaCounters.get(ev).put("p.".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

        //count is mobile for HBW and EV ownership
        personsByEV.forEach((ev, persons) -> {
            persons.parallelStream()
                    //group number of trips by mode
                    .collect(Collectors.groupingBy(MitoPerson::getMitoOccupationStatus, Collectors.counting()))
                    //calculate and add share to data set table
                    .forEach((size, count) -> {
                        personaCounters.get(ev).put("p.".concat(String.valueOf(size)),(double) count/totalPersons.get(ev));
                    });
        });

    }





}
