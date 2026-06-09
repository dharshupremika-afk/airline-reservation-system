package airline.model;

import java.io.Serializable;

public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int flightId;
    private String flightNumber;
    private String source;
    private String destination;
    private String departureTime;
    private int seatsAvailable;
    private double fare;

    public Flight(int flightId, String flightNumber, String source, String destination,
                  String departureTime, int seatsAvailable, double fare) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.seatsAvailable = seatsAvailable;
        this.fare = fare;
    }

    public int getFlightId() {
        return flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public boolean hasSeats(int seatsRequested) {
        return seatsRequested > 0 && seatsAvailable >= seatsRequested;
    }

    public void bookSeats(int seatsRequested) {
        seatsAvailable -= seatsRequested;
    }

    public void cancelSeats(int seatsToRestore) {
        seatsAvailable += seatsToRestore;
    }

    public boolean matchesRoute(String source, String destination) {
        return this.source.equalsIgnoreCase(source.trim())
                && this.destination.equalsIgnoreCase(destination.trim());
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d | No: %s | %s -> %s | Time: %s | Seats: %d | Fare: Rs. %.2f",
                flightId, flightNumber, source, destination, departureTime, seatsAvailable, fare);
    }
}
