package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TransportService;

import java.io.IOException;
import java.util.List;

public class CitiesHandler implements HttpHandler {

    private final TransportService transportService;

    public CitiesHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<String> cities = transportService.getAllCities();

        List<Object> json = cities.stream()
                .map(c -> (Object) c)
                .toList();

        HttpUtil.sendJson(
                exchange,
                200,
                JsonUtil.toJson(json)
        );
    }
}