package user;

import model.Transport;
import service.TransportService;

public class AdminUser extends User {

    public AdminUser(String id, String name) {
        super(id, name);
    }

    @Override
    public void login() {
        System.out.println("Admin '" + name + "' logged in.");
    }

    @Override
    public void logout() {
        System.out.println("Admin '" + name + "' logged out.");
    }

    public void viewAllTransports(TransportService transportService) {
        transportService.displayAll();
    }

    public void addTransport(TransportService transportService, Transport transport) {
        transportService.addTransport(transport);
    }

    public void removeTransport(TransportService transportService, String transportId) {
        transportService.removeTransport(transportId);
    }

    public void updateTransport(TransportService transportService, String transportId,
                                 double newFare, int newTotalSeats) {
        transportService.updateTransport(transportId, newFare, newTotalSeats);
    }
}