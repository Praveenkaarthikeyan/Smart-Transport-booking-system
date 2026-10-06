package file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Passenger;
import model.Ticket;
import model.Transport;
import service.TransportService;

public class FileManager {

    private static final String FILE_PATH = "bookings.csv";

    public void appendBooking(Ticket ticket) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toCsvLine(ticket));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving booking to file: " + e.getMessage());
        }
    }

    public void rewriteAll(Map<String, Ticket> bookings) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Ticket ticket : bookings.values()) {
                writer.write(toCsvLine(ticket));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error rewriting bookings file: " + e.getMessage());
        }
    }

    public Map<String, Ticket> loadBookings(TransportService transportService) {
        Map<String, Ticket> bookings = new LinkedHashMap<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return bookings;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    continue;
                }
                String pnr = parts[0];
                String passengerName = parts[1];
                String transportType = parts[2];
                String transportName = parts[3];
                int seatNumber = Integer.parseInt(parts[4]);
                double fare = Double.parseDouble(parts[5]);

                Transport transport = transportService.findByTypeAndName(transportType, transportName)
                        .orElse(null);
                if (transport == null) {
                    continue;
                }

                Passenger passenger = new Passenger("P-" + pnr, passengerName, 0, "-");
                Ticket ticket = new Ticket(pnr, passenger, transport, seatNumber, LocalDate.now(), fare);
                bookings.put(pnr, ticket);

                transport.reserveSeat();
            }
        } catch (IOException e) {
            System.out.println("Error loading bookings file: " + e.getMessage());
        }

        return bookings;
    }

    private String toCsvLine(Ticket ticket) {
        Transport t = ticket.getTransport();
        return ticket.getPnr() + "," +
                ticket.getPassenger().name() + "," +
                t.getType() + "," +
                t.getName() + "," +
                ticket.getSeatNumber() + "," +
                ticket.getFare();
    }
}