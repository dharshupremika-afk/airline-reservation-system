# Airline Reservation System

A Core Java, menu-driven console application that demonstrates OOP concepts with users, admins, flights, and bookings.

## Project Structure

```text
src/
  airline/
    AirlineSystem.java
    exception/
      AirlineException.java
    model/
      Admin.java
      Booking.java
      Flight.java
      User.java
```

## Compile

```powershell
javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
```

## Run

```powershell
java -cp out airline.AirlineSystem
```

## Run Web Page Version

Open this file in your browser:

```text
web/index.html
```

The web version uses browser `localStorage`, so registrations, flights, and bookings stay saved in the same browser.

## Sample Test Data

The first run creates `data/airline-data.ser` and loads this sample data:

- Admin: `admin@airline.com` / `admin123`
- User: `user@airline.com` / `user123`
- Flights:
  - `AI101`: Delhi to Mumbai, 09:30, 50 seats, Rs. 4500
  - `AI202`: Mumbai to Bengaluru, 14:15, 40 seats, Rs. 5200
  - `AI303`: Delhi to Chennai, 19:45, 35 seats, Rs. 6100

## Notes

- Data is stored using `ArrayList`-style collections and `HashMap` collections in memory.
- File I/O is implemented with Java serialization.
- Delete the `data/airline-data.ser` file if you want to reset to the original sample data.
