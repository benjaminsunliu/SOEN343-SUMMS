export interface VehicleApiResponse {
  id: number;
  type: string;
  status: string;
  location: {
    latitude: number | null;
    longitude: number | null;
  } | null;
  locationAddress: string | null;
  locationCity: string | null;
  providerId: number | null;
  costPerMinute: number | null;
  maxRange: number | null;
  licensePlate: string | null;
  seatingCapacity: number | null;
}

export interface VehicleCatalogItem {
  id: number;
  type: "Bicycle" | "Scooter" | "Car";
  rawType: string;
  name: string;
  provider: string;
  condition: string;
  energy: string;
  pricePerMinute: number;
  priceLabel: string;
  available: boolean;
  station: string;
  locationCity: string | null;
  distance: string;
  latitude: number | null;
  longitude: number | null;
}

export const DEFAULT_MAP_CENTER: [number, number] = [45.5019, -73.5674];

export function mapVehiclesToCatalog(
  vehicles: VehicleApiResponse[],
): VehicleCatalogItem[] {
  return vehicles.map(mapVehicleToCatalog);
}

export function mapVehicleToCatalog(
  vehicle: VehicleApiResponse,
): VehicleCatalogItem {
  const type = normalizeType(vehicle.type);
  const provider = formatProvider(vehicle.providerId);
  const [latitude, longitude] = resolveCoordinates(vehicle.location);
  const pricePerMinute = resolvePricePerMinute(vehicle.costPerMinute);
  const hasCoordinates = latitude !== null && longitude !== null;
  const locationAddress = normalizeOptionalText(vehicle.locationAddress);

  return {
    id: vehicle.id,
    type,
    rawType: vehicle.type,
    name: formatName(vehicle, type),
    provider,
    condition: formatCondition(vehicle.status),
    energy: formatEnergy(vehicle, type),
    pricePerMinute,
    priceLabel: `$${pricePerMinute.toFixed(2)}/min`,
    available: vehicle.status.toUpperCase() === "AVAILABLE",
    station: locationAddress ?? (hasCoordinates
      ? `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`
      : "Location unavailable"),
    locationCity: normalizeOptionalText(vehicle.locationCity),
    distance: "Distance unavailable",
    latitude,
    longitude,
  };
}

function normalizeType(type: string): VehicleCatalogItem["type"] {
  const normalized = type.toUpperCase();
  if (normalized === "BICYCLE") {
    return "Bicycle";
  }
  if (normalized === "SCOOTER") {
    return "Scooter";
  }
  return "Car";
}

function formatProvider(providerId: number | null): string {
  if (providerId !== null) {
    return `Provider #${providerId}`;
  }

  return "Unknown Provider";
}

function formatName(
  vehicle: VehicleApiResponse,
  type: VehicleCatalogItem["type"],
): string {
  if (type === "Bicycle") {
    return `Bicycle #${vehicle.id}`;
  }
  if (type === "Scooter") {
    return `Scooter #${vehicle.id}`;
  }
  if (vehicle.licensePlate && vehicle.licensePlate.trim().length > 0) {
    return `Car ${vehicle.licensePlate}`;
  }
  return `Car #${vehicle.id}`;
}

function formatCondition(status: string): string {
  const normalized = status.toUpperCase();
  if (normalized === "AVAILABLE") {
    return "Excellent";
  }
  if (normalized === "RESERVED") {
    return "Reserved";
  }
  if (normalized === "IN_USE") {
    return "In Use";
  }
  return "Good";
}

function formatEnergy(
  vehicle: VehicleApiResponse,
  type: VehicleCatalogItem["type"],
): string {
  if (type === "Bicycle") {
    return "Human-powered";
  }
  if (type === "Scooter") {
    if (typeof vehicle.maxRange === "number" && Number.isFinite(vehicle.maxRange)) {
      return `${Math.round(vehicle.maxRange)} km range`;
    }
    return "Battery range unavailable";
  }

  if (typeof vehicle.seatingCapacity === "number" && Number.isFinite(vehicle.seatingCapacity)) {
    return `${vehicle.seatingCapacity} seats`;
  }
  return "4 seats";
}

function resolveCoordinates(
  location: VehicleApiResponse["location"],
): [number | null, number | null] {
  const latitude = location?.latitude;
  const longitude = location?.longitude;

  if (typeof latitude === "number" && typeof longitude === "number") {
    return [latitude, longitude];
  }

  return [null, null];
}

function resolvePricePerMinute(costPerMinute: number | null): number {
  if (typeof costPerMinute !== "number" || !Number.isFinite(costPerMinute)) {
    return 0;
  }

  return costPerMinute;
}

function normalizeOptionalText(value: string | null): string | null {
  if (typeof value !== "string") {
    return null;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : null;
}
