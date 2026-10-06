package planner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Location;
import model.Transport;
import model.TripType;
import service.LocationService;
import service.TransportService;

public class RoutePlannerService {

    private static final double TIME_WEIGHT = 0.4;
    private static final double FARE_WEIGHT = 0.3;
    private static final double AVAIL_WEIGHT = 0.3;
    private static final double MIXED_ROUTE_THRESHOLD_KM = 6.0;
    private static final double FIRST_LAST_MILE_KM = 2.0;

    private final LocationService locationService;
    private final TransportService transportService;

    public RoutePlannerService(LocationService locationService, TransportService transportService) {
        this.locationService = locationService;
        this.transportService = transportService;
    }

    public List<Mode> candidateModes(TripType tripType) {
        return switch (tripType) {
            case INTRACITY -> List.of(Mode.AUTO, Mode.METRO, Mode.BUS);
            case INTERCITY_SAME_STATE -> List.of(Mode.BUS, Mode.TRAIN);
            case INTERSTATE -> List.of(Mode.TRAIN, Mode.FLIGHT);
        };
    }

    public record PlanResult(TripType tripType, List<RouteOption> allOptions,
                              RouteOption bestDirect, RouteOption bestMixed) {
    }

    public PlanResult plan(String sourceName, String destinationName) {
        Optional<Location> sourceOpt = locationService.findByName(sourceName);
        Optional<Location> destOpt = locationService.findByName(destinationName);
        if (sourceOpt.isEmpty() || destOpt.isEmpty()) {
            return null;
        }

        Location source = sourceOpt.get();
        Location destination = destOpt.get();
        TripType tripType = locationService.classify(source, destination);

        List<RouteOption> candidates = new ArrayList<>();
        for (Mode mode : candidateModes(tripType)) {
            candidates.add(buildDirectOption(source, destination, mode));
        }

        if (tripType == TripType.INTRACITY) {
            double distance = ModeEstimator.effectiveDistanceKm(source, destination);
            if (distance > MIXED_ROUTE_THRESHOLD_KM) {
                candidates.add(buildMixedOption(source, destination));
            }
        } else {
            boolean hasRealDirect = candidates.stream().anyMatch(c -> c.legs().get(0).scheduled());
            if (!hasRealDirect) {
                buildBestHubRoute(source, destination).ifPresent(candidates::add);
            }
        }

        List<RouteOption> ranked = rescore(candidates).stream()
                .sorted(Comparator.comparingDouble(RouteOption::score).reversed())
                .toList();

        RouteOption topDirect = ranked.stream().filter(r -> !r.isMixed()).findFirst().orElse(null);
        RouteOption topMixed = ranked.stream().filter(RouteOption::isMixed).findFirst().orElse(null);

        return new PlanResult(tripType, ranked, topDirect, topMixed);
    }

    private RouteOption buildDirectOption(Location source, Location destination, Mode mode) {
        double distance = ModeEstimator.effectiveDistanceKm(source, destination);

        Optional<Transport> real = transportService.searchByRoute(source.parentCity(), destination.parentCity())
                .stream()
                .filter(t -> t.getType().equalsIgnoreCase(mode.name()))
                .findFirst();

        RouteLeg leg;
        if (real.isPresent()) {
            Transport t = real.get();
            leg = new RouteLeg(mode, source, destination, distance, (int) t.getDurationMinutes(),
                    t.getFare(), t.getFare(), true, t.getName());
        } else {
            int minutes = ModeEstimator.estimateMinutes(mode, distance);
            double fareMin = ModeEstimator.estimateFareMin(mode, distance);
            double fareMax = ModeEstimator.estimateFareMax(mode, distance);
            leg = new RouteLeg(mode, source, destination, distance, minutes, fareMin, fareMax, false,
                    defaultLabel(mode));
        }

        return new RouteOption(List.of(leg), leg.estimatedMinutes(), leg.fareMin(), leg.fareMax(), "DIRECT", 0.0, "");
    }

    private RouteOption buildMixedOption(Location source, Location destination) {
        double totalDistance = ModeEstimator.effectiveDistanceKm(source, destination);
        double trunkDistance = Math.max(totalDistance - (2 * FIRST_LAST_MILE_KM), 1.0);

        int metroMinutes = ModeEstimator.estimateMinutes(Mode.METRO, trunkDistance);
        int busMinutes = ModeEstimator.estimateMinutes(Mode.BUS, trunkDistance);
        Mode trunkMode = metroMinutes <= busMinutes ? Mode.METRO : Mode.BUS;

        RouteLeg leg1 = syntheticLeg(Mode.AUTO, source, destination, FIRST_LAST_MILE_KM, "Auto to station");
        RouteLeg leg2 = syntheticLeg(trunkMode, source, destination, trunkDistance,
                trunkMode == Mode.METRO ? "Metro" : "City Bus");
        RouteLeg leg3 = syntheticLeg(Mode.AUTO, source, destination, FIRST_LAST_MILE_KM, "Auto from station");

        List<RouteLeg> legs = List.of(leg1, leg2, leg3);
        int totalMinutes = legs.stream().mapToInt(RouteLeg::estimatedMinutes).sum();
        double fareMin = legs.stream().mapToDouble(RouteLeg::fareMin).sum();
        double fareMax = legs.stream().mapToDouble(RouteLeg::fareMax).sum();

        return new RouteOption(legs, totalMinutes, fareMin, fareMax, "MIXED", 0.0, "");
    }

