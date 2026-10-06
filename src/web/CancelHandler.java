package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.BookingService;
import util.Response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CancelHandler implements HttpHandler {

    private final BookingService bookingService;

    public CancelHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtil.parseFlatObject(HttpUtil.readBody(exchange));
        String pnr = body.getOrDefault("pnr", "");

        Response<String> response = bookingService.cancelTicket(pnr);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", response.isSuccess());
        result.put("message", response.getMessage());
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }
}