package model;

public class Bus extends Transport {

    private String busType;

    public Bus(String transportId, String name, String source, String destination,
               String departureTime, String arrivalTime, double fare, int totalSeats,
               String busType) {
        super(transportId, name, source, destination, departureTime, arrivalTime, fare, totalSeats);
        this.busType = busType;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    @Override
    public String getType() {
        return "BUS";
    }

    @Override
    public String toString() {
        return super.toString() + " BusType:" + busType;
    }
}