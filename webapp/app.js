// ===========================================================
// Smart Transit — frontend logic. Talks to the Java backend
// (com.sun.net.httpserver) via plain fetch() calls, no framework.
// ===========================================================

const state = {
  lastSearch: { source: "", destination: "" },
  pendingTransportId: null,
};

// ---------- Cities dropdown ----------

async function loadCities() {
  const cities = await api("/api/cities");
  const sourceSelect = document.getElementById("source");
  const destSelect = document.getElementById("destination");

  const options = `<option value="">Select a city</option>` +
    cities.map((c) => `<option value="${c}">${c}</option>`).join("");

  sourceSelect.innerHTML = options;
  destSelect.innerHTML = options;
}

document.getElementById("swap-cities").addEventListener("click", () => {
  const sourceSelect = document.getElementById("source");
  const destSelect = document.getElementById("destination");
  const temp = sourceSelect.value;
  sourceSelect.value = destSelect.value;
  destSelect.value = temp;
});

loadCities();

// ---------- Plan a Trip ----------

async function loadLocations() {
  const locations = await api("/api/locations");
  const sourceSelect = document.getElementById("plan-source");
  const destSelect = document.getElementById("plan-destination");
  const options = `<option value="">Select a place</option>` +
    locations.map((l) =>
      `<option value="${l.name}">${l.name}${l.isLocality ? " (" + l.parentCity + ")" : ", " + l.state}</option>`
    ).join("");
  sourceSelect.innerHTML = options;
  destSelect.innerHTML = options;
}

document.getElementById("swap-plan").addEventListener("click", () => {
  const s = document.getElementById("plan-source");
  const d = document.getElementById("plan-destination");
  const temp = s.value;
  s.value = d.value;
  d.value = temp;
});

loadLocations();

function modeEmoji(mode) {
  return { AUTO: "🛺", METRO: "🚇", BUS: "🚌", TRAIN: "🚆", FLIGHT: "✈️" }[mode] || "";
}

function formatMinutes(mins) {
  if (mins < 60) {
    return mins + " min";
  }
  const hours = Math.floor(mins / 60);
  const remaining = mins % 60;
  return remaining === 0 ? hours + "h" : hours + "h " + remaining + "m";
}

function tripTypeLabel(type) {
  return {
    INTRACITY: "Within the same city",
    INTERCITY_SAME_STATE: "Intercity · same state",
    INTERSTATE: "Interstate",
  }[type] || type;
}

function routeCardHtml(option, isBest) {
  if (!option) {
    return `<div class="empty-state">No option found.</div>`;
  }

  const legsHtml = option.legs
    .map((leg, idx) => {
      const chip = `
      <div class="leg-chip">
        <span class="leg-dot ${leg.mode}"></span>
        <span class="leg-label">${modeEmoji(leg.mode)} ${leg.label} &rarr; ${leg.to}</span>
        <span class="leg-meta">${formatMinutes(leg.estimatedMinutes)} &middot; &#8377;${leg.fareMin}-${leg.fareMax}</span>
        ${leg.scheduled ? "" : '<span class="leg-estimated-tag">Est.</span>'}
      </div>`;
      return idx < option.legs.length - 1 ? chip + `<span class="leg-arrow">&#8594;</span>` : chip;
    })
    .join("");

  return `
    <div class="route-card ${isBest ? "best" : ""}">
      <div class="route-legs">${legsHtml}</div>
      <div class="route-summary">
        <div class="route-time">${formatMinutes(option.totalMinutes)}<span>Total time</span></div>
        <div class="route-fare">&#8377;${option.totalFareMin} - &#8377;${option.totalFareMax}<span>Est. fare</span></div>
      </div>
      <div class="route-reason">${option.reason}${option.category === "MIXED" ? " &middot; Mixed route" : ""}</div>
    </div>`;
}

document.getElementById("plan-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const source = document.getElementById("plan-source").value;
  const destination = document.getElementById("plan-destination").value;

  const result = await api(`/api/plan?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`);
  const section = document.getElementById("plan-results");
  section.classList.remove("hidden");

  const badge = document.getElementById("trip-type-badge");
  const directCard = document.getElementById("best-direct-card");
  const mixedCard = document.getElementById("best-mixed-card");
  const allList = document.getElementById("all-options-list");

  if (!result.success) {
    badge.textContent = "";
    directCard.innerHTML = `<div class="empty-state">${result.message}</div>`;
    mixedCard.innerHTML = "";
    allList.innerHTML = "";
    return;
  }

  badge.textContent = tripTypeLabel(result.tripType);

  const directIsBest = result.bestDirect && (!result.bestMixed || result.bestDirect.score >= result.bestMixed.score);
  const mixedIsBest = result.bestMixed && (!result.bestDirect || result.bestMixed.score > result.bestDirect.score);

  directCard.innerHTML = result.bestDirect
    ? routeCardHtml(result.bestDirect, directIsBest)
    : `<div class="empty-state">No direct option found.</div>`;

  mixedCard.innerHTML = result.bestMixed
    ? routeCardHtml(result.bestMixed, mixedIsBest)
    : `<div class="empty-state">Direct option is good enough — no mix needed.</div>`;

  allList.innerHTML = result.options.map((o) => routeCardHtml(o, false)).join("");
  allList.classList.add("hidden");
  document.getElementById("toggle-all-options").innerHTML = "Show all options &#9662;";
});

