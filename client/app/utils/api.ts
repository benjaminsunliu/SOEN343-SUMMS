import { getAuthToken } from "./auth";

export function apiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  // Use explicit API origin when provided, otherwise rely on relative paths.
  const env = import.meta.env as Record<string, string | boolean | undefined>;
  const apiOrigin =
    typeof env.VITE_API_ORIGIN === "string" ? env.VITE_API_ORIGIN : undefined;
  if (apiOrigin && apiOrigin.length > 0) {
    return `${apiOrigin.replace(/\/$/, "")}${normalizedPath}`;
  }

  return normalizedPath;
}

export async function apiFetch(
  url: string,
  options: RequestInit = {},
): Promise<Response> {
  const headers = new Headers(authHeaders(options.headers));

  // Default to JSON for API payloads while preserving caller overrides.
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(apiUrl(url), {
    ...options,
    headers,
  });
}

export function authHeaders(extraHeaders?: HeadersInit): HeadersInit {
  const token = getAuthToken();
  const headers = new Headers(extraHeaders);

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  return headers;
}

export interface ParkingSearchParams {
  destination: string;
  city: string;
  arrivalDate: string;
  arrivalTime: string;
  durationHours: number;
  vehicleType: string;
  maxPricePerHour: number;
}

export interface ParkingFacility {
  facilityId: number;
  name: string;
  address: string;
  city: string;
  distanceKm: number;
  pricePerHour: number;
  estimatedTotal: number;
  rating: number;
  availableSpots: number;
  totalSpots: number;
  availabilityStatus: "AVAILABLE" | "ALMOST_FULL" | "FULL";
  covered: boolean;
  openTwentyFourHours: boolean;
  evCharging: boolean;
  security: boolean;
  amenityTags: string[];
}

//Parking API Calls

const API_BASE = "http://localhost:8080/api";

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem("token");
  return token
    ? { "Content-Type": "application/json", Authorization: `Bearer ${token}` }
    : { "Content-Type": "application/json" };
}

export async function searchParking(
  params: ParkingSearchParams
): Promise<ParkingFacility[]> {
  const query = new URLSearchParams({
    destination:      params.destination,
    city:             params.city,
    arrivalDate:      params.arrivalDate,
    arrivalTime:      params.arrivalTime,
    durationHours:    String(params.durationHours),
    vehicleType:      params.vehicleType,
    maxPricePerHour:  String(params.maxPricePerHour),
  });

  const res = await apiFetch(`/api/parking/search?${query}`);

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Parking service unavailable");
  }

  return res.json();
}

export interface CreateParkingReservationRequest {
  facilityId:      number;
  facilityName:    string;
  facilityAddress: string;
  city:            string;
  arrivalDate:     string;
  arrivalTime:     string;
  durationHours:   number;
  totalCost:       number;
  paymentMethod:   string;
}

export interface ParkingReservationResponse {
  reservationId:  number;
  facilityName:   string;
  facilityAddress:string;
  arrivalDate:    string;
  arrivalTime:    string;
  durationHours:  number;
  totalCost:      number;
  status:         string;
  confirmedAt:    string;
}

export async function createParkingReservation(
  data: CreateParkingReservationRequest
): Promise<ParkingReservationResponse> {
  const res = await apiFetch("/api/parking/reservations", {
    method: "POST",
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Failed to create reservation");
  }

  return res.json();
}

export interface TransitSearchParams {
  origin:      string;
  destination: string;
  date:        string;
  time:        string;
  type:        string; // "ALL" | "METRO" | "BUS" | "TRAIN" | "REM"
}

export interface TransitRoute {
  routeId:         number;
  lineNumber:      string;
  lineName:        string;
  type:            string;
  origin:          string;
  destination:     string;
  departureTime:   string;
  arrivalTime:     string;
  durationMinutes: number;
  transfers:       number;
  fare:            number;
  status:          "ON_TIME" | "DELAYED" | "DISRUPTED";
  statusMessage:   string;
  stops:           string[];
  lineColor:       string;
}

export interface TransitLineStatus {
  lineId:        string;
  lineNumber:    string;
  lineName:      string;
  type:          string;
  status:        "ON_TIME" | "DELAYED" | "DISRUPTED";
  statusMessage: string;
  lineColor:     string;
}

export async function searchTransitRoutes(
  params: TransitSearchParams
): Promise<TransitRoute[]> {
  const query = new URLSearchParams({
    origin:      params.origin,
    destination: params.destination,
    date:        params.date,
    time:        params.time,
    type:        params.type,
  });

  const res = await apiFetch(`/api/transit/routes?${query}`);
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Transit service unavailable");
  }
  return res.json();
}

export async function fetchTransitLineStatuses(): Promise<TransitLineStatus[]> {
  const res = await apiFetch("/api/transit/status");
  if (!res.ok) throw new Error("Could not load line statuses");
  return res.json();
}