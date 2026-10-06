package planner;

import model.Location;
import util.DistanceUtil;

import java.util.Map;

public final class ModeEstimator {

    private static final Map<Mode, ModeProfile> PROFILES = Map.of(
            Mode.AUTO, new ModeProfile(22, 30, 15, 3, 0.20),
            Mode.METRO, new ModeProfile(34, 10, 2, 10, 0.05),
            Mode.BUS, new ModeProfile(28, 15, 1.5, 12, 0.10),
            Mode.TRAIN, new ModeProfile(55, 50, 1.2, 20, 0.15),
            Mode.FLIGHT, new ModeProfile(600, 1800, 5.5, 90, 0.25)
    );

    private ModeEstimator() {
    }

    public static ModeProfile profileOf(Mode mode) {
        return PROFILES.get(mode);
    }

    public static double effectiveDistanceKm(Location a, Location b) {
        double straightLine = DistanceUtil.distanceKm(a, b);
        return straightLine * 1.25;
    }

    public static int estimateMinutes(Mode mode, double distanceKm) {
        ModeProfile p = PROFILES.get(mode);
        double travelMinutes = (distanceKm / p.avgSpeedKmph()) * 60.0;
        return (int) Math.round(travelMinutes + p.overheadMinutes());
    }

    public static double estimateFareMid(Mode mode, double distanceKm) {
        ModeProfile p = PROFILES.get(mode);
        return p.baseFare() + (distanceKm * p.farePerKm());
    }

    public static double estimateFareMin(Mode mode, double distanceKm) {
        return estimateFareMid(mode, distanceKm) * (1 - PROFILES.get(mode).fareUncertainty());
    }

    public static double estimateFareMax(Mode mode, double distanceKm) {
        return estimateFareMid(mode, distanceKm) * (1 + PROFILES.get(mode).fareUncertainty());
    }
}