const STORAGE_KEY = "airline-web-data-v1";

let state = loadState();
let currentUser = null;
let filteredFlights = null;

const elements = {
  sessionText: document.getElementById("sessionText"),
  logoutBtn: document.getElementById("logoutBtn"),
  navButtons: document.querySelectorAll(".nav-btn"),
  userOnly: document.querySelectorAll(".user-only"),
  adminOnly: document.querySelectorAll(".admin-only"),
  loginForm: document.getElementById("loginForm"),
  registerForm: document.getElementById("registerForm"),
  searchForm: document.getElementById("searchForm"),
  clearSearchBtn: document.getElementById("clearSearchBtn"),
  addFlightForm: document.getElementById("addFlightForm"),
  flightsList: document.getElementById("flightsList"),
  myBookingsList: document.getElementById("myBookingsList"),
  adminFlightsList: document.getElementById("adminFlightsList"),
  allBookingsList: document.getElementById("allBookingsList"),
  toast: document.getElementById("toast")
};

document.addEventListener("DOMContentLoaded", () => {
  bindEvents();
  renderAll();
});

function bindEvents() {
  elements.navButtons.forEach((button) => {
    button.addEventListener("click", () => showView(button.dataset.view));
  });

  elements.logoutBtn.addEventListener("click", () => {
    currentUser = null;
    filteredFlights = null;
    showToast("Logged out successfully.");
    showView("home");
    renderAll();
  });

  elements.loginForm.addEventListener("submit", (event) => {
    event.preventDefault();
    const email = valueOf("loginEmail").toLowerCase();
    const password = valueOf("loginPassword");
    const user = state.users.find((item) => item.email === email && item.password === password);

    if (!user) {
      showToast("Invalid email or password.");
      return;
    }

    currentUser = user;
    event.target.reset();
    showToast(`Welcome, ${user.name}.`);
    showView(user.role === "admin" ? "admin" : "flights");
    renderAll();
  });

  elements.registerForm.addEventListener("submit", (event) => {
    event.preventDefault();
    const name = valueOf("registerName");
    const email = valueOf("registerEmail").toLowerCase();
    const password = valueOf("registerPassword");

    if (state.users.some((user) => user.email === email)) {
      showToast("An account with this email already exists.");
      return;
    }

    const user = {
      id: state.nextUserId++,
      name,
      email,
      password,
      role: "user"
    };
    state.users.push(user);
    currentUser = user;
    saveState();
    event.target.reset();
    showToast("Registration successful.");
    showView("flights");
    renderAll();
  });

  elements.searchForm.addEventListener("submit", (event) => {
    event.preventDefault();
    const source = valueOf("searchSource").toLowerCase();
    const destination = valueOf("searchDestination").toLowerCase();

    filteredFlights = state.flights.filter((flight) => {
      const sourceMatches = !source || flight.source.toLowerCase().includes(source);
      const destinationMatches = !destination || flight.destination.toLowerCase().includes(destination);
      return sourceMatches && destinationMatches;
    });
    renderFlights();
  });

  elements.clearSearchBtn.addEventListener("click", () => {
    filteredFlights = null;
    elements.searchForm.reset();
    renderFlights();
  });

  elements.addFlightForm.addEventListener("submit", (event) => {
    event.preventDefault();
    if (!isAdmin()) {
      showToast("Admin access required.");
      return;
    }

    const flightNumber = valueOf("flightNumber").toUpperCase();
    const source = valueOf("flightSource");
    const destination = valueOf("flightDestination");
    const departureTime = valueOf("flightTime");
    const seatsAvailable = Number(valueOf("flightSeats"));
    const fare = Number(valueOf("flightFare"));

    if (source.toLowerCase() === destination.toLowerCase()) {
      showToast("Source and destination cannot be the same.");
      return;
    }
    if (state.flights.some((flight) => flight.flightNumber.toLowerCase() === flightNumber.toLowerCase())) {
      showToast("Flight number already exists.");
      return;
    }

    state.flights.push({
      id: state.nextFlightId++,
      flightNumber,
      source,
      destination,
      departureTime,
      seatsAvailable,
      fare
    });
    saveState();
    event.target.reset();
    showToast("Flight added successfully.");
    renderAll();
  });
}

function renderAll() {
  renderSession();
  renderFlights();
  renderMyBookings();
  renderAdmin();
}

