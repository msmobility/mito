package de.tum.bgu.msm.modules.trafficAssignment;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import de.tum.bgu.msm.data.Purpose;
import de.tum.bgu.msm.io.input.readers.GenericCsvReader;
import de.tum.bgu.msm.io.input.readers.GenericCsvReader.GenericCsvTable;

public class TripCSVToMATSimPlan {

	// This class will read trip lists in CSV from MITO and creates a MATSim XML
	// plan file

	private static String[] columns = { "id", "originX", "originY", "destinationX", "destinationY", "purpose", "person",
			"mode", "distance", "departure_time", "departure_time_return" };

	private static String filename;
	private static GenericCsvTable table;
	private static PopulationFactory factory;
	private static Network network;
	private static Map<String, Integer> indexes = new HashMap<String, Integer>();

	public static void main(String[] args) {
		filename = args[0];
		String networkFile = args[1];

		// TODO add logging
		GenericCsvReader reader = new GenericCsvReader(filename);
		reader.read();
		table = reader.getTable();

		for (String column : columns) {
			indexes.put(column, table.getColumnIndexOf(column));
		}

		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkFile);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		network = scenario.getNetwork();

		Population population = PopulationUtils.createPopulation(config);
		factory = population.getFactory();

		for (int i = 0; i < table.getRowCount(); i++) {
			population.addPerson(createPersonFromTrips(i));
		}

		PopulationWriter popwriter = new PopulationWriter(population);
		popwriter.write(filename + ".xml.gz");

		System.out.println("done.");
	}

	private static Person createPersonFromTrips(int i) {
		Id<Person> matsimId = Id.createPersonId(table.getString(i, indexes.get("id")) + "_" + i);

		Person p = factory.createPerson(Id.createPersonId(matsimId));
		Plan plan = factory.createPlan();

		Purpose purpose = Purpose.valueOf(table.getString(i, indexes.get("purpose")));
		boolean roundTrip = !(purpose.equals(Purpose.NHBW) || purpose.equals(Purpose.NHBO));
		
		String firstActivityType = MatsimPopulationGenerator.getOriginActivity(purpose);
		Coord firstCoord = new Coord(table.getDouble(i, indexes.get("originX")),
				table.getDouble(i, indexes.get("originY")));

		Activity firstAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
		firstAct.setLinkId(NetworkUtils.getNearestLink(network, firstCoord).getId());

		Double departureTime = table.getDouble(i, indexes.get("departure_time"));
		firstAct.setEndTime(departureTime);
		plan.addActivity(firstAct);

		String mode = decodeMode(table.getString(i, indexes.get("mode")));
		Leg firstLeg = factory.createLeg(mode);
		firstLeg.setDepartureTime(departureTime);
		plan.addLeg(firstLeg);
		
		String secondActivityType = MatsimPopulationGenerator.getDestinationActivity(purpose);
		Coord secondCoord = new Coord(table.getDouble(i, indexes.get("destinationX")),
				table.getDouble(i, indexes.get("destinationY")));
		
		Activity secondAct = factory.createActivityFromCoord(secondActivityType, secondCoord);
		secondAct.setLinkId(NetworkUtils.getNearestLink(network, secondCoord).getId());
		secondAct.setStartTime(departureTime + 1); // TODO include MITO's travel time estimations
		
		Double departureTimeReturn = null;
		if (roundTrip) {
			departureTimeReturn = table.getDouble(i, indexes.get("departure_time_return"));
			secondAct.setEndTime(departureTimeReturn);
		}
		plan.addActivity(secondAct);
		
		if (roundTrip) {
			Leg secondLeg = factory.createLeg(mode);
			secondLeg.setDepartureTime(departureTimeReturn);
			plan.addLeg(secondLeg);
			
			Activity thirdAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
			thirdAct.setLinkId(NetworkUtils.getNearestLink(network, firstCoord).getId());
			thirdAct.setStartTime(departureTimeReturn + 1); // TODO include MITO's travel time estimations
			plan.addActivity(thirdAct);
		}

		p.addPlan(plan);
		p.setSelectedPlan(plan);
		return p;
	}

	private static String decodeMode(String encodedMode) {
		switch (encodedMode) {
		case "autoDriver":
			return "car";
		case "train":
		case "bus":
		case "tramOrMetro":
			return "pt";
		default:
			return encodedMode;
		}
	}
}
