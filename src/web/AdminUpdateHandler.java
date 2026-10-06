package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TransportService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminUpdateHandler implements HttpHandler {

    private final TransportService transportService;

    public AdminUpdateHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtil.parseFlatObject(HttpUtil.readBody(exchange));
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            String id = body.get("id");
            double fare = Double.parseDouble(body.get("fare"));
            int totalSeats = Integer.parseInt(body.get("totalSeats"));

            boolean updated = transportService.updateTransport(id, fare, totalSeats);
            result.put("success", updated);
            result.put("message", updated ? "Transport updated successfully" : "Transport ID not found");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Invalid input: " + e.getMessage());
        }

        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }
}