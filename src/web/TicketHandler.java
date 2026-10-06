package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.TicketInfo;
import service.BookingService;
import util.Response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TicketHandler implements HttpHandler {

    private final BookingService bookingService;

    public TicketHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getQuery());
        String pnr = query.getOrDefault("pnr", "");

        Response<TicketInfo> response = bookingService.viewTicket(pnr);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("ticket", Dto.ticketMap(response.getData()));
        } else {
            result.put("message", response.getMessage());
        }
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }
}