package model;

public class Metro extends Transport {

    private String lineName;

    public Metro(String transportId, String name, String source, String destination,
                 String departureTime, String arrivalTime, double fare, int totalSeats,
                 String lineName) {
        super(transportId, name, source, destination, departureTime, arrivalTime, fare, totalSeats);
        this.lineName = lineName;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    @Override
    public String getType() {
        return "METRO";
    }

    @Override
    public String toString() {
        return super.toString() + " Line:" + lineName;
    }
}