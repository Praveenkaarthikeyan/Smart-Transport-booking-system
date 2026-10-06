package service;

import java.util.List;
import model.Passenger;
import model.TicketInfo;
import util.Response;

public interface BookingService {

    Response<TicketInfo> bookTicket(Passenger passenger, String transportId);

    Response<String> cancelTicket(String pnr);

    Response<TicketInfo> viewTicket(String pnr);

    List<TicketInfo> viewBookingHistory(String passengerName);
}