package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Bus;
import model.Metro;
import model.Train;
import model.Transport;
import service.TransportService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminAddHandler implements HttpHandler {

    private final TransportService transportService;

    public AdminAddHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtil.parseFlatObject(HttpUtil.readBody(exchange));
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            String type = body.getOrDefault("type", "").toUpperCase();
            String id = body.get("id");
            String name = body.get("name");
            String source = body.get("source");
            String destination = body.get("destination");
            String departure = body.get("departureTime");
            String arrival = body.get("arrivalTime");
            double fare = Double.parseDouble(body.get("fare"));
            int totalSeats = Integer.parseInt(body.get("totalSeats"));
            String extra = body.getOrDefault("extra", "General");

            Transport transport = switch (type) {
                case "TRAIN" -> new Train(id, name, source, destination, departure, arrival, fare, totalSeats, extra);
                case "BUS" -> new Bus(id, name, source, destination, departure, arrival, fare, totalSeats, extra);
                case "METRO" -> new Metro(id, name, source, destination, departure, arrival, fare, totalSeats, extra);
                default -> null;
            };

            if (transport == null) {
                result.put("success", false);
                result.put("message", "Unknown transport type: " + type);
            } else {
                transportService.addTransport(transport);
                result.put("success", true);
                result.put("message", type + " added successfully");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Invalid input: " + e.getMessage());
        }

        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }
}