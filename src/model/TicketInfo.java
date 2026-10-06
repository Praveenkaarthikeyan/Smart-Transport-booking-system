package model;

public record TicketInfo(
        String pnr,
        String passengerName,
        String transportType,
        String transportName,
        int seatNumber,
        double fare
) {
    public void print() {
        System.out.println("---------------------------------------------");
        System.out.println("PNR              : " + pnr);
        System.out.println("Passenger Name   : " + passengerName);
        System.out.println("Transport Type   : " + transportType);
        System.out.println("Transport Name   : " + transportName);
        System.out.println("Seat Number      : " + seatNumber);
        System.out.println("Fare             : " + fare);
        System.out.println("---------------------------------------------");
    }
}