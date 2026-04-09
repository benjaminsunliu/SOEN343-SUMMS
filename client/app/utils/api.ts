import { getAuthToken } from "./auth";

export function apiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

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

// Parking types

export interface ParkingSearchParams {
  destination:     string;
  city:            string;
  arrivalDate:     string;
  arrivalTime:     string;
  durationHours:   number;
  vehicleType:     string;
  maxPricePerHour: number;
}

export interface ParkingFacility {
  facilityId:         number;
  name:               string;
  address:            string;
  city:               string;
  latitude?:          number;
  longitude?:         number;
  distanceKm:         number;
  pricePerHour:       number;
  estimatedTotal:     number;
  rating:             number;
  availableSpots:     number;
  totalSpots:         number;
  availabilityStatus: "AVAILABLE" | "ALMOST_FULL" | "FULL";
  covered:            boolean;
  openTwentyFourHours:boolean;
  evCharging:         boolean;
  security:           boolean;
  amenityTags:        string[];
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
  creditCardNumber?: string;
  paypalEmail?:     string;
  paypalPassword?:  string;
  includeServiceFee:boolean;
  serviceFeeAmount: number;
  includeTax:       boolean;
  taxRate:          number;
  includeInsuranceFee:boolean;
  insuranceFeeAmount:number;
  includeDiscount:  boolean;
  discountAmount:   number;
}

export interface ParkingReservationResponse {
  reservationId:   number;
  facilityName:    string;
  facilityAddress: string;
  city:            string;  
  arrivalDate:     string;
  arrivalTime:     string;
  durationHours:   number;
  totalCost:       number;
  status:          string;
  confirmedAt:     string;
}

export interface ParkingSpaceUpsertRequest {
  name: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  pricePerHour: number;
  rating: number;
  totalSpots: number;
  covered: boolean;
  openTwentyFourHours: boolean;
  evCharging: boolean;
  security: boolean;
}

export interface ParkingSummary {
  totalFacilities: number;
  totalSpots: number;
  availableSpots: number;
  reservedSpots: number;
}

export interface ParkingCatalogEntry {
  terrainCode: string;
  name: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  pricePerHour: number;
  rating: number;
  totalSpots: number;
  covered: boolean;
  openTwentyFourHours: boolean;
  evCharging: boolean;
  security: boolean;
  added: boolean;
  addedFacilityId: number | null;
}

export interface CityParkingFacilityAnalytics {
  facilityId: number;
  name: string;
  city: string;
  totalSpots: number;
  reservedSpaces: number;
  occupiedSpaces: number;
  availableSpots: number;
  totalRevenue: number;
}

export interface CityParkingActiveReservation {
  reservationId: number;
  facilityId: number;
  facilityName: string;
  city: string;
  arrivalDate: string;
  arrivalTime: string;
  durationHours: number;
  totalCost: number;
  status: string;
  confirmedAt: string;
}

export interface CityParkingAnalytics {
  totalFacilities: number;
  totalSpots: number;
  reservedSpaces: number;
  occupiedSpaces: number;
  availableSpots: number;
  totalRevenue: number;
  facilities: CityParkingFacilityAnalytics[];
  activeReservations: CityParkingActiveReservation[];
}

//Parking API calls

export async function searchParking(
  params: ParkingSearchParams
): Promise<ParkingFacility[]> {
  const query = new URLSearchParams({
    destination:     params.destination,
    city:            params.city,
    arrivalDate:     params.arrivalDate,
    arrivalTime:     params.arrivalTime,
    durationHours:   String(params.durationHours),
    vehicleType:     params.vehicleType,
    maxPricePerHour: String(params.maxPricePerHour),
  });

  const res = await apiFetch(`/api/parking/search?${query}`);

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Parking service unavailable");
  }

  return res.json();
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

export async function listParkingReservations(): Promise<ParkingReservationResponse[]> {
  const res = await apiFetch("/api/parking/reservations");

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load parking reservations");
  }

  return res.json();
}

