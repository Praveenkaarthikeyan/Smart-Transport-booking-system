package model;

public record Location(String name, String parentCity, String state, double latitude, double longitude) {

    public boolean isLocality() {
        return !name.equalsIgnoreCase(parentCity);
    }
}