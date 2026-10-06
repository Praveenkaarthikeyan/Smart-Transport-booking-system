package web;

import model.TicketInfo;
import model.Transport;
import planner.RouteLeg;
import planner.RouteOption;
import service.RecommendationService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts the project's existing model/service objects into plain
 * Map<String,Object> structures that JsonUtil can turn into JSON.
 * Keeping this separate means none of the original model classes
 * (Transport, TicketInfo, etc.) need to know anything about the web layer.
 */
public final class Dto {

    private Dto() {
    }

    public static Map<String, Object> transportMap(Transport t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getTransportId());
        map.put("type", t.getType());
        map.put("name", t.getName());
        map.put("source", t.getSource());
        map.put("destination", t.getDestination());
        map.put("departureTime", t.getDepartureTime());
        map.put("arrivalTime", t.getArrivalTime());
        map.put("durationMinutes", t.getDurationMinutes());
        map.put("fare", t.getFare());
        map.put("totalSeats", t.getTotalSeats());
        map.put("availableSeats", t.getAvailableSeats());
        return map;
    }

    public static Map<String, Object> ticketMap(TicketInfo info) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pnr", info.pnr());
        map.put("passengerName", info.passengerName());
        map.put("transportType", info.transportType());
        map.put("transportName", info.transportName());
        map.put("seatNumber", info.seatNumber());
        map.put("fare", info.fare());
        return map;
    }

    public static Map<String, Object> scoredOptionMap(RecommendationService.ScoredOption option) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("transport", transportMap(option.transport()));
        map.put("score", Math.round(option.score() * 100.0) / 100.0);
        map.put("reason", option.reason());
        return map;
    }

    public static Map<String, Object> routeLegMap(RouteLeg leg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mode", leg.mode().name());
        map.put("from", leg.from().name());
        map.put("to", leg.to().name());
        map.put("distanceKm", Math.round(leg.distanceKm() * 10.0) / 10.0);
        map.put("estimatedMinutes", leg.estimatedMinutes());
        map.put("fareMin", Math.round(leg.fareMin()));
        map.put("fareMax", Math.round(leg.fareMax()));
        map.put("scheduled", leg.scheduled());
        map.put("label", leg.label());
        return map;
    }

    public static Map<String, Object> routeOptionMap(RouteOption option) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Object> legs = option.legs().stream().map(l -> (Object) routeLegMap(l)).toList();
        map.put("legs", legs);
        map.put("totalMinutes", option.totalMinutes());
        map.put("totalFareMin", Math.round(option.totalFareMin()));
        map.put("totalFareMax", Math.round(option.totalFareMax()));
        map.put("category", option.category());
        map.put("score", Math.round(option.score() * 100.0) / 100.0);
        map.put("reason", option.reason());
        return map;
    }
}