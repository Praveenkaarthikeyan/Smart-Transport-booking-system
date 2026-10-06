package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transport;
import service.TransportService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchHandler implements HttpHandler {

    private final TransportService transportService;

    public SearchHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getQuery());
        String source = query.getOrDefault("source", "");
        String destination = query.getOrDefault("destination", "");

        List<Transport> results = transportService.searchByRoute(source, destination);
        List<Object> json = results.stream().map(t -> (Object) Dto.transportMap(t)).toList();
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(json));
    }
}