function renderSession() {
  elements.sessionText.textContent = currentUser
    ? `${currentUser.name} (${currentUser.role})`
    : "Not signed in";
  elements.logoutBtn.classList.toggle("hidden", !currentUser);
  elements.userOnly.forEach((item) => item.classList.toggle("hidden", !currentUser || currentUser.role !== "user"));
  elements.adminOnly.forEach((item) => item.classList.toggle("hidden", !currentUser || currentUser.role !== "admin"));
}

function renderFlights() {
  const flights = filteredFlights || state.flights;
  if (flights.length === 0) {
    elements.flightsList.innerHTML = `<div class="empty">No flights found.</div>`;
    return;
  }

  elements.flightsList.innerHTML = flights
    .map((flight) => `
      <article class="flight-card">
        <div>
          <p class="eyebrow">${escapeHtml(flight.flightNumber)}</p>
          <h3>${escapeHtml(flight.source)} to ${escapeHtml(flight.destination)}</h3>
        </div>
        <div class="meta">
          <span>Departure: ${escapeHtml(flight.departureTime)}</span>
          <span>Seats available: ${flight.seatsAvailable}</span>
          <span>Fare: Rs. ${money(flight.fare)}</span>
        </div>
        ${bookingControls(flight)}
      </article>
    `)
    .join("");

  document.querySelectorAll("[data-book-flight]").forEach((button) => {
    button.addEventListener("click", () => bookFlight(Number(button.dataset.bookFlight)));
  });
}

function bookingControls(flight) {
  if (!currentUser) {
    return `<button type="button" disabled>Login to Book</button>`;
  }
  if (currentUser.role !== "user") {
    return `<button class="secondary" type="button" disabled>Passenger Only</button>`;
  }
  if (flight.seatsAvailable <= 0) {
    return `<button class="secondary" type="button" disabled>Sold Out</button>`;
  }
  return `
    <div class="book-row">
      <input id="seats-${flight.id}" type="number" min="1" max="${flight.seatsAvailable}" value="1" aria-label="Seats">
      <button type="button" data-book-flight="${flight.id}">Book</button>
    </div>
  `;
}

function bookFlight(flightId) {
  if (!currentUser || currentUser.role !== "user") {
    showToast("Please login as a passenger to book.");
    return;
  }

  const flight = state.flights.find((item) => item.id === flightId);
  const seatsInput = document.getElementById(`seats-${flightId}`);
  const seats = Number(seatsInput.value);

  if (!flight || seats < 1 || seats > flight.seatsAvailable) {
    showToast("Please enter a valid seat count.");
    return;
  }

  flight.seatsAvailable -= seats;
  state.bookings.push({
    id: state.nextBookingId++,
    userId: currentUser.id,
    flightId: flight.id,
    seatsBooked: seats,
    totalFare: seats * flight.fare,
    bookingTime: new Date().toISOString(),
    cancelled: false
  });
  saveState();
  showToast("Ticket booked successfully.");
  renderAll();
}

function renderMyBookings() {
  if (!currentUser || currentUser.role !== "user") {
    elements.myBookingsList.innerHTML = `<div class="empty">Login as a passenger to view bookings.</div>`;
    return;
  }

  const bookings = state.bookings.filter((booking) => booking.userId === currentUser.id);
  elements.myBookingsList.innerHTML = renderBookingsTable(bookings, true);

  document.querySelectorAll("[data-cancel-booking]").forEach((button) => {
    button.addEventListener("click", () => cancelBooking(Number(button.dataset.cancelBooking)));
  });
}

function cancelBooking(bookingId) {
  const booking = state.bookings.find((item) => item.id === bookingId);
  if (!booking || !currentUser || booking.userId !== currentUser.id || booking.cancelled) {
    showToast("Booking cannot be cancelled.");
    return;
  }

  booking.cancelled = true;
  const flight = state.flights.find((item) => item.id === booking.flightId);
  if (flight) {
    flight.seatsAvailable += booking.seatsBooked;
  }
  saveState();
  showToast("Booking cancelled successfully.");
  renderAll();
}

function renderAdmin() {
  if (!currentUser || currentUser.role !== "admin") {
    elements.adminFlightsList.innerHTML = `<div class="empty">Login as admin to manage flights.</div>`;
    elements.allBookingsList.innerHTML = `<div class="empty">Login as admin to view bookings.</div>`;
    return;
  }

  elements.adminFlightsList.innerHTML = renderFlightsTable();
  elements.allBookingsList.innerHTML = renderBookingsTable(state.bookings, false);

  document.querySelectorAll("[data-delete-flight]").forEach((button) => {
    button.addEventListener("click", () => deleteFlight(Number(button.dataset.deleteFlight)));
  });
}

