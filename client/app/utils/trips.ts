export interface ActiveTrip {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
}

export interface ActiveReservation {
  reservationId: number;
  vehicleId: number;
  vehicleName: string;
  pricePerMinute: number;
  startDate: string;
  endDate: string;
}

const ACTIVE_TRIP_STORAGE_KEY = "summs.activeTrip";
const ACTIVE_RESERVATION_STORAGE_KEY = "summs.activeReservation";
const ACTIVE_TRIP_CHANGED_EVENT = "summs:activeTripChanged";
const ACTIVE_RESERVATION_CHANGED_EVENT = "summs:activeReservationChanged";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

// ============ ACTIVE RESERVATION FUNCTIONS ============

export function getActiveReservation(): ActiveReservation | null {
  if (!isBrowser()) {
    return null;
  }

  const raw = window.localStorage.getItem(ACTIVE_RESERVATION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<ActiveReservation>;
    if (
      typeof parsed.reservationId === "number" &&
      typeof parsed.vehicleId === "number" &&
      typeof parsed.vehicleName === "string" &&
      typeof parsed.pricePerMinute === "number" &&
      typeof parsed.startDate === "string" &&
      typeof parsed.endDate === "string"
    ) {
      return parsed as ActiveReservation;
    }
  } catch {
    // Ignore malformed payload
  }

  window.localStorage.removeItem(ACTIVE_RESERVATION_STORAGE_KEY);
  return null;
}

export function setActiveReservation(reservation: ActiveReservation): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(ACTIVE_RESERVATION_STORAGE_KEY, JSON.stringify(reservation));
  window.dispatchEvent(new CustomEvent(ACTIVE_RESERVATION_CHANGED_EVENT, { detail: { activeReservation: reservation } }));
}

export function clearActiveReservation(): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(ACTIVE_RESERVATION_STORAGE_KEY);
  window.dispatchEvent(new CustomEvent(ACTIVE_RESERVATION_CHANGED_EVENT, { detail: { activeReservation: null } }));
}

export function onActiveReservationChanged(callback: (activeReservation: ActiveReservation | null) => void): () => void {
  if (!isBrowser()) {
    return () => {};
  }

  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<{ activeReservation: ActiveReservation | null }>;
    callback(customEvent.detail.activeReservation);
  };

  window.addEventListener(ACTIVE_RESERVATION_CHANGED_EVENT, handler);
  return () => window.removeEventListener(ACTIVE_RESERVATION_CHANGED_EVENT, handler);
}

// ============ ACTIVE TRIP FUNCTIONS ============

export function getActiveTrip(): ActiveTrip | null {
  if (!isBrowser()) {
    return null;
  }

  const raw = window.localStorage.getItem(ACTIVE_TRIP_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<ActiveTrip>;
    if (
      typeof parsed.tripId === "number" &&
      typeof parsed.vehicleId === "number" &&
      typeof parsed.citizenId === "number" &&
      typeof parsed.startTime === "string"
    ) {
      return parsed as ActiveTrip;
    }
  } catch {
    // Ignore malformed payload.
  }

  window.localStorage.removeItem(ACTIVE_TRIP_STORAGE_KEY);
  return null;
}

export function setActiveTrip(activeTrip: ActiveTrip): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(ACTIVE_TRIP_STORAGE_KEY, JSON.stringify(activeTrip));
  // When trip starts, clear the reservation
  clearActiveReservation();
  // Dispatch custom event to notify listeners (e.g., SiteNav)
  window.dispatchEvent(new CustomEvent(ACTIVE_TRIP_CHANGED_EVENT, { detail: { activeTrip } }));
}

export function clearActiveTrip(): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(ACTIVE_TRIP_STORAGE_KEY);
  // Dispatch custom event to notify listeners (e.g., SiteNav)
  window.dispatchEvent(new CustomEvent(ACTIVE_TRIP_CHANGED_EVENT, { detail: { activeTrip: null } }));
}

// Listener function for components that need to react to active trip changes
export function onActiveTripChanged(callback: (activeTrip: ActiveTrip | null) => void): () => void {
  if (!isBrowser()) {
    return () => {};
  }

  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<{ activeTrip: ActiveTrip | null }>;
    callback(customEvent.detail.activeTrip);
  };

  window.addEventListener(ACTIVE_TRIP_CHANGED_EVENT, handler);
  
  // Return cleanup function
  return () => window.removeEventListener(ACTIVE_TRIP_CHANGED_EVENT, handler);
}
