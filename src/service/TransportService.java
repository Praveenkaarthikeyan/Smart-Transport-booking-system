package service;

import model.Bus;
import model.Metro;
import model.Train;
import model.Transport;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class TransportService {

    private final List<Transport> transportList;

    public TransportService() {
        transportList = new CopyOnWriteArrayList<>();
        loadSampleData();
    }

    private void loadSampleData() {
        transportList.add(new Train("T120", "Cheran Express", "Chennai", "Coimbatore", "21:35", "05:45", 400.0, 60, "Sleeper"));
        transportList.add(new Bus("B260", "TNSTC AC Ultra", "Chennai", "Coimbatore", "22:00", "06:00", 550.0, 40, "AC"));

        transportList.add(new Train("T121", "Vaigai Express", "Chennai", "Madurai", "06:35", "13:15", 350.0, 65, "AC Chair Car"));
        transportList.add(new Bus("B261", "TNSTC Ultra Deluxe", "Chennai", "Madurai", "21:30", "05:30", 500.0, 42, "AC Sleeper"));

        transportList.add(new Train("T122", "Rockfort Express", "Chennai", "Tiruchirapalli", "22:00", "05:30", 320.0, 55, "Sleeper"));
        transportList.add(new Bus("B264", "TNSTC Express", "Chennai", "Tiruchirapalli", "21:00", "04:30", 380.0, 40, "AC"));

        transportList.add(new Bus("B262", "SETC AC Sleeper", "Chennai", "Salem", "23:00", "05:00", 420.0, 36, "AC Sleeper"));
        transportList.add(new Train("T124", "Kovai Express", "Chennai", "Salem", "07:15", "12:45", 280.0, 58, "AC Chair Car"));

        transportList.add(new Bus("B263", "TNSTC Express", "Chennai", "Vellore", "07:00", "10:00", 180.0, 45, "Non-AC"));

        transportList.add(new Train("T123", "Thanjavur Express", "Chennai", "Thanjavur", "21:00", "05:15", 340.0, 58, "Sleeper"));

        transportList.add(new Train("T125", "Nellai Express", "Chennai", "Tirunelveli", "20:30", "06:30", 450.0, 60, "Sleeper"));
        transportList.add(new Bus("B265", "SETC Ultra Deluxe", "Chennai", "Tirunelveli", "20:00", "06:00", 600.0, 38, "AC Sleeper"));

        transportList.add(new Train("T126", "Erode Express", "Chennai", "Erode", "22:30", "05:00", 300.0, 55, "Sleeper"));
        transportList.add(new Bus("B266", "TNSTC AC", "Chennai", "Erode", "21:30", "04:30", 380.0, 40, "AC"));

        transportList.add(new Train("T127", "Kanchipuram Local", "Chennai", "Kanchipuram", "08:00", "09:30", 40.0, 90, "General"));
        transportList.add(new Bus("B267", "TNSTC Express", "Chennai", "Kanchipuram", "09:00", "10:45", 60.0, 45, "Non-AC"));

        transportList.add(new Bus("B268", "TNSTC Express", "Chennai", "Cuddalore", "10:00", "13:30", 150.0, 42, "Non-AC"));

        transportList.add(new Train("T128", "Pearl City Express", "Chennai", "Thoothukudi", "19:30", "06:30", 420.0, 55, "Sleeper"));

        transportList.add(new Train("T129", "Kanyakumari Express", "Chennai", "Nagercoil", "17:30", "06:15", 460.0, 58, "Sleeper"));

        transportList.add(new Bus("B269", "TNSTC Express", "Chennai", "Hosur", "08:30", "12:30", 220.0, 40, "AC"));

        transportList.add(new Bus("B270", "TNSTC Express", "Coimbatore", "Erode", "07:00", "08:30", 80.0, 45, "Non-AC"));
        transportList.add(new Train("T130", "Coimbatore-Erode Passenger", "Coimbatore", "Erode", "06:30", "08:00", 60.0, 60, "General"));

        transportList.add(new Bus("B271", "TNSTC Express", "Coimbatore", "Tiruppur", "08:00", "09:00", 50.0, 45, "Non-AC"));

        transportList.add(new Bus("B272", "TNSTC Express", "Erode", "Tiruppur", "09:00", "09:45", 40.0, 45, "Non-AC"));

        transportList.add(new Bus("B273", "TNSTC Express", "Madurai", "Tirunelveli", "10:00", "12:30", 130.0, 42, "Non-AC"));

        transportList.add(new Bus("B274", "TNSTC Express", "Madurai", "Dindigul", "09:00", "10:15", 70.0, 45, "Non-AC"));
        transportList.add(new Train("T131", "Madurai-Dindigul Passenger", "Madurai", "Dindigul", "08:30", "09:45", 50.0, 60, "General"));

        transportList.add(new Bus("B275", "TNSTC Express", "Tiruchirapalli", "Karur", "11:00", "12:15", 65.0, 42, "Non-AC"));
    }

    public List<Transport> getTransportList() {
        return transportList;
    }

    public List<String> getAllCities() {
        return transportList.stream()
                .flatMap(t -> Stream.of(t.getSource(), t.getDestination()))
                .distinct()
                .sorted()
                .toList();
    }

    public void displayAll() {
        if (transportList.isEmpty()) {
            System.out.println("No transport services available.");
            return;
        }
        transportList.forEach(System.out::println);
    }

    public List<Transport> searchByRoute(String source, String destination) {
        return transportList.stream()
                .filter(t -> t.getSource().equalsIgnoreCase(source)
                        && t.getDestination().equalsIgnoreCase(destination))
                .toList();
    }

    public Optional<Transport> findById(String transportId) {
        return transportList.stream()
                .filter(t -> t.getTransportId().equalsIgnoreCase(transportId))
                .findFirst();
    }

    public Optional<Transport> findByTypeAndName(String type, String name) {
        return transportList.stream()
                .filter(t -> t.getType().equalsIgnoreCase(type) && t.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void addTransport(Transport transport) {
        transportList.add(transport);
        System.out.println(transport.getType() + " added successfully: " + transport.getName());
    }

    public boolean removeTransport(String transportId) {
        boolean removed = transportList.removeIf(t -> t.getTransportId().equalsIgnoreCase(transportId));
        System.out.println(removed ? "Transport removed successfully." : "Transport ID not found.");
        return removed;
    }

    public boolean updateTransport(String transportId, double newFare, int newTotalSeats) {
        Optional<Transport> opt = findById(transportId);
        if (opt.isEmpty()) {
            System.out.println("Transport ID not found.");
            return false;
        }
        Transport t = opt.get();
        t.setFare(newFare);
        t.setTotalSeats(newTotalSeats);
        t.setAvailableSeats(newTotalSeats);
        System.out.println("Transport updated successfully.");
        return true;
    }
}