function renderFlightsTable() {
  if (state.flights.length === 0) {
    return `<div class="empty">No flights available.</div>`;
  }

  return `
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>No.</th>
          <th>Route</th>
          <th>Time</th>
          <th>Seats</th>
          <th>Fare</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        ${state.flights.map((flight) => `
          <tr>
            <td>${flight.id}</td>
            <td>${escapeHtml(flight.flightNumber)}</td>
            <td>${escapeHtml(flight.source)} to ${escapeHtml(flight.destination)}</td>
            <td>${escapeHtml(flight.departureTime)}</td>
            <td>${flight.seatsAvailable}</td>
            <td>Rs. ${money(flight.fare)}</td>
            <td><button class="danger" type="button" data-delete-flight="${flight.id}">Delete</button></td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;
}

function deleteFlight(flightId) {
  if (!isAdmin()) {
    showToast("Admin access required.");
    return;
  }
  state.flights = state.flights.filter((flight) => flight.id !== flightId);
  state.bookings.forEach((booking) => {
    if (booking.flightId === flightId) {
      booking.cancelled = true;
    }
  });
  saveState();
  showToast("Flight deleted. Related bookings were cancelled.");
  renderAll();
}

function renderBookingsTable(bookings, includeCancelAction) {
  if (bookings.length === 0) {
    return `<div class="empty">No bookings found.</div>`;
  }

  return `
    <table>
      <thead>
        <tr>
          <th>Booking</th>
          <th>Passenger</th>
          <th>Flight</th>
          <th>Seats</th>
          <th>Total</th>
          <th>Status</th>
          ${includeCancelAction ? "<th>Action</th>" : ""}
        </tr>
      </thead>
      <tbody>
        ${bookings.map((booking) => {
          const user = state.users.find((item) => item.id === booking.userId);
          const flight = state.flights.find((item) => item.id === booking.flightId);
          const statusClass = booking.cancelled ? "status cancelled" : "status";
          return `
            <tr>
              <td>${booking.id}<br><small>${formatDate(booking.bookingTime)}</small></td>
              <td>${escapeHtml(user ? user.name : "Unknown")}</td>
              <td>${flight ? escapeHtml(`${flight.flightNumber}: ${flight.source} to ${flight.destination}`) : "Deleted flight"}</td>
              <td>${booking.seatsBooked}</td>
              <td>Rs. ${money(booking.totalFare)}</td>
              <td><span class="${statusClass}">${booking.cancelled ? "Cancelled" : "Confirmed"}</span></td>
              ${includeCancelAction ? `
                <td>
                  <button class="danger" type="button" data-cancel-booking="${booking.id}" ${booking.cancelled ? "disabled" : ""}>
                    Cancel
                  </button>
                </td>
              ` : ""}
            </tr>
          `;
        }).join("")}
      </tbody>
    </table>
  `;
}

function showView(viewName) {
  document.querySelectorAll(".view").forEach((view) => view.classList.remove("active"));
  document.getElementById(`${viewName}View`).classList.add("active");

  elements.navButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.view === viewName);
  });
}

function isAdmin() {
  return currentUser && currentUser.role === "admin";
}

function loadState() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    try {
      return JSON.parse(saved);
    } catch (error) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  return {
    nextUserId: 3,
    nextFlightId: 4,
    nextBookingId: 1001,
    users: [
      { id: 1, name: "System Admin", email: "admin@airline.com", password: "admin123", role: "admin" },
      { id: 2, name: "Sample User", email: "user@airline.com", password: "user123", role: "user" }
    ],
    flights: [
      { id: 1, flightNumber: "AI101", source: "Delhi", destination: "Mumbai", departureTime: "09:30", seatsAvailable: 50, fare: 4500 },
      { id: 2, flightNumber: "AI202", source: "Mumbai", destination: "Bengaluru", departureTime: "14:15", seatsAvailable: 40, fare: 5200 },
      { id: 3, flightNumber: "AI303", source: "Delhi", destination: "Chennai", departureTime: "19:45", seatsAvailable: 35, fare: 6100 }
    ],
    bookings: []
  };
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function valueOf(id) {
  return document.getElementById(id).value.trim();
}

function money(value) {
  return Number(value).toFixed(2);
}

function formatDate(value) {
  return new Date(value).toLocaleString();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.remove("hidden");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    elements.toast.classList.add("hidden");
  }, 2600);
}
