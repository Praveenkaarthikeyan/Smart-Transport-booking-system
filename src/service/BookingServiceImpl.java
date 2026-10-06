package service;

import exception.InvalidPNRException;
import exception.SeatUnavailableException;
import file.FileManager;
import model.Passenger;
import model.Ticket;
import model.TicketInfo;
import model.Transport;
import util.PNRGenerator;
import util.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BookingServiceImpl implements BookingService {

    private final Map<String, Ticket> bookings;
    private final TransportService transportService;
    private final FileManager fileManager;

    public BookingServiceImpl(TransportService transportService, FileManager fileManager) {
        this.transportService = transportService;
        this.fileManager = fileManager;
        this.bookings = new ConcurrentHashMap<>(fileManager.loadBookings(transportService));
    }

    @Override
    public Response<TicketInfo> bookTicket(Passenger passenger, String transportId) {
        try {
            Optional<Transport> transportOpt = transportService.findById(transportId);
            if (transportOpt.isEmpty()) {
                throw new SeatUnavailableException("Transport ID not found: " + transportId);
            }

            Transport transport = transportOpt.get();
            if (!transport.reserveSeat()) {
                throw new SeatUnavailableException("No seats available on: " + transport.getName());
            }

            int seatNumber = transport.getTotalSeats() - transport.getAvailableSeats();
            String pnr = PNRGenerator.generatePNR();

            Ticket ticket = new Ticket(pnr, passenger, transport, seatNumber, LocalDate.now(), transport.getFare());
            bookings.put(pnr, ticket);
            fileManager.appendBooking(ticket);

            return Response.success(toTicketInfo(ticket), "Booking successful");
        } catch (SeatUnavailableException e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public Response<String> cancelTicket(String pnr) {
        try {
            Ticket ticket = bookings.get(pnr);
            if (ticket == null) {
                throw new InvalidPNRException("No booking found for PNR: " + pnr);
            }

            bookings.remove(pnr);
            ticket.getTransport().releaseSeat();
            fileManager.rewriteAll(bookings);

            return Response.success(pnr, "Ticket cancelled successfully");
        } catch (InvalidPNRException e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public Response<TicketInfo> viewTicket(String pnr) {
        try {
            Ticket ticket = bookings.get(pnr);
            if (ticket == null) {
                throw new InvalidPNRException("No booking found for PNR: " + pnr);
            }
            return Response.success(toTicketInfo(ticket));
        } catch (InvalidPNRException e) {
            return Response.failure(e.getMessage());
        }
    }

    @Override
    public List<TicketInfo> viewBookingHistory(String passengerName) {
        return bookings.values().stream()
                .filter(t -> t.getPassenger().name().equalsIgnoreCase(passengerName))
                .map(this::toTicketInfo)
                .toList();
    }

    private TicketInfo toTicketInfo(Ticket ticket) {
        Transport t = ticket.getTransport();
        return new TicketInfo(
                ticket.getPnr(),
                ticket.getPassenger().name(),
                t.getType(),
                t.getName(),
                ticket.getSeatNumber(),
                ticket.getFare()
        );
    }
}