export async function cancelParkingReservation(reservationId: number): Promise<void> {
  const res = await apiFetch(`/api/parking/reservations/${reservationId}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Could not cancel parking reservation");
  }
}

export async function occupyParkingReservation(
  reservationId: number,
): Promise<ParkingReservationResponse> {
  const res = await apiFetch(`/api/parking/reservations/${reservationId}/occupy`, {
    method: "POST",
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Could not occupy parking spot");
  }

  return res.json();
}

export async function checkoutParkingReservation(
  reservationId: number,
): Promise<ParkingReservationResponse> {
  const res = await apiFetch(`/api/parking/reservations/${reservationId}/checkout`, {
    method: "POST",
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Could not check out of parking spot");
  }

  return res.json();
}

export async function listParkingSpacesForProvider(): Promise<ParkingFacility[]> {
  const res = await apiFetch("/api/parking/management/spaces");
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load parking spaces");
  }

  return res.json();
}

export async function listParkingCatalogForProvider(): Promise<ParkingCatalogEntry[]> {
  const res = await apiFetch("/api/parking/management/catalog");
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load parking catalog");
  }

  return res.json();
}

export async function fetchCityParkingAnalytics(): Promise<CityParkingAnalytics> {
  const res = await apiFetch("/api/parking/management/analytics");
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load city parking analytics");
  }

  return res.json();
}

export async function addParkingCatalogEntryForProvider(terrainCode: string): Promise<ParkingFacility> {
  const res = await apiFetch(`/api/parking/management/catalog/${encodeURIComponent(terrainCode)}/add`, {
    method: "POST",
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to add parking space from catalog");
  }

  return res.json();
}

export async function createParkingSpaceForProvider(
  payload: ParkingSpaceUpsertRequest,
): Promise<ParkingFacility> {
  const res = await apiFetch("/api/parking/management/spaces", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to create parking space");
  }

  return res.json();
}

export async function updateParkingSpaceForProvider(
  facilityId: number,
  payload: ParkingSpaceUpsertRequest,
): Promise<ParkingFacility> {
  const res = await apiFetch(`/api/parking/management/spaces/${facilityId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to update parking space");
  }

  return res.json();
}

export async function deleteParkingSpaceForProvider(facilityId: number): Promise<void> {
  const res = await apiFetch(`/api/parking/management/spaces/${facilityId}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to delete parking space");
  }
}

export async function fetchParkingSummary(): Promise<ParkingSummary> {
  const res = await apiFetch("/api/parking/summary");
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load parking summary");
  }

  return res.json();
}

export async function listActiveParkingFacilities(): Promise<ParkingFacility[]> {
  const res = await apiFetch("/api/parking/facilities");
  if (res.ok) {
    return res.json();
  }

  // Compatibility fallback for older servers that don't yet expose /api/parking/facilities.
  if (res.status === 404 || res.status === 405) {
    const now = new Date();
    const date = now.toISOString().slice(0, 10);
    const time = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
    return searchParking({
      destination: "Montreal",
      city: "Montreal",
      arrivalDate: date,
      arrivalTime: time,
      durationHours: 1,
      vehicleType: "ANY",
      maxPricePerHour: 9999,
    });
  }

  {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Unable to load parking facilities");
  }
}

//Transit types

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

export interface TransitTopOriginMetric {
  origin: string;
  count: number;
}

export interface TransitTopDestinationMetric {
  destination: string;
  count: number;
}

export interface TransitSearchTypeMetric {
  type: string;
  count: number;
}

export interface TransitResultTypeMetric {
  type: string;
  count: number;
}

export interface TransitResultLineMetric {
  lineNumber: string;
  lineName: string;
  count: number;
}

export interface TransitAnalyticsResponse {
  totalTrips: number;
  totalSearches: number;
  topOrigins: TransitTopOriginMetric[];
  topDestinations: TransitTopDestinationMetric[];
  searchesByType: TransitSearchTypeMetric[];
  topResultTransitTypes: TransitResultTypeMetric[];
  topResultLines: TransitResultLineMetric[];
}

//Transit API calls 

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

export async function fetchTransitAnalytics(): Promise<TransitAnalyticsResponse> {
  const res = await apiFetch("/api/transit/analytics");
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message ?? "Could not load transit analytics");
  }

  return res.json();
}
