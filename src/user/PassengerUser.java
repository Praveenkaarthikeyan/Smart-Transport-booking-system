package user;

public class PassengerUser extends User {

    public PassengerUser(String id, String name) {
        super(id, name);
    }

    @Override
    public void login() {
        System.out.println("Passenger '" + name + "' logged in.");
    }

    @Override
    public void logout() {
        System.out.println("Passenger '" + name + "' logged out.");
    }
}