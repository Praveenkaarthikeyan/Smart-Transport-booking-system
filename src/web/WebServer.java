package web;

import com.sun.net.httpserver.HttpServer;
import file.FileManager;
import planner.RoutePlannerService;
import service.BookingService;
import service.BookingServiceImpl;
import service.LocationService;
import service.RecommendationService;
import service.TransportService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Entry point for the web version of the Smart Public Transport Booking
 * System. Uses Java's built-in HttpServer (com.sun.net.httpserver) - no
 * external framework, no Maven/Gradle. Reuses every existing service
 * class unchanged.
 *
 * Each incoming HTTP request is handled on its own Virtual Thread
 * (Unit 4), so this is not just a menu demo anymore - real concurrent
 * requests now exercise the synchronized Transport.reserveSeat() method
 * for genuine thread-safe seat allocation.
 */
public class WebServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        TransportService transportService = new TransportService();
        FileManager fileManager = new FileManager();
        BookingService bookingService = new BookingServiceImpl(transportService, fileManager);
        RecommendationService recommendationService = new RecommendationService();
        LocationService locationService = new LocationService();
        RoutePlannerService routePlannerService = new RoutePlannerService(locationService, transportService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/transports", new TransportsHandler(transportService));
        server.createContext("/api/cities", new CitiesHandler(transportService));
        server.createContext("/api/locations", new LocationsHandler(locationService));
        server.createContext("/api/plan", new PlanRouteHandler(routePlannerService));
        server.createContext("/api/search", new SearchHandler(transportService));
        server.createContext("/api/book", new BookHandler(bookingService));
        server.createContext("/api/alternatives", new AlternativesHandler(transportService, recommendationService));
        server.createContext("/api/ticket", new TicketHandler(bookingService));
        server.createContext("/api/cancel", new CancelHandler(bookingService));
        server.createContext("/api/history", new HistoryHandler(bookingService));
        server.createContext("/api/admin/add", new AdminAddHandler(transportService));
        server.createContext("/api/admin/update", new AdminUpdateHandler(transportService));
        server.createContext("/api/admin/remove", new AdminRemoveHandler(transportService));
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        System.out.println("===================================================");
        System.out.println("Smart Public Transport Booking System is running!");
        System.out.println("Open this in your browser: http://localhost:" + PORT);
        System.out.println("Press Ctrl+C in this terminal to stop the server.");
        System.out.println("===================================================");
    }
}