package airline;

import airline.exception.AirlineException;
import airline.model.Admin;
import airline.model.Booking;
import airline.model.Flight;
import airline.model.User;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AirlineSystem {
    private static final String DATA_FILE = "data/airline-data.ser";

    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<Integer, Flight> flightsById = new HashMap<>();
    private final Map<Integer, Booking> bookingsById = new HashMap<>();

    private int nextUserId = 1;
    private int nextFlightId = 1;
    private int nextBookingId = 1001;

    public static void main(String[] args) {
        AirlineSystem system = new AirlineSystem();
        system.loadData();
        system.run();
    }

    private void run() {
        System.out.println("\n====================================");
        System.out.println("      AIRLINE RESERVATION SYSTEM");
        System.out.println("====================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1:
                        registerUser();
                        break;
                    case 2:
                        loginUser();
                        break;
                    case 3:
                        loginAdmin();
                        break;
                    case 4:
                        viewAllFlights();
                        break;
                    case 5:
                        searchFlights();
                        break;
                    case 0:
                        saveData();
                        running = false;
                        System.out.println("Thank you for using the Airline Reservation System.");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (AirlineException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Something went wrong: " + ex.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n---------- Main Menu ----------");
        System.out.println("1. Register User");
        System.out.println("2. User Login");
        System.out.println("3. Admin Login");
        System.out.println("4. View Available Flights");
        System.out.println("5. Search Flights");
        System.out.println("0. Exit");
    }

    private void registerUser() throws AirlineException {
        System.out.println("\n---------- User Registration ----------");
        String name = readRequiredText("Name: ");
        String email = readEmail("Email: ");
        if (usersByEmail.containsKey(email.toLowerCase())) {
            throw new AirlineException("An account with this email already exists.");
        }
        String password = readPassword("Password: ");

        User user = new User(nextUserId++, name, email.toLowerCase(), password);
        usersByEmail.put(user.getEmail(), user);
        saveData();
        System.out.println("Registration successful. Your User ID is " + user.getUserId() + ".");
    }

    private void loginUser() throws AirlineException {
        System.out.println("\n---------- User Login ----------");
        User user = authenticateUser(false);
        System.out.println("Welcome, " + user.getName() + "!");
        userMenu(user);
    }

    private void loginAdmin() throws AirlineException {
        System.out.println("\n---------- Admin Login ----------");
        User admin = authenticateUser(true);
        System.out.println("Welcome, " + admin.getName() + " (" + admin.getRole() + ")!");
        adminMenu();
    }

    private User authenticateUser(boolean adminRequired) throws AirlineException {
        String email = readEmail("Email: ");
        String password = readPassword("Password: ");
        User user = usersByEmail.get(email.toLowerCase());

        if (user == null || !user.checkPassword(password)) {
            throw new AirlineException("Invalid email or password.");
        }
        if (adminRequired && !(user instanceof Admin)) {
            throw new AirlineException("This account does not have admin access.");
        }
        if (!adminRequired && user instanceof Admin) {
            throw new AirlineException("Please use the Admin Login option for this account.");
        }
        return user;
    }

    private void userMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n---------- User Menu ----------");
            System.out.println("1. View Available Flights");
            System.out.println("2. Search Flights");
            System.out.println("3. Book Ticket");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. View My Bookings");
            System.out.println("0. Logout");

            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1:
                        viewAllFlights();
                        break;
                    case 2:
                        searchFlights();
                        break;
                    case 3:
                        bookTicket(user);
                        break;
                    case 4:
                        cancelTicket(user);
                        break;
                    case 5:
                        viewUserBookings(user);
                        break;
                    case 0:
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (AirlineException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void adminMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n---------- Admin Menu ----------");
            System.out.println("1. Add Flight");
            System.out.println("2. View All Flights");
            System.out.println("3. Delete Flight");
            System.out.println("4. View All Bookings");
            System.out.println("0. Logout");

            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1:
                        addFlight();
                        break;
                    case 2:
                        viewAllFlights();
                        break;
                    case 3:
                        deleteFlight();
                        break;
                    case 4:
                        viewAllBookings();
                        break;
                    case 0:
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (AirlineException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void addFlight() throws AirlineException {
        System.out.println("\n---------- Add Flight ----------");
        String flightNumber = readRequiredText("Flight Number: ");
        ensureUniqueFlightNumber(flightNumber);
        String source = readRequiredText("Source: ");
        String destination = readRequiredText("Destination: ");
        if (source.equalsIgnoreCase(destination)) {
            throw new AirlineException("Source and destination cannot be the same.");
        }
        String time = readRequiredText("Departure Time: ");
        int seats = readPositiveInt("Seats Available: ");
        double fare = readPositiveDouble("Fare: ");

        Flight flight = new Flight(nextFlightId++, flightNumber, source, destination, time, seats, fare);
        flightsById.put(flight.getFlightId(), flight);
        saveData();
        System.out.println("Flight added successfully with ID " + flight.getFlightId() + ".");
    }

    private void ensureUniqueFlightNumber(String flightNumber) throws AirlineException {
        for (Flight flight : flightsById.values()) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber.trim())) {
                throw new AirlineException("A flight with this number already exists.");
            }
        }
    }

    private void viewAllFlights() {
        System.out.println("\n---------- Flights ----------");
        if (flightsById.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }
        flightsById.values().stream()
                .sorted((first, second) -> Integer.compare(first.getFlightId(), second.getFlightId()))
                .forEach(System.out::println);
    }

    private List<Flight> searchFlights() {
        System.out.println("\n---------- Search Flights ----------");
        String source = readRequiredText("Source: ");
        String destination = readRequiredText("Destination: ");
        List<Flight> matches = new ArrayList<>();

        for (Flight flight : flightsById.values()) {
            if (flight.matchesRoute(source, destination)) {
                matches.add(flight);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No flights found for this route.");
        } else {
            System.out.println("Matching flights:");
            matches.forEach(System.out::println);
        }
        return matches;
    }

    private void deleteFlight() throws AirlineException {
        viewAllFlights();
        int flightId = readPositiveInt("Enter Flight ID to delete: ");
        Flight removedFlight = flightsById.remove(flightId);
        if (removedFlight == null) {
            throw new AirlineException("Flight not found.");
        }

        for (Booking booking : bookingsById.values()) {
            if (booking.getFlightId() == flightId && !booking.isCancelled()) {
                booking.cancel();
            }
        }
        saveData();
        System.out.println("Flight deleted. Related active bookings were marked as cancelled.");
    }

    private void bookTicket(User user) throws AirlineException {
        viewAllFlights();
        int flightId = readPositiveInt("Enter Flight ID to book: ");
        Flight flight = flightsById.get(flightId);
        if (flight == null) {
            throw new AirlineException("Flight not found.");
        }

        int seats = readPositiveInt("Number of seats: ");
        if (!flight.hasSeats(seats)) {
            throw new AirlineException("Not enough seats available.");
        }

        flight.bookSeats(seats);
        double totalFare = seats * flight.getFare();
        Booking booking = new Booking(nextBookingId++, user.getUserId(), flightId, seats, totalFare);
        bookingsById.put(booking.getBookingId(), booking);
        saveData();

        System.out.println("Ticket booked successfully.");
        printBookingDetails(booking);
    }

    private void cancelTicket(User user) throws AirlineException {
        viewUserBookings(user);
        int bookingId = readPositiveInt("Enter Booking ID to cancel: ");
        Booking booking = bookingsById.get(bookingId);
        if (booking == null || booking.getUserId() != user.getUserId()) {
            throw new AirlineException("Booking not found for your account.");
        }
        if (booking.isCancelled()) {
            throw new AirlineException("This booking is already cancelled.");
        }

        booking.cancel();
        Flight flight = flightsById.get(booking.getFlightId());
        if (flight != null) {
            flight.cancelSeats(booking.getSeatsBooked());
        }
        saveData();
        System.out.println("Booking cancelled successfully.");
    }

    private void viewUserBookings(User user) {
        System.out.println("\n---------- My Bookings ----------");
        boolean found = false;
        for (Booking booking : bookingsById.values()) {
            if (booking.getUserId() == user.getUserId()) {
                printBookingDetails(booking);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No bookings found.");
        }
    }

    private void viewAllBookings() {
        System.out.println("\n---------- All Bookings ----------");
        if (bookingsById.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        bookingsById.values().stream()
                .sorted((first, second) -> Integer.compare(first.getBookingId(), second.getBookingId()))
                .forEach(this::printBookingDetails);
    }

    private void printBookingDetails(Booking booking) {
        Flight flight = flightsById.get(booking.getFlightId());
        String flightInfo = flight == null ? "Flight deleted" : flight.toString();
        System.out.println(booking);
        System.out.println("Flight: " + flightInfo);
        System.out.println("--------------------------------");
    }

    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private String readEmail(String prompt) {
        while (true) {
            String email = readRequiredText(prompt).toLowerCase();
            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return email;
            }
            System.out.println("Please enter a valid email address.");
        }
    }

    private String readPassword(String prompt) {
        while (true) {
            String password = readRequiredText(prompt);
            if (password.length() >= 4) {
                return password;
            }
            System.out.println("Password must contain at least 4 characters.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than zero.");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            seedSampleData();
            saveData();
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            AirlineData data = (AirlineData) input.readObject();
            usersByEmail.clear();
            usersByEmail.putAll(data.usersByEmail);
            flightsById.clear();
            flightsById.putAll(data.flightsById);
            bookingsById.clear();
            bookingsById.putAll(data.bookingsById);
            nextUserId = data.nextUserId;
            nextFlightId = data.nextFlightId;
            nextBookingId = data.nextBookingId;
        } catch (EOFException ex) {
            seedSampleData();
            saveData();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Could not load saved data. Starting with sample data.");
            seedSampleData();
            saveData();
        }
    }

    private void saveData() {
        File file = new File(DATA_FILE);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("Warning: Could not create data directory.");
            return;
        }

        AirlineData data = new AirlineData(usersByEmail, flightsById, bookingsById,
                nextUserId, nextFlightId, nextBookingId);
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(data);
        } catch (IOException ex) {
            System.out.println("Warning: Could not save data: " + ex.getMessage());
        }
    }

    private void seedSampleData() {
        usersByEmail.clear();
        flightsById.clear();
        bookingsById.clear();

        Admin admin = new Admin(nextUserId++, "System Admin", "admin@airline.com", "admin123");
        User user = new User(nextUserId++, "Sample User", "user@airline.com", "user123");
        usersByEmail.put(admin.getEmail(), admin);
        usersByEmail.put(user.getEmail(), user);

        Flight flightOne = new Flight(nextFlightId++, "AI101", "Delhi", "Mumbai", "09:30", 50, 4500.00);
        Flight flightTwo = new Flight(nextFlightId++, "AI202", "Mumbai", "Bengaluru", "14:15", 40, 5200.00);
        Flight flightThree = new Flight(nextFlightId++, "AI303", "Delhi", "Chennai", "19:45", 35, 6100.00);
        flightsById.put(flightOne.getFlightId(), flightOne);
        flightsById.put(flightTwo.getFlightId(), flightTwo);
        flightsById.put(flightThree.getFlightId(), flightThree);
    }

    private static class AirlineData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<String, User> usersByEmail;
        private final Map<Integer, Flight> flightsById;
        private final Map<Integer, Booking> bookingsById;
        private final int nextUserId;
        private final int nextFlightId;
        private final int nextBookingId;

        private AirlineData(Map<String, User> usersByEmail, Map<Integer, Flight> flightsById,
                            Map<Integer, Booking> bookingsById, int nextUserId,
                            int nextFlightId, int nextBookingId) {
            this.usersByEmail = new HashMap<>(usersByEmail);
            this.flightsById = new HashMap<>(flightsById);
            this.bookingsById = new HashMap<>(bookingsById);
            this.nextUserId = nextUserId;
            this.nextFlightId = nextFlightId;
            this.nextBookingId = nextBookingId;
        }
    }
}
