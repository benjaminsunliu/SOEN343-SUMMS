export interface ActiveTrip {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
}

const ACTIVE_TRIP_STORAGE_KEY = "summs.activeTrip";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

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
}

export function clearActiveTrip(): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(ACTIVE_TRIP_STORAGE_KEY);
}
