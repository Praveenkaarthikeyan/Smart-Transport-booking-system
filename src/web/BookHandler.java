package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Passenger;
import model.TicketInfo;
import service.BookingService;
import util.Response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookHandler implements HttpHandler {

    private final BookingService bookingService;

    public BookHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendJson(exchange, 405, JsonUtil.toJson(Map.of("success", false, "message", "Use POST")));
            return;
        }

        Map<String, String> body = JsonUtil.parseFlatObject(HttpUtil.readBody(exchange));
        String name = body.getOrDefault("name", "Guest");
        int age = parseIntSafe(body.get("age"));
        String gender = body.getOrDefault("gender", "-");
        String transportId = body.getOrDefault("transportId", "");

        Passenger passenger = new Passenger("P-" + System.currentTimeMillis(), name, age, gender);
        Response<TicketInfo> response = bookingService.bookTicket(passenger, transportId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("ticket", Dto.ticketMap(response.getData()));
        } else {
            result.put("message", response.getMessage());
        }
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}