package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Location;
import service.LocationService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocationsHandler implements HttpHandler {

    private final LocationService locationService;

    public LocationsHandler(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<Location> all = locationService.getAllLocations();
        List<Object> json = all.stream()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .map(loc -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", loc.name());
                    map.put("parentCity", loc.parentCity());
                    map.put("state", loc.state());
                    map.put("isLocality", loc.isLocality());
                    return (Object) map;
                })
                .toList();
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(json));
    }
}