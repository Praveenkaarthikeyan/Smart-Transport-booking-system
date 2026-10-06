package model;

import java.time.LocalDate;

public class Ticket {

    private String pnr;
    private Passenger passenger;
    private Transport transport;
    private int seatNumber;
    private LocalDate bookingDate;
    private double fare;

    public Ticket(String pnr, Passenger passenger, Transport transport, int seatNumber,
                  LocalDate bookingDate, double fare) {
        this.pnr = pnr;
        this.passenger = passenger;
        this.transport = transport;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.fare = fare;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "pnr='" + pnr + '\'' +
                ", passenger=" + passenger +
                ", transport=" + transport.getType() + " " + transport.getName() +
                ", seatNumber=" + seatNumber +
                ", bookingDate=" + bookingDate +
                ", fare=" + fare +
                '}';
    }
}