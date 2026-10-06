import file.FileManager;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.Bus;
import model.Metro;
import model.Passenger;
import model.TicketInfo;
import model.Train;
import model.Transport;
import service.BookingService;
import service.BookingServiceImpl;
import service.RecommendationService;
import service.TransportService;
import user.AdminUser;
import user.PassengerUser;
import util.Response;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final TransportService transportService = new TransportService();
    private static final FileManager fileManager = new FileManager();
    private static final BookingService bookingService = new BookingServiceImpl(transportService, fileManager);
    private static final RecommendationService recommendationService = new RecommendationService();

    public static void main(String[] args) {
        PassengerUser passengerUser = new PassengerUser("U1", "Guest Passenger");
        passengerUser.login();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> transportService.displayAll();
                case 2 -> searchTransport();
                case 3 -> bookTicketFlow();
                case 4 -> viewTicket();
                case 5 -> cancelTicket();
                case 6 -> viewBookingHistory();
                case 7 -> adminMenu();
                case 8 -> simulateConcurrentBooking();
                case 9 -> {
                    System.out.println("Thank you for using Smart Public Transport Booking System.");
                    passengerUser.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===================================================");
        System.out.println("Smart Public Transport Booking System");
        System.out.println("===================================================");
        System.out.println("1 View All Transports");
        System.out.println("2 Search by Route");
        System.out.println("3 Book Ticket");
        System.out.println("4 View Ticket");
        System.out.println("5 Cancel Ticket");
        System.out.println("6 View Booking History");
        System.out.println("7 Admin Menu");
        System.out.println("8 Simulate Concurrent Booking (Virtual Threads Demo)");
        System.out.println("9 Exit");
    }

    private static void searchTransport() {
        System.out.print("Enter source: ");
        String source = scanner.nextLine();
        System.out.print("Enter destination: ");
        String destination = scanner.nextLine();

        List<Transport> results = transportService.searchByRoute(source, destination);
        if (results.isEmpty()) {
            System.out.println("No transport services found for this route.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void bookTicketFlow() {
        System.out.print("Enter source: ");
        String source = scanner.nextLine();
        System.out.print("Enter destination: ");
        String destination = scanner.nextLine();

        List<Transport> options = transportService.searchByRoute(source, destination);
        if (options.isEmpty()) {
            System.out.println("No transport services found for this route.");
            return;
        }

        System.out.println("Available options:");
        options.forEach(System.out::println);

        System.out.print("Enter Transport ID to book: ");
        String transportId = scanner.nextLine();

        Passenger passenger = readPassengerDetails();

        Response<TicketInfo> response = bookingService.bookTicket(passenger, transportId);
        if (response.isSuccess()) {
            System.out.println("Booking successful! Here is your ticket:");
            response.getData().print();
            return;
        }

        System.out.println("Could not book that option: " + response.getMessage());
        System.out.println("Searching for alternatives...");

        List<Transport> alternatives = options.stream()
                .filter(t -> !t.getTransportId().equalsIgnoreCase(transportId))
                .toList();

        List<RecommendationService.ScoredOption> ranked = recommendationService.rank(alternatives);
        if (ranked.isEmpty()) {
            System.out.println("No alternatives available for this route right now.");
            return;
        }

        System.out.println("Top recommended alternatives:");
        int limit = Math.min(3, ranked.size());
        for (int i = 0; i < limit; i++) {
            RecommendationService.ScoredOption option = ranked.get(i);
            System.out.printf("%d) %s | Score: %.2f | %s%n",
                    i + 1, option.transport(), option.score(), option.reason());
        }

        System.out.print("Enter the Transport ID of the alternative you want to book (or 0 to skip): ");
        String altId = scanner.nextLine();
        if (altId.equals("0")) {
            System.out.println("Booking skipped.");
            return;
        }

        Response<TicketInfo> altResponse = bookingService.bookTicket(passenger, altId);
        if (altResponse.isSuccess()) {
            System.out.println("Booking successful! Here is your ticket:");
            altResponse.getData().print();
        } else {
            System.out.println("Booking failed: " + altResponse.getMessage());
        }
    }

    private static Passenger readPassengerDetails() {
        System.out.print("Enter Passenger Name: ");
        String name = scanner.nextLine();
        int age = readInt("Enter Passenger Age: ");
        System.out.print("Enter Passenger Gender: ");
        String gender = scanner.nextLine();
        return new Passenger("P-" + System.currentTimeMillis(), name, age, gender);
    }

    private static void viewTicket() {
        System.out.print("Enter PNR: ");
        String pnr = scanner.nextLine();
        Response<TicketInfo> response = bookingService.viewTicket(pnr);
        if (response.isSuccess()) {
            response.getData().print();
        } else {
            System.out.println("Error: " + response.getMessage());
        }
    }

    private static void cancelTicket() {
        System.out.print("Enter PNR to cancel: ");
        String pnr = scanner.nextLine();
        Response<String> response = bookingService.cancelTicket(pnr);
        System.out.println(response.isSuccess() ? response.getMessage() : "Error: " + response.getMessage());
    }

    private static void viewBookingHistory() {
        System.out.print("Enter your name as used during booking: ");
        String name = scanner.nextLine();
        List<TicketInfo> history = bookingService.viewBookingHistory(name);
        if (history.isEmpty()) {
            System.out.println("No bookings found for " + name);
        } else {
            history.forEach(TicketInfo::print);
        }
    }

    private static void adminMenu() {
        AdminUser admin = new AdminUser("A1", "System Admin");
        admin.login();

        boolean inAdmin = true;
        while (inAdmin) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1 View All Transports");
            System.out.println("2 Add Train");
            System.out.println("3 Add Bus");
            System.out.println("4 Add Metro");
            System.out.println("5 Update Transport (fare/seats)");
            System.out.println("6 Remove Transport");
            System.out.println("7 Back to Main Menu");

            int choice = readInt("Enter admin choice: ");
            switch (choice) {
                case 1 -> admin.viewAllTransports(transportService);
                case 2 -> addTrain(admin);
                case 3 -> addBus(admin);
                case 4 -> addMetro(admin);
                case 5 -> updateTransport(admin);
                case 6 -> removeTransport(admin);
                case 7 -> {
                    admin.logout();
                    inAdmin = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addTrain(AdminUser admin) {
        String[] common = readCommonTransportFields();
        System.out.print("Enter Coach Type (AC/Sleeper/General): ");
        String coachType = scanner.nextLine();
        Train train = new Train(common[0], common[1], common[2], common[3], common[4], common[5],
                Double.parseDouble(common[6]), Integer.parseInt(common[7]), coachType);
        admin.addTransport(transportService, train);
    }

    private static void addBus(AdminUser admin) {
        String[] common = readCommonTransportFields();
        System.out.print("Enter Bus Type (AC/Non-AC/Sleeper): ");
        String busType = scanner.nextLine();
        Bus bus = new Bus(common[0], common[1], common[2], common[3], common[4], common[5],
                Double.parseDouble(common[6]), Integer.parseInt(common[7]), busType);
        admin.addTransport(transportService, bus);
    }

    private static void addMetro(AdminUser admin) {
        String[] common = readCommonTransportFields();
        System.out.print("Enter Line Name: ");
        String lineName = scanner.nextLine();
        Metro metro = new Metro(common[0], common[1], common[2], common[3], common[4], common[5],
                Double.parseDouble(common[6]), Integer.parseInt(common[7]), lineName);
        admin.addTransport(transportService, metro);
    }

    private static String[] readCommonTransportFields() {
        System.out.print("Enter Transport ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Source: ");
        String source = scanner.nextLine();
        System.out.print("Enter Destination: ");
        String destination = scanner.nextLine();
        System.out.print("Enter Departure Time (HH:mm): ");
        String departure = scanner.nextLine();
        System.out.print("Enter Arrival Time (HH:mm): ");
        String arrival = scanner.nextLine();
        double fare = Double.parseDouble(promptNonEmpty("Enter Fare: "));
        int seats = Integer.parseInt(promptNonEmpty("Enter Total Seats: "));
        return new String[]{id, name, source, destination, departure, arrival,
                String.valueOf(fare), String.valueOf(seats)};
    }

    private static String promptNonEmpty(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void updateTransport(AdminUser admin) {
        System.out.print("Enter Transport ID to update: ");
        String id = scanner.nextLine();
        double fare = Double.parseDouble(promptNonEmpty("Enter new Fare: "));
        int seats = Integer.parseInt(promptNonEmpty("Enter new Total Seats: "));
        admin.updateTransport(transportService, id, fare, seats);
    }

    private static void removeTransport(AdminUser admin) {
        System.out.print("Enter Transport ID to remove: ");
        String id = scanner.nextLine();
        admin.removeTransport(transportService, id);
    }

    private static void simulateConcurrentBooking() {
        int demoSeats = 5;
        int concurrentAttempts = 20;

        Train demoTrain = new Train("DEMO", "Concurrency Demo Express", "CityA", "CityB",
                "10:00", "12:00", 100.0, demoSeats, "General");

        System.out.println("Starting demo: " + concurrentAttempts + " virtual threads competing for "
                + demoSeats + " seats on '" + demoTrain.getName() + "'...");

        List<Callable<Boolean>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < concurrentAttempts; i++) {
            tasks.add(demoTrain::reserveSeat);
        }

        long successCount = 0;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successCount++;
                }
            }
        } catch (Exception e) {
            System.out.println("Simulation error: " + e.getMessage());
            return;
        }

        System.out.println("Result: " + successCount + " out of " + concurrentAttempts
                + " booking attempts succeeded (seats available were " + demoSeats + ").");
        System.out.println("This confirms seat allocation stayed thread-safe under concurrent virtual threads.");
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            System.out.print(prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }
}