document.getElementById("toggle-all-options").addEventListener("click", () => {
  const list = document.getElementById("all-options-list");
  const btn = document.getElementById("toggle-all-options");
  list.classList.toggle("hidden");
  btn.innerHTML = list.classList.contains("hidden") ? "Show all options &#9662;" : "Hide all options &#9652;";
});

// ---------- Navigation ----------

document.querySelectorAll(".nav-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".nav-btn").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".view").forEach((v) => v.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById("view-" + btn.dataset.view).classList.add("active");
    if (btn.dataset.view === "admin") loadAdminList();
  });
});

// ---------- Helpers ----------

async function api(path, options) {
  const res = await fetch(path, options);
  return res.json();
}

function transportTypeLabel(type) {
  return type.charAt(0) + type.slice(1).toLowerCase();
}

function ticketCardHtml(t, actionHtml, extraHtml) {
  const soldOut = t.availableSeats === 0;
  return `
    <div class="ticket-card">
      <div class="ticket-spine ${t.type}"></div>
      <div class="ticket-body">
        <span class="ticket-type-badge ${t.type}">${transportTypeLabel(t.type)}</span>
        <div class="ticket-name">${t.name}</div>
        <div class="ticket-route">${t.source} <span class="arrow">&#8594;</span> ${t.destination}
          &nbsp;·&nbsp; ${t.departureTime} - ${t.arrivalTime}</div>
        <div class="ticket-perforation"></div>
        <div class="ticket-footer">
          <div class="ticket-fare">&#8377;${t.fare}<span>${
            soldOut ? "Sold out" : (t.availableSeats + " seats left")
          }</span></div>
          ${actionHtml || `<button class="btn-book" data-id="${t.id}" ${soldOut ? "disabled" : ""}>Book</button>`}
        </div>
        ${extraHtml || ""}
      </div>
    </div>`;
}

// ---------- Search ----------

document.getElementById("search-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const source = document.getElementById("source").value.trim();
  const destination = document.getElementById("destination").value.trim();
  state.lastSearch = { source, destination };

  document.getElementById("alternatives-section").classList.add("hidden");

  const results = await api(`/api/search?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`);
  const grid = document.getElementById("results-grid");
  const section = document.getElementById("results-section");
  section.classList.remove("hidden");

  if (results.length === 0) {
    grid.innerHTML = `<div class="empty-state">No trains, buses or metros found for this route. Try a different source or destination.</div>`;
    return;
  }

  grid.innerHTML = results.map((t) => ticketCardHtml(t)).join("");
  grid.querySelectorAll(".btn-book").forEach((btn) => {
    btn.addEventListener("click", () => openPassengerModal(btn.dataset.id));
  });
});

// ---------- Passenger modal -> Book ----------

function openPassengerModal(transportId) {
  state.pendingTransportId = transportId;
  document.getElementById("passenger-form").reset();
  document.getElementById("passenger-modal").classList.remove("hidden");
}

document.getElementById("cancel-passenger").addEventListener("click", () => {
  document.getElementById("passenger-modal").classList.add("hidden");
});

document.getElementById("passenger-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("p-name").value.trim();
  const age = document.getElementById("p-age").value;
  const gender = document.getElementById("p-gender").value;
  const transportId = state.pendingTransportId;

  document.getElementById("passenger-modal").classList.add("hidden");

  const result = await api("/api/book", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, age, gender, transportId }),
  });

  if (result.success) {
    showTicket(result.ticket);
    document.getElementById("alternatives-section").classList.add("hidden");
  } else {
    document.getElementById("alt-fail-message").textContent = result.message;
    await loadAlternatives(transportId, name, age, gender);
  }
});

// ---------- Alternatives ----------

async function loadAlternatives(excludeId, name, age, gender) {
  const { source, destination } = state.lastSearch;
  const ranked = await api(
    `/api/alternatives?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&excludeId=${encodeURIComponent(excludeId)}`
  );

  const section = document.getElementById("alternatives-section");
  const grid = document.getElementById("alternatives-grid");
  section.classList.remove("hidden");
  section.scrollIntoView({ behavior: "smooth", block: "center" });

  if (ranked.length === 0) {
    grid.innerHTML = `<div class="empty-state">No alternatives available on this route right now.</div>`;
    return;
  }

  grid.innerHTML = ranked
    .map((opt) => {
      const actionHtml = `<button class="btn-book" data-id="${opt.transport.id}">Book this</button>`;
      const reasonHtml = `<span class="reason-tag">${opt.reason} · Match score ${opt.score}</span>`;
      return ticketCardHtml(opt.transport, actionHtml, reasonHtml);
    })
    .join("");

  grid.querySelectorAll(".btn-book").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const result = await api("/api/book", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, age, gender, transportId: btn.dataset.id }),
      });
      if (result.success) {
        showTicket(result.ticket);
        section.classList.add("hidden");
      } else {
        alert(result.message);
      }
    });
  });
}

// ---------- Ticket modal ----------

function showTicket(ticket) {
  document.getElementById("ticket-details").innerHTML = `
    <div>PNR: <b>${ticket.pnr}</b></div>
    <div>Passenger: <b>${ticket.passengerName}</b></div>
    <div>Transport: <b>${ticket.transportType} · ${ticket.transportName}</b></div>
    <div>Seat Number: <b>${ticket.seatNumber}</b></div>
    <div>Fare: <b>&#8377;${ticket.fare}</b></div>
  `;
  document.getElementById("ticket-modal").classList.remove("hidden");
}

document.getElementById("close-modal").addEventListener("click", () => {
  document.getElementById("ticket-modal").classList.add("hidden");
});

// ---------- Booking history ----------

document.getElementById("history-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("history-name").value.trim();
  const history = await api(`/api/history?name=${encodeURIComponent(name)}`);
  const grid = document.getElementById("history-grid");

  if (history.length === 0) {
    grid.innerHTML = `<div class="empty-state">No bookings found for "${name}".</div>`;
    return;
  }

  grid.innerHTML = history
    .map(
      (t) => `
    <div class="ticket-card">
      <div class="ticket-spine ${t.transportType}"></div>
      <div class="ticket-body">
        <span class="ticket-type-badge ${t.transportType}">${transportTypeLabel(t.transportType)}</span>
        <div class="ticket-name">${t.transportName}</div>
        <div class="ticket-route">PNR: ${t.pnr} &nbsp;·&nbsp; Seat ${t.seatNumber}</div>
        <div class="ticket-perforation"></div>
        <div class="ticket-footer">
          <div class="ticket-fare">&#8377;${t.fare}</div>
        </div>
      </div>
    </div>`
    )
    .join("");
});

document.getElementById("cancel-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const pnr = document.getElementById("cancel-pnr").value.trim();
  const result = await api("/api/cancel", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pnr }),
  });
  const line = document.getElementById("cancel-result");
  line.textContent = result.message;
  line.className = "result-line " + (result.success ? "ok" : "err");
});

// ---------- Admin ----------

async function loadAdminList() {
  const all = await api("/api/transports");
  const grid = document.getElementById("admin-list");
  grid.innerHTML = all.map((t) => ticketCardHtml(t, `<span></span>`)).join("");
}

document.getElementById("admin-add-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    type: document.getElementById("add-type").value,
    id: document.getElementById("add-id").value.trim(),
    name: document.getElementById("add-name").value.trim(),
    source: document.getElementById("add-source").value.trim(),
    destination: document.getElementById("add-destination").value.trim(),
    departureTime: document.getElementById("add-departure").value.trim(),
    arrivalTime: document.getElementById("add-arrival").value.trim(),
    fare: document.getElementById("add-fare").value,
    totalSeats: document.getElementById("add-seats").value,
    extra: document.getElementById("add-extra").value.trim() || "General",
  };
  const result = await api("/api/admin/add", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const line = document.getElementById("add-result");
  line.textContent = result.message;
  line.className = "result-line " + (result.success ? "ok" : "err");
  if (result.success) {
    e.target.reset();
    loadAdminList();
  }
});

document.getElementById("admin-update-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    id: document.getElementById("update-id").value.trim(),
    fare: document.getElementById("update-fare").value,
    totalSeats: document.getElementById("update-seats").value,
  };
  const result = await api("/api/admin/update", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const line = document.getElementById("update-result");
  line.textContent = result.message;
  line.className = "result-line " + (result.success ? "ok" : "err");
  if (result.success) loadAdminList();
});

document.getElementById("admin-remove-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = { id: document.getElementById("remove-id").value.trim() };
  const result = await api("/api/admin/remove", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const line = document.getElementById("remove-result");
  line.textContent = result.message;
  line.className = "result-line " + (result.success ? "ok" : "err");
  if (result.success) loadAdminList();
});