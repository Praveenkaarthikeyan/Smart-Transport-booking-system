package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.TicketInfo;
import service.BookingService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HistoryHandler implements HttpHandler {

    private final BookingService bookingService;

    public HistoryHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getQuery());
        String name = query.getOrDefault("name", "");

        List<TicketInfo> history = bookingService.viewBookingHistory(name);
        List<Object> json = history.stream().map(t -> (Object) Dto.ticketMap(t)).toList();
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(json));
    }
}