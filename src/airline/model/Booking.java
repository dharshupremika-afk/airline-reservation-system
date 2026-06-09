package airline.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int bookingId;
    private final int userId;
    private final int flightId;
    private final int seatsBooked;
    private final double totalFare;
    private final LocalDateTime bookingTime;
    private boolean cancelled;

    public Booking(int bookingId, int userId, int flightId, int seatsBooked, double totalFare) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.seatsBooked = seatsBooked;
        this.totalFare = totalFare;
        this.bookingTime = LocalDateTime.now();
        this.cancelled = false;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public int getFlightId() {
        return flightId;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public String getFormattedTime() {
        return bookingTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return String.format(
                "Booking ID: %d | User ID: %d | Flight ID: %d | Seats: %d | Total: Rs. %.2f | Date: %s | Status: %s",
                bookingId, userId, flightId, seatsBooked, totalFare, getFormattedTime(),
                cancelled ? "Cancelled" : "Confirmed");
    }
}
