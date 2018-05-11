package de.tum.bgu.msm.io.input;

import com.google.common.collect.Range;
import de.tum.bgu.msm.data.DataSet;
import de.tum.bgu.msm.data.MitoHousehold;
import de.tum.bgu.msm.data.MitoPerson;
import de.tum.bgu.msm.data.MitoZone;
import de.tum.bgu.msm.data.travelTimes.SkimTravelTimes;
import de.tum.bgu.msm.data.travelTimes.TravelTimes;
import de.tum.bgu.msm.io.input.readers.*;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Input {

    private static final Logger logger = Logger.getLogger(Input.class);

    private final DataSet dataSet;
    private HashMap<String, Integer> economicStatusDefinition;


    public Input(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        dataSet.setTravelTimes(new SkimTravelTimes());
        new ZonesReader(dataSet).read();
        if (Resources.INSTANCE.getBoolean(Properties.REMOVE_TRIPS_AT_BORDER)) {
            new BorderDampersReader(dataSet).read();
        }
        new SkimsReader(dataSet).read();
        new HouseholdsReader(dataSet).read();
        new PersonsReader(dataSet).read();
        new JobReader(dataSet).read();
    }

    public void readAdditionalData() {
        new SchoolEnrollmentReader(dataSet).read();
        new TripAttractionRatesReader(dataSet).read();
//        new TravelSurveyReader(dataSet).read();
        new ModeChoiceInputReader(dataSet).read();
        economicStatusDefinition = new HashMap<>();
        new EconomicStatusReader(dataSet, economicStatusDefinition).read();
        assignEconomicStatusToAllHouseholds();
    }

    public void readFromFeed(InputFeed feed) {
        for(MitoZone zone: feed.zones.values()) {
            dataSet.addZone(zone);
        }
        dataSet.setTravelTimes(feed.travelTimes);
        setHouseholdsFromFeed(feed.households);
    }

    private void setHouseholdsFromFeed(Map<Integer, MitoHousehold> households) {
        for (MitoHousehold household : households.values()) {
            if (dataSet.getZones().containsKey(household.getHomeZone().getId())) {
                dataSet.getZones().get(household.getHomeZone().getId()).addHousehold();
            } else {
                throw new RuntimeException("Fed household " + household.getId() + " refers to non-existing home zone "
                        + household.getHomeZone());
            }
            dataSet.addHousehold(household);
            for(MitoPerson person: household.getPersons().values()) {
                dataSet.addPerson(person);
            }
        }
    }

    public final static class InputFeed {

        private final Map<Integer, MitoZone> zones;
        private final TravelTimes travelTimes;
        private final Map<Integer, MitoHousehold> households;

        public InputFeed(Map<Integer, MitoZone> zones, TravelTimes travelTimes, Map<Integer, MitoHousehold> households) {
            this.zones = zones;
            this.travelTimes = travelTimes;
            this.households = households;
        }
    }

    private void assignEconomicStatusToAllHouseholds() {
        logger.info("  Assigning economic status to all households");
        for (MitoHousehold hh: dataSet.getHouseholds().values()) {
            hh.setEconomicStatus(getEconomicStatus(hh));
        }
    }

    private int getEconomicStatus(MitoHousehold hh) {
        /*
        Defined as:
            1: Sehr niedrig
            2: Niedrig
            3: Mittel
            4: Hoch
            5: Sehr hoch
         */
        int countAdults = (int) hh.getPersons().values().stream().filter(person ->
                person.getAge() > 14).count();
        int countChildren = (int) hh.getPersons().values().stream().filter(person ->
                person.getAge() <= 14).count();
        // Mobilität in Deutschland 2008, Variablenaufbereitung Haushaltsdatensatz: In Anlehnung an die neue Berechnungsskala der OECD gingen bei der Berechnung Kinder bis zu 14 Jahren mit dem Faktor 0,3 ein. Von den Personen ab 15 Jahren im Haushalt wurde eine Person mit dem Faktor 1, alle weiteren Personen ab 15 Jahren mit dem Faktor 0,5 gewichtet.
        float weightedHhSize = MitoUtil.rounder(Math.min(3.5f, 1.0f + (countAdults - 1f) * 0.5f + countChildren * 0.3f), 1);
        String incomeCategory = getMidIncomeCategory(hh.getIncome());
        return economicStatusDefinition.get(weightedHhSize+"_"+incomeCategory);
    }


    private String getMidIncomeCategory(int income) {

        final String[] incomeBrackets = {"Inc0-500","Inc500-900","Inc900-1500","Inc1500-2000","Inc2000-2600",
                "Inc2600-3000","Inc3000-3600","Inc3600-4000","Inc4000-4600","Inc4600-5000","Inc5000-5600",
                "Inc5600-6000","Inc6000-6600","Inc6600-7000","Inc7000+"};

        for (String incomeBracket : incomeBrackets) {
            String shortIncomeBrackets = incomeBracket.substring(3);
            try{
                String[] incomeBounds = shortIncomeBrackets.split("-");
                if (income >= Integer.parseInt(incomeBounds[0]) && income < Integer.parseInt(incomeBounds[1])) {
                    return incomeBracket;
                }
            } catch (Exception e) {
                if (income >= 7000) return incomeBrackets[incomeBrackets.length-1];
            }
        }
        logger.error("Unrecognized income: " + income);
        return null;
    }

}