    private RouteLeg syntheticLeg(Mode mode, Location from, Location to, double distanceKm, String label) {
        int minutes = ModeEstimator.estimateMinutes(mode, distanceKm);
        double fareMin = ModeEstimator.estimateFareMin(mode, distanceKm);
        double fareMax = ModeEstimator.estimateFareMax(mode, distanceKm);
        return new RouteLeg(mode, from, to, distanceKm, minutes, fareMin, fareMax, false, label);
    }

    private Optional<RouteOption> buildBestHubRoute(Location source, Location destination) {
        List<Location> hubs = locationService.getAllLocations().stream()
                .filter(loc -> !loc.isLocality())
                .filter(loc -> !loc.name().equalsIgnoreCase(source.parentCity())
                        && !loc.name().equalsIgnoreCase(destination.parentCity()))
                .toList();

        RouteOption best = null;
        int bestTotalMinutes = Integer.MAX_VALUE;

        for (Location hub : hubs) {
            List<Transport> leg1Options = transportService.searchByRoute(source.parentCity(), hub.name());
            List<Transport> leg2Options = transportService.searchByRoute(hub.name(), destination.parentCity());
            if (leg1Options.isEmpty() || leg2Options.isEmpty()) {
                continue;
            }

            Transport t1 = leg1Options.stream().min(Comparator.comparingLong(Transport::getDurationMinutes)).orElseThrow();
            Transport t2 = leg2Options.stream().min(Comparator.comparingLong(Transport::getDurationMinutes)).orElseThrow();

            int totalMinutes = (int) (t1.getDurationMinutes() + t2.getDurationMinutes());
            if (totalMinutes < bestTotalMinutes) {
                bestTotalMinutes = totalMinutes;

                RouteLeg leg1 = new RouteLeg(Mode.valueOf(t1.getType()), source, hub,
                        ModeEstimator.effectiveDistanceKm(source, hub), (int) t1.getDurationMinutes(),
                        t1.getFare(), t1.getFare(), true, t1.getName());
                RouteLeg leg2 = new RouteLeg(Mode.valueOf(t2.getType()), hub, destination,
                        ModeEstimator.effectiveDistanceKm(hub, destination), (int) t2.getDurationMinutes(),
                        t2.getFare(), t2.getFare(), true, t2.getName());

                double totalFare = t1.getFare() + t2.getFare();
                best = new RouteOption(List.of(leg1, leg2), totalMinutes, totalFare, totalFare, "MIXED", 0.0, "");
            }
        }

        return Optional.ofNullable(best);
    }

    private String defaultLabel(Mode mode) {
        return switch (mode) {
            case AUTO -> "Auto/Rapido";
            case METRO -> "Metro";
            case BUS -> "Bus";
            case TRAIN -> "Train";
            case FLIGHT -> "Flight";
        };
    }

    private List<RouteOption> rescore(List<RouteOption> options) {
        if (options.isEmpty()) {
            return options;
        }

        int minTime = options.stream().mapToInt(RouteOption::totalMinutes).min().orElse(0);
        int maxTime = options.stream().mapToInt(RouteOption::totalMinutes).max().orElse(0);
        double minFare = options.stream().mapToDouble(this::midFare).min().orElse(0);
        double maxFare = options.stream().mapToDouble(this::midFare).max().orElse(0);

        List<RouteOption> result = new ArrayList<>();
        for (RouteOption o : options) {
            double timeScore = maxTime == minTime ? 1.0 : (double) (maxTime - o.totalMinutes()) / (maxTime - minTime);
            double fareScore = maxFare == minFare ? 1.0 : (maxFare - midFare(o)) / (maxFare - minFare);
            double availScore = 1.0;
            double total = (timeScore * TIME_WEIGHT) + (fareScore * FARE_WEIGHT) + (availScore * AVAIL_WEIGHT);
            String reason = buildReason(timeScore * TIME_WEIGHT, fareScore * FARE_WEIGHT, availScore * AVAIL_WEIGHT);
            if (o.isMixed()) {
                if (o.legs().size() == 2) {
                    String hubName = o.legs().get(0).to().name();
                    reason = "Connects via " + hubName + " — " + reason;
                } else {
                    reason = "Mixed route (first/last mile by Auto) — " + reason;
                }
            }
            result.add(new RouteOption(o.legs(), o.totalMinutes(), o.totalFareMin(), o.totalFareMax(),
                    o.category(), total, reason));
        }
        return result;
    }

    private double midFare(RouteOption o) {
        return (o.totalFareMin() + o.totalFareMax()) / 2;
    }

    private String buildReason(double weightedTime, double weightedFare, double weightedAvail) {
        double max = Math.max(weightedTime, Math.max(weightedFare, weightedAvail));
        if (max == weightedTime) {
            return "Fastest option for this route";
        } else if (max == weightedFare) {
            return "Cheapest option for this route";
        } else {
            return "Good balance of cost and availability";
        }
    }
}