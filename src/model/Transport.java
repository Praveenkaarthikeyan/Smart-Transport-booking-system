package model;

import java.time.Duration;
import java.time.LocalTime;

public abstract class Transport {

    protected String transportId;
    protected String name;
    protected String source;
    protected String destination;
    protected String departureTime;
    protected String arrivalTime;
    protected double fare;
    protected int totalSeats;
    protected int availableSeats;

    protected Transport(String transportId, String name, String source, String destination,
                         String departureTime, String arrivalTime, double fare, int totalSeats) {
        this.transportId = transportId;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public abstract String getType();

    public synchronized boolean reserveSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    public synchronized void releaseSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    public long getDurationMinutes() {
        LocalTime dep = LocalTime.parse(departureTime);
        LocalTime arr = LocalTime.parse(arrivalTime);
        Duration duration = Duration.between(dep, arr);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration.toMinutes();
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-5s %-20s %-12s -> %-12s Dep:%-6s Arr:%-6s Fare:%-8.2f Seats:%d/%d",
                getType(), transportId, name, source, destination, departureTime, arrivalTime,
                fare, availableSeats, totalSeats);
    }
}