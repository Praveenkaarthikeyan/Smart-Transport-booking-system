package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TransportService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminRemoveHandler implements HttpHandler {

    private final TransportService transportService;

    public AdminRemoveHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtil.parseFlatObject(HttpUtil.readBody(exchange));
        String id = body.getOrDefault("id", "");

        boolean removed = transportService.removeTransport(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", removed);
        result.put("message", removed ? "Transport removed successfully" : "Transport ID not found");
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(result));
    }
}