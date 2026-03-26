import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";
import { SiteNav } from "../root";
import { MapView, type MapMarker } from "../components/MapView";
import { apiFetch } from "../utils/api";
import { getBrowserLocation, type GeoLocation } from "../utils/location";
import {
  DEFAULT_MAP_CENTER,
  mapVehiclesToCatalog,
  type VehicleApiResponse,
  type VehicleCatalogItem,
} from "../utils/vehicle-catalog";
import type { Route } from "./+types/vehicle-search";

type VehicleTypeFilter = "ALL" | VehicleCatalogItem["type"];

const VEHICLE_TYPE_OPTIONS: Array<{
  value: VehicleTypeFilter;
  label: string;
}> = [
  { value: "ALL", label: "All" },
  { value: "Bike", label: "Bike" },
  { value: "Scooter", label: "Scooter" },
  { value: "Car", label: "Car" },
];

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Search | SUMMS" },
    { name: "description", content: "Search available vehicles for rental." },
  ];
}

export default function VehicleSearchPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [vehicles, setVehicles] = useState<VehicleCatalogItem[]>([]);
  const [userLocation, setUserLocation] = useState<GeoLocation | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>(DEFAULT_MAP_CENTER);
  const [isLoadingVehicles, setIsLoadingVehicles] = useState(true);
  const [vehicleError, setVehicleError] = useState<string | null>(null);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [selectedVehicleType, setSelectedVehicleType] =
    useState<VehicleTypeFilter>("ALL");

  useEffect(() => {
    let isMounted = true;

    async function loadAvailableVehicles() {
      try {
        const response = await apiFetch("/api/vehicles/status/AVAILABLE");
        if (!response.ok) {
          throw new Error(`Failed to fetch vehicles (${response.status})`);
        }

        const data = (await response.json()) as VehicleApiResponse[];
        if (!isMounted) {
          return;
        }

        const mappedVehicles = mapVehiclesToCatalog(data);
        if (mappedVehicles.length === 0) {
          setVehicles([]);
          setVehicleError("No live vehicles found.");
          return;
        }

        setVehicles(mappedVehicles);
        setVehicleError(null);
      } catch {
        if (isMounted) {
          setVehicles([]);
          setVehicleError("Unable to load live vehicles.");
        }
      } finally {
        if (isMounted) {
          setIsLoadingVehicles(false);
        }
      }
    }

    void loadAvailableVehicles();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function locateUser() {
      try {
        const location = await getBrowserLocation();
        if (!isMounted) {
          return;
        }

        setUserLocation(location);
        setLocationError(null);
      } catch {
        if (isMounted) {
          setUserLocation(null);
          setLocationError(
            "Location unavailable. Enable browser location to show your position on the map.",
          );
        }
      }
    }

    void locateUser();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!userLocation) {
      return;
    }

    setMapCenter([userLocation.latitude, userLocation.longitude]);
  }, [userLocation]);

  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(
    null,
  );

  const vehiclesWithDistance = useMemo(
    () =>
      vehicles.map((vehicle) => ({
        ...vehicle,
        distance: formatDistanceFromUser(vehicle, userLocation),
      })),
    [userLocation, vehicles],
  );

  const filteredVehicles = useMemo(
    () =>
      vehiclesWithDistance.filter((vehicle) =>
        matchesVehicleTypeFilter(vehicle, selectedVehicleType),
      ),
    [selectedVehicleType, vehiclesWithDistance],
  );

  useEffect(() => {
    if (filteredVehicles.length === 0) {
      setSelectedVehicleId(null);
      return;
    }

    setSelectedVehicleId((previousVehicleId) => {
      if (
        previousVehicleId !== null &&
        filteredVehicles.some((vehicle) => vehicle.id === previousVehicleId)
      ) {
        return previousVehicleId;
      }

      const nextVehicle =
        filteredVehicles.find((vehicle) => vehicle.available) ??
        filteredVehicles[0];
      return nextVehicle.id;
    });
  }, [filteredVehicles]);

  useEffect(() => {
    const vehicleIdFromQuery = Number(searchParams.get("vehicleId"));
    if (!Number.isFinite(vehicleIdFromQuery)) {
      return;
    }

    const matchingVehicle = vehicles.find((vehicle) => vehicle.id === vehicleIdFromQuery);
    if (matchingVehicle) {
      setSelectedVehicleId(matchingVehicle.id);
    }
  }, [searchParams, vehicles]);

  const selectedVehicle = useMemo(
    () => {
      if (filteredVehicles.length === 0) {
        return null;
      }

      if (selectedVehicleId === null) {
        return filteredVehicles[0];
      }

      return (
        filteredVehicles.find((vehicle) => vehicle.id === selectedVehicleId) ??
        filteredVehicles[0]
      );
    },
    [filteredVehicles, selectedVehicleId],
  );

  const reservePrice = selectedVehicle
    ? `$${selectedVehicle.pricePerMinute.toFixed(2)}`
    : "$0.00";
  const conditionLabel = selectedVehicle?.condition ?? "Good";

  const vehicleMarkers: MapMarker[] = filteredVehicles
    .filter(hasCoordinates)
    .map((vehicle) => ({
      position: [vehicle.latitude, vehicle.longitude],
      label: `${vehicle.name} (${vehicle.type})`,
      kind: markerKindForVehicleType(vehicle.type),
    }));

  const markers: MapMarker[] = useMemo(() => {
    if (!userLocation) {
      return vehicleMarkers;
    }

    return [
      {
        position: [userLocation.latitude, userLocation.longitude],
        label: "You are here",
        kind: "user",
      },
      ...vehicleMarkers,
    ];
  }, [vehicleMarkers, userLocation]);

  function selectVehicle(vehicle: VehicleCatalogItem) {
    setSelectedVehicleId(vehicle.id);

    if (hasCoordinates(vehicle)) {
      setMapCenter([vehicle.latitude, vehicle.longitude]);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight">Find a Vehicle</h1>
        </header>
        {vehicleError && (
          <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2 text-sm text-amber-200">
            {vehicleError}
          </div>
        )}
        {locationError && (
          <div className="mb-3 rounded-xl border border-blue-500/70 bg-gray-900lue-500/20 px-4 py-2 text-sm text-blue-200">
            {locationError}
          </div>
        )}

        <section className="grid gap-5 xl:grid-cols-[320px_1fr]">
          <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-4">
            <h2 className="mb-3 border-b border-[#2a354a] pb-2 text-xl font-semibold">Search Filters</h2>

            <form className="space-y-3" onSubmit={(event) => event.preventDefault()}>
              <div>
                <label className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">Vehicle Type</label>
                <div className="grid grid-cols-4 gap-2 rounded-xl bg-[#14233d] p-1">
                  {VEHICLE_TYPE_OPTIONS.map((option) => {
                    const isActive = selectedVehicleType === option.value;
                    return (
                      <button
                        key={option.value}
                        type="button"
                        onClick={() => setSelectedVehicleType(option.value)}
                        className={`rounded-lg px-2 py-1.5 text-xs font-semibold transition ${
                          isActive
                            ? "bg-cyan-400 text-slate-900"
                            : "text-gray-200 hover:bg-[#1d2f4d]"
                        }`}
                      >
                        {option.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div>
                <label htmlFor="city" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  City
                </label>
                <input
                  id="city"
                  type="text"
                  placeholder="Enter city"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="pickup" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Pickup Location
                </label>
                <input
                  id="pickup"
                  type="text"
                  placeholder="Enter pickup location"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="date" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Date
                </label>
                <input
                  id="date"
                  type="date"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="max-price" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Max Price / Min
                </label>
                <input
                  id="max-price"
                  type="text"
                  placeholder="Optional"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <button
                type="submit"
                className="w-full rounded-xl bg-cyan-400 px-4 py-2.5 text-lg font-semibold text-slate-900 transition hover:bg-cyan-300"
              >
                Search Vehicles
              </button>
            </form>
          </article>

          <div className="space-y-3">
            <h2 className="text-xl font-semibold">Map & Available Vehicles Near You</h2>

            <div className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-3">
              <h3 className="mb-2 text-lg font-semibold">Map View</h3>
              <div className="relative">
                <MapView center={mapCenter} markers={markers} />
                {userLocation && (
                  <button
                    type="button"
                    onClick={() =>
                      setMapCenter([userLocation.latitude, userLocation.longitude])
                    }
                    className="absolute right-3 top-3 z-[500] rounded-md border border-cyan-300 bg-gray-900/80 px-2.5 py-1 text-xs font-semibold text-cyan-200 hover:bg-gray-900"
                  >
                    My Location
                  </button>
                )}
                {isLoadingVehicles && (
                  <p className="pointer-events-none absolute inset-0 grid place-items-center rounded-lg bg-gray-900/40 text-sm text-gray-200">
                    Loading live vehicles...
                  </p>
                )}
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {filteredVehicles.length === 0 ? (
                <div className="rounded-xl border border-[#23324c] bg-[#06142b] p-4 text-sm text-gray-300">
                  No vehicles found for this type.
                </div>
              ) : (
                filteredVehicles.map((vehicle) => (
                  <article
                    key={vehicle.id}
                    className={`relative cursor-pointer rounded-2xl border bg-[#06142b] p-3.5 transition-colors ${
                      selectedVehicle?.id === vehicle.id
                        ? "border-cyan-400"
                        : "border-[#23324c] hover:border-[#3f5374]"
                    }`}
                    onClick={() => selectVehicle(vehicle)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        selectVehicle(vehicle);
                      }
                    }}
                    tabIndex={0}
                    role="button"
                    aria-label={`Select ${vehicle.name}`}
                  >
                    <span
                      className={`absolute right-3 top-3 h-3 w-3 rounded-full ${
                        vehicle.available ? "bg-green-400" : "bg-red-400"
                      }`}
                      aria-hidden="true"
                    />
                    <p className="mb-1 text-2xl text-gray-300">#</p>
                    <h3 className="text-xl font-semibold leading-tight">{vehicle.name}</h3>
                    <p className="mt-1 text-base text-gray-300">{vehicle.distance}</p>
                    <p className="mt-1 text-base text-gray-300">{vehicle.energy}</p>
                    <p className="mt-1 text-base text-gray-300">{vehicle.provider}</p>
                    <p
                      className={`mt-2.5 text-2xl font-bold ${
                        vehicle.available ? "text-cyan-400" : "text-gray-400"
                      }`}
                    >
                      {vehicle.available ? vehicle.priceLabel : "Unavailable"}
                    </p>
                  </article>
                ))
              )}
            </div>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <div className="flex items-center justify-between border-b border-[#2a354a] px-4 py-3">
                <h3 className="text-xl font-semibold">
                  Selected: {selectedVehicle ? selectedVehicle.name : "None"}
                </h3>
                <span
                  className={`rounded-md px-3 py-1 text-sm font-semibold ${
                    selectedVehicle?.available
                      ? "bg-green-500/25 text-green-400"
                      : "bg-red-500/25 text-red-300"
                  }`}
                >
                  {selectedVehicle?.type ?? "Vehicle"}
                </span>
              </div>

              <div className="flex flex-col gap-3 px-4 py-4 md:flex-row md:items-end md:justify-between">
                <div className="space-y-1 text-base text-gray-200">
                  <p>Station: {selectedVehicle?.station ?? "--"}</p>
                  <p>Condition: {conditionLabel}</p>
                  <p>Provider: {selectedVehicle?.provider ?? "--"}</p>
                </div>

                <p className="text-4xl font-bold text-cyan-400">
                  {reservePrice}
                  <span className="text-gray-400">/min</span>
                </p>
              </div>

              <div className="px-4 pb-4">
                <button
                  type="button"
                  onClick={() => {
                    if (!selectedVehicle) {
                      return;
                    }

                    navigate("/reservation", {
                      state: {
                        selectedVehicleId: selectedVehicle.id,
                      },
                    });
                  }}
                  disabled={!selectedVehicle || !selectedVehicle.available}
                  className="w-full rounded-xl bg-cyan-400 px-4 py-2.5 text-xl font-semibold text-slate-900 transition hover:bg-cyan-300"
                >
                  {selectedVehicle?.available
                    ? "Reserve this Vehicle"
                    : "Vehicle Unavailable"}
                </button>
              </div>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}

function hasCoordinates(
  vehicle: VehicleCatalogItem,
): vehicle is VehicleCatalogItem & { latitude: number; longitude: number } {
  return (
    typeof vehicle.latitude === "number" &&
    typeof vehicle.longitude === "number"
  );
}

function markerKindForVehicleType(
  type: VehicleCatalogItem["type"],
): "bike" | "car" | "scooter" {
  if (type === "Bike") {
    return "bike";
  }
  if (type === "Scooter") {
    return "scooter";
  }
  return "car";
}

function matchesVehicleTypeFilter(
  vehicle: VehicleCatalogItem,
  filter: VehicleTypeFilter,
): boolean {
  if (filter === "ALL") {
    return true;
  }

  return vehicle.type === filter;
}

function formatDistanceFromUser(
  vehicle: VehicleCatalogItem,
  userLocation: GeoLocation | null,
): string {
  if (!userLocation || !hasCoordinates(vehicle)) {
    return "Distance unavailable";
  }

  const distanceKm = haversineDistanceKm(
    userLocation.latitude,
    userLocation.longitude,
    vehicle.latitude,
    vehicle.longitude,
  );

  if (!Number.isFinite(distanceKm)) {
    return "Distance unavailable";
  }

  if (distanceKm < 1) {
    return `${Math.round(distanceKm * 1000)} m away`;
  }

  return `${distanceKm.toFixed(distanceKm < 10 ? 1 : 0)} km away`;
}

function haversineDistanceKm(
  latitudeOne: number,
  longitudeOne: number,
  latitudeTwo: number,
  longitudeTwo: number,
): number {
  const toRadians = (degrees: number) => (degrees * Math.PI) / 180;
  const earthRadiusKm = 6371;

  const latDiff = toRadians(latitudeTwo - latitudeOne);
  const lonDiff = toRadians(longitudeTwo - longitudeOne);
  const latOneRad = toRadians(latitudeOne);
  const latTwoRad = toRadians(latitudeTwo);

  const a =
    Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
    Math.cos(latOneRad) * Math.cos(latTwoRad) *
      Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return earthRadiusKm * c;
}
