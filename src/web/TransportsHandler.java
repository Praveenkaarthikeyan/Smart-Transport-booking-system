package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transport;
import service.TransportService;

import java.io.IOException;
import java.util.List;

public class TransportsHandler implements HttpHandler {

    private final TransportService transportService;

    public TransportsHandler(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<Transport> all = transportService.getTransportList();
        List<Object> json = all.stream().map(t -> (Object) Dto.transportMap(t)).toList();
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(json));
    }
}