package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import planner.RouteOption;
import planner.RoutePlannerService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanRouteHandler implements HttpHandler {

    private final RoutePlannerService routePlannerService;

    public PlanRouteHandler(RoutePlannerService routePlannerService) {
        this.routePlannerService = routePlannerService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getQuery());
        String source = query.getOrDefault("source", "");
        String destination = query.getOrDefault("destination", "");

        RoutePlannerService.PlanResult result = routePlannerService.plan(source, destination);

        Map<String, Object> response = new LinkedHashMap<>();
        if (result == null) {
            response.put("success", false);
            response.put("message", "Unknown source or destination. Please pick from the list.");
            HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(response));
            return;
        }

        response.put("success", true);
        response.put("tripType", result.tripType().name());

        List<Object> allJson = result.allOptions().stream()
                .map(o -> (Object) Dto.routeOptionMap(o))
                .toList();
        response.put("options", allJson);
        response.put("bestDirect", result.bestDirect() != null ? Dto.routeOptionMap(result.bestDirect()) : null);
        response.put("bestMixed", result.bestMixed() != null ? Dto.routeOptionMap(result.bestMixed()) : null);

        HttpUtil.sendJson(exchange, 200, JsonUtil.toJson(response));
    }
}