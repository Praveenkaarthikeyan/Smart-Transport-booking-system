package model;

public class Train extends Transport {

    private String coachType;

    public Train(String transportId, String name, String source, String destination,
                 String departureTime, String arrivalTime, double fare, int totalSeats,
                 String coachType) {
        super(transportId, name, source, destination, departureTime, arrivalTime, fare, totalSeats);
        this.coachType = coachType;
    }

    public String getCoachType() {
        return coachType;
    }

    public void setCoachType(String coachType) {
        this.coachType = coachType;
    }

    @Override
    public String getType() {
        return "TRAIN";
    }

    @Override
    public String toString() {
        return super.toString() + " CoachType:" + coachType;
    }
}