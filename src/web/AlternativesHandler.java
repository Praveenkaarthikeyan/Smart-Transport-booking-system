package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transport;
import service.RecommendationService;
import service.TransportService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AlternativesHandler implements HttpHandler {

    private final TransportService transportService;
    private final RecommendationService recommendationService;

    public AlternativesHandler(TransportService transportService, RecommendationService recommendationService) {
        this.transportService = transportService;
        this.recommendationService = recommendationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getQuery());
        String source = query.getOrDefault("source", "");
        String destination = query.getOrDefault("destination", "");
        String excludeId = query.getOrDefault("excludeId", "");

        List<Transport> options = transportService.searchByRoute(source, destination).stream()
                .filter(t -> !t.getTransportId().equalsIgnoreCase(excludeId))
                .toList();

        List<RecommendationService.ScoredOption> ranked = recommendationService.rank(options);
        List<Object> json = ranked.stream().map(o -> (Object) Dto.scoredOptionMap(o)).toList();
        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(json));
    }
}