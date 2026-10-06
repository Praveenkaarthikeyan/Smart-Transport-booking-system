package planner;

public record ModeProfile(
        double avgSpeedKmph,
        double baseFare,
        double farePerKm,
        int overheadMinutes,
        double fareUncertainty
) {
}