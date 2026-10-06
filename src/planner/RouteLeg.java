package planner;

import model.Location;

public record RouteLeg(
        Mode mode,
        Location from,
        Location to,
        double distanceKm,
        int estimatedMinutes,
        double fareMin,
        double fareMax,
        boolean scheduled,
        String label
) {
}