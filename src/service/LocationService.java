package service;

import model.Location;
import model.TripType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationService {

    private final List<Location> locations = new ArrayList<>();

    public LocationService() {
        loadLocations();
    }

    private void loadLocations() {
        locations.add(new Location("Tambaram", "Chennai", "Tamil Nadu", 12.9249, 80.1000));
        locations.add(new Location("Kundrathur", "Chennai", "Tamil Nadu", 12.9750, 80.0670));
        locations.add(new Location("Guindy", "Chennai", "Tamil Nadu", 13.0067, 80.2206));
        locations.add(new Location("Velachery", "Chennai", "Tamil Nadu", 12.9750, 80.2200));
        locations.add(new Location("T Nagar", "Chennai", "Tamil Nadu", 13.0418, 80.2341));
        locations.add(new Location("Egmore", "Chennai", "Tamil Nadu", 13.0732, 80.2609));
        locations.add(new Location("Anna Nagar", "Chennai", "Tamil Nadu", 13.0850, 80.2101));
        locations.add(new Location("Porur", "Chennai", "Tamil Nadu", 13.0382, 80.1565));
        locations.add(new Location("Chromepet", "Chennai", "Tamil Nadu", 12.9516, 80.1462));
        locations.add(new Location("Adyar", "Chennai", "Tamil Nadu", 13.0012, 80.2565));
        locations.add(new Location("Perambur", "Chennai", "Tamil Nadu", 13.1143, 80.2329));
        locations.add(new Location("Ambattur", "Chennai", "Tamil Nadu", 13.1143, 80.1548));
        locations.add(new Location("Avadi", "Chennai", "Tamil Nadu", 13.1147, 80.0970));
        locations.add(new Location("Sholinganallur", "Chennai", "Tamil Nadu", 12.9010, 80.2279));
        locations.add(new Location("Thoraipakkam", "Chennai", "Tamil Nadu", 12.9410, 80.2350));
        locations.add(new Location("Tiruvanmiyur", "Chennai", "Tamil Nadu", 12.9830, 80.2594));
        locations.add(new Location("Mylapore", "Chennai", "Tamil Nadu", 13.0339, 80.2619));
        locations.add(new Location("Pallavaram", "Chennai", "Tamil Nadu", 12.9675, 80.1491));
        locations.add(new Location("Poonamallee", "Chennai", "Tamil Nadu", 13.0500, 80.1000));
        locations.add(new Location("Redhills", "Chennai", "Tamil Nadu", 13.1930, 80.1830));

        locations.add(new Location("Chennai", "Chennai", "Tamil Nadu", 13.0827, 80.2707));
        locations.add(new Location("Coimbatore", "Coimbatore", "Tamil Nadu", 11.0168, 76.9558));
        locations.add(new Location("Madurai", "Madurai", "Tamil Nadu", 9.9252, 78.1198));
        locations.add(new Location("Tiruchirapalli", "Tiruchirapalli", "Tamil Nadu", 10.7905, 78.7047));
        locations.add(new Location("Salem", "Salem", "Tamil Nadu", 11.6643, 78.1460));
        locations.add(new Location("Vellore", "Vellore", "Tamil Nadu", 12.9165, 79.1325));
        locations.add(new Location("Thanjavur", "Thanjavur", "Tamil Nadu", 10.7870, 79.1378));
        locations.add(new Location("Tirunelveli", "Tirunelveli", "Tamil Nadu", 8.7139, 77.7567));
        locations.add(new Location("Erode", "Erode", "Tamil Nadu", 11.3410, 77.7172));
        locations.add(new Location("Tiruppur", "Tiruppur", "Tamil Nadu", 11.1085, 77.3411));
        locations.add(new Location("Dindigul", "Dindigul", "Tamil Nadu", 10.3624, 77.9695));
        locations.add(new Location("Karur", "Karur", "Tamil Nadu", 10.9601, 78.0766));
        locations.add(new Location("Kanchipuram", "Kanchipuram", "Tamil Nadu", 12.8342, 79.7036));
        locations.add(new Location("Cuddalore", "Cuddalore", "Tamil Nadu", 11.7480, 79.7714));
        locations.add(new Location("Thoothukudi", "Thoothukudi", "Tamil Nadu", 8.7642, 78.1348));
        locations.add(new Location("Nagercoil", "Nagercoil", "Tamil Nadu", 8.1833, 77.4119));
        locations.add(new Location("Hosur", "Hosur", "Tamil Nadu", 12.7409, 77.8253));
    }

    public List<Location> getAllLocations() {
        return locations;
    }

    public Optional<Location> findByName(String name) {
        return locations.stream()
                .filter(loc -> loc.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public TripType classify(Location source, Location destination) {
        if (source.parentCity().equalsIgnoreCase(destination.parentCity())) {
            return TripType.INTRACITY;
        }
        if (source.state().equalsIgnoreCase(destination.state())) {
            return TripType.INTERCITY_SAME_STATE;
        }
        return TripType.INTERSTATE;
    }
}