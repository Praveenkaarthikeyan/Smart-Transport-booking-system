package planner;

import java.util.List;

public record RouteOption(
        List<RouteLeg> legs,
        int totalMinutes,
        double totalFareMin,
        double totalFareMax,
        String category,
        double score,
        String reason
) {
    public boolean isMixed() {
        return legs.size() > 1;
    }
}