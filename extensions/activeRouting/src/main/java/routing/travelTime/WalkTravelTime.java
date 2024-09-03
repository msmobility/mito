package routing.travelTime;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import routing.components.Gradient;

public class WalkTravelTime implements TravelTime {

    @Override
    // Applies Tobler's Hiking Function
    public double getLinkTravelTime(Link link, double v, Person person, Vehicle vehicle) {
        double gradient = Math.min(1,Math.max(-1,Gradient.getGradient(link)));
        return 3 * link.getLength() / (5 * Math.exp(-3.5*Math.abs(gradient + 0.05)));
    }
}

