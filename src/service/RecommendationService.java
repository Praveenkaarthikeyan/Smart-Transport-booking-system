package service;

import model.Transport;

import java.util.Comparator;
import java.util.List;

public class RecommendationService {

    private static final double TIME_WEIGHT = 0.4;
    private static final double FARE_WEIGHT = 0.3;
    private static final double SEAT_WEIGHT = 0.3;

    public record ScoredOption(Transport transport, double score, String reason) {
    }

    public List<ScoredOption> rank(List<Transport> candidates) {
        List<Transport> available = candidates.stream()
                .filter(t -> t.getAvailableSeats() > 0)
                .toList();

        if (available.isEmpty()) {
            return List.of();
        }

        long minTime = available.stream().mapToLong(Transport::getDurationMinutes).min().orElse(0);
        long maxTime = available.stream().mapToLong(Transport::getDurationMinutes).max().orElse(0);
        double minFare = available.stream().mapToDouble(Transport::getFare).min().orElse(0);
        double maxFare = available.stream().mapToDouble(Transport::getFare).max().orElse(0);
        int minSeats = available.stream().mapToInt(Transport::getAvailableSeats).min().orElse(0);
        int maxSeats = available.stream().mapToInt(Transport::getAvailableSeats).max().orElse(0);

        return available.stream()
                .map(t -> scoreOption(t, minTime, maxTime, minFare, maxFare, minSeats, maxSeats))
                .sorted(Comparator.comparingDouble(ScoredOption::score).reversed())
                .toList();
    }

    private ScoredOption scoreOption(Transport t, long minTime, long maxTime,
                                      double minFare, double maxFare, int minSeats, int maxSeats) {
        double timeScore = maxTime == minTime ? 1.0 : (double) (maxTime - t.getDurationMinutes()) / (maxTime - minTime);
        double fareScore = maxFare == minFare ? 1.0 : (maxFare - t.getFare()) / (maxFare - minFare);
        double seatScore = maxSeats == minSeats ? 1.0 : (double) (t.getAvailableSeats() - minSeats) / (maxSeats - minSeats);

        double totalScore = (timeScore * TIME_WEIGHT) + (fareScore * FARE_WEIGHT) + (seatScore * SEAT_WEIGHT);

        String reason = buildReason(timeScore, fareScore, seatScore);
        return new ScoredOption(t, totalScore, reason);
    }

    private String buildReason(double timeScore, double fareScore, double seatScore) {
        double weightedTime = timeScore * TIME_WEIGHT;
        double weightedFare = fareScore * FARE_WEIGHT;
        double weightedSeat = seatScore * SEAT_WEIGHT;

        double max = Math.max(weightedTime, Math.max(weightedFare, weightedSeat));
        if (max == weightedTime) {
            return "Recommended mainly for shorter travel time";
        } else if (max == weightedFare) {
            return "Recommended mainly for lower fare";
        } else {
            return "Recommended mainly for better seat availability";
        }
    }
}