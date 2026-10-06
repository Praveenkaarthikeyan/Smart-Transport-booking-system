package model;

public class Flight extends Transport {

    private String airline;

    public Flight(String transportId, String name, String source, String destination,
                  String departureTime, String arrivalTime, double fare, int totalSeats,
                  String airline) {
        super(transportId, name, source, destination, departureTime, arrivalTime, fare, totalSeats);
        this.airline = airline;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    @Override
    public String getType() {
        return "FLIGHT";
    }

    @Override
    public String toString() {
        return super.toString() + " Airline:" + airline;
    }
}