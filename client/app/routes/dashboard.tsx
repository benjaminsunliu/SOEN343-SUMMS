import { SiteNav } from "../root";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import {
  apiFetch,
  listParkingReservations,
  type ParkingReservationResponse,
} from "../utils/api";
import { getAuthUser } from "../utils/auth";
import type { Route } from "./+types/dashboard";
import { getBrowserLocation, type GeoLocation } from "../utils/location";
import { MapView, type MapMarker } from "../components/MapView";

interface VehicleResponse {
  id: number;
  type: string;
  status: string;
  location: {
    latitude: number | null;
    longitude: number | null;
  } | null;
}

interface ReservationSummary {
  reservationId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
}

type ReservationType = "VEHICLE" | "PARKING";

interface BookingSummary {
  reservationId: number;
  reservationType: ReservationType;
  title: string;
  subtitle: string;
  sortTimestamp: number;
}

interface ActiveTripSummary {
  tripId: number;
  vehicleId: number;
}

// Default map center for dashboard when user location is not yet available
const DEFAULT_CENTER: [number, number] = [45.5019, -73.5674];

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Dashboard | SUMMS" },
    {
      name: "description",
      content: "Dashboard overview for rentals, vehicles, parking, and weather.",
    },
  ];
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const authUser = useMemo(() => getAuthUser(), []);
  const [availableVehicles, setAvailableVehicles] = useState<VehicleResponse[]>([]);
  const [activeReservations, setActiveReservations] = useState<number | null>(null);
  const [reservations, setReservations] = useState<BookingSummary[]>([]);
  const [activeTrip, setActiveTrip] = useState<ActiveTripSummary | null>(null);
  const [isLoadingReservations, setIsLoadingReservations] = useState(true);
  const [isLoadingMap, setIsLoadingMap] = useState(true);
  const [mapError, setMapError] = useState<string | null>(null);

  const [userLocation, setUserLocation] = useState<GeoLocation | null>(null);
  const [hasPreciseUserLocation, setHasPreciseUserLocation] = useState(false);
  const [isLocating, setIsLocating] = useState(true);
  const [locationError, setLocationError] = useState<string | null>(null);

  const [weather, setWeather] = useState<{
    temperature: number | null;
    windSpeed: number | null;
    humidity: number | null;
    description: string | null;
  }>({ temperature: null, windSpeed: null, humidity: null, description: null });
  const [isLoadingWeather, setIsLoadingWeather] = useState(false);
  const [weatherError, setWeatherError] = useState<string | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>(DEFAULT_CENTER);

  useEffect(() => {
    // Fetch vehicles
    let isMounted = true;

    async function loadAvailableVehicles() {
      try {
        const response = await apiFetch("/api/vehicles/status/AVAILABLE");
        if (!response.ok) {
          throw new Error("Failed to load vehicles");
        }

        const vehicles = (await response.json()) as VehicleResponse[];
        if (!isMounted) return;

        setAvailableVehicles(vehicles);
        setMapError(null);
      } catch {
        if (isMounted) {
          setMapError("Unable to load live vehicle coordinates.");
          setAvailableVehicles([]);
        }
      } finally {
        if (isMounted) {
          setIsLoadingMap(false);
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

    async function loadReservations() {
      try {
        const [reservationsResponse, parkingReservations, activeTripResponse] = await Promise.all([
          apiFetch("/api/reservations"),
          listParkingReservations().catch(() => []),
          authUser ? apiFetch(`/api/trips/active/${authUser.id}`) : Promise.resolve(null),
        ]);

        if (!reservationsResponse.ok) {
          throw new Error("Failed to load reservations");
        }

        const vehicleReservations = (await reservationsResponse.json()) as ReservationSummary[];
        if (!isMounted) {
          return;
        }

        const confirmedVehicleBookings: BookingSummary[] = vehicleReservations
          .filter((reservation) => reservation.status.toUpperCase() === "CONFIRMED")
          .map((reservation) => ({
            reservationId: reservation.reservationId,
            reservationType: "VEHICLE",
            title: `Reservation #${reservation.reservationId}`,
            subtitle: `Vehicle #${reservation.vehicleId} - ${reservation.city}`,
            sortTimestamp: Date.parse(reservation.startDate),
          }));

        const confirmedParkingBookings: BookingSummary[] = parkingReservations
          .filter((reservation) => reservation.status.toUpperCase() === "CONFIRMED")
          .map((reservation) => ({
            reservationId: reservation.reservationId,
            reservationType: "PARKING",
            title: `Parking Reservation #${reservation.reservationId}`,
            subtitle: `${reservation.facilityName} - ${reservation.city}`,
            sortTimestamp: parkingReservationSortTimestamp(reservation),
          }));

        const activeBookings = [...confirmedVehicleBookings, ...confirmedParkingBookings]
          .map((booking) => ({
            ...booking,
            sortTimestamp: Number.isFinite(booking.sortTimestamp)
              ? booking.sortTimestamp
              : booking.reservationId,
          }))
          .sort((a, b) => b.sortTimestamp - a.sortTimestamp);

        let hasActiveTrip = false;

        if (activeTripResponse && activeTripResponse.ok) {
          const trip = (await activeTripResponse.json()) as { tripId: number; vehicleId: number };
          setActiveTrip({ tripId: trip.tripId, vehicleId: trip.vehicleId });
          hasActiveTrip = true;
        } else {
          setActiveTrip(null);
        }

        setActiveReservations(
          activeBookings.length + (hasActiveTrip ? 1 : 0),
        );
        setReservations(activeBookings);
      } catch {
        if (isMounted) {
          setReservations([]);
          setActiveTrip(null);
          setActiveReservations(null);
        }
      } finally {
        if (isMounted) {
          setIsLoadingReservations(false);
        }
      }
    }

    void loadReservations();

    return () => {
      isMounted = false;
    };
  }, [authUser]);

  useEffect(() => {
    // Get browser location
    let isMounted = true;

    async function locateAndFetchWeather() {
      try {
        setIsLocating(true);
        const loc = await getBrowserLocation();
        if (!isMounted) return;
        setUserLocation(loc);
        setMapCenter([loc.latitude, loc.longitude]);
        setHasPreciseUserLocation(true);
        setLocationError(null);

        // Fetch weather from Open-Meteo
        setIsLoadingWeather(true);
        const searchParams = new URLSearchParams({
          latitude: String(loc.latitude),
          longitude: String(loc.longitude),
          current_weather: "true",
          hourly: "relative_humidity_2m",
          timezone: "auto",
        });
        const weatherResponse = await fetch(`https://api.open-meteo.com/v1/forecast?${searchParams.toString()}`);
        if (!weatherResponse.ok) {
          throw new Error("Failed to fetch weather");
        }
        const data = await weatherResponse.json();

        const temperature = data.current_weather?.temperature ?? null;
        const windSpeed = data.current_weather?.windspeed ?? null;

        let humidity: number | null = null;
        try {
          if (data.hourly?.time && data.hourly?.relative_humidity_2m) {
            const nowIso = new Date().toISOString().slice(0, 13);
            const idx = (data.hourly.time as string[]).findIndex((t: string) => t.startsWith(nowIso));
            if (idx >= 0) {
              humidity = data.hourly.relative_humidity_2m[idx] ?? null;
            }
          }
        } catch {
          humidity = null;
        }

        const description = buildWeatherDescription(data.current_weather?.weathercode);

        if (isMounted) {
          setWeather({ temperature, windSpeed, humidity, description });
          setWeatherError(null);
        }
      } catch (error) {
        if (isMounted) {
          console.error("Weather/location error", error);
          setLocationError(
            "We couldn't access your location. Showing generic Montreal conditions.",
          );
          setWeatherError("Unable to load live weather.");
          setHasPreciseUserLocation(false);
        }
      } finally {
        if (isMounted) {
          setIsLocating(false);
          setIsLoadingWeather(false);
        }
      }
    }

    void locateAndFetchWeather();

    return () => {
      isMounted = false;
    };
  }, [authUser]);

  const mapMarkers: MapMarker[] = useMemo(() => {
    const vehicleMarkers = availableVehicles
      .filter(
        (vehicle) =>
          vehicle.location !== null &&
          typeof vehicle.location.latitude === "number" &&
          typeof vehicle.location.longitude === "number",
      )
      .map((vehicle) => ({
        position: [
          vehicle.location!.latitude as number,
          vehicle.location!.longitude as number,
        ] as [number, number],
        label: `${vehicle.type} #${vehicle.id}`,
        kind: markerKindForVehicleType(vehicle.type),
      }));

    if (hasPreciseUserLocation && userLocation) {
      return [
        {
          position: [userLocation.latitude, userLocation.longitude] as [
            number,
            number,
          ],
          label: "You are here",
          kind: "user",
        },
        ...vehicleMarkers,
      ];
    }

    return vehicleMarkers;
  }, [availableVehicles, hasPreciseUserLocation, userLocation]);

  const vehiclesByType = useMemo(() => {
    const counts = { bikes: 0, cars: 0, scooters: 0 };
    availableVehicles.forEach((vehicle) => {
      if (vehicle.type === "BICYCLE") {
        counts.bikes += 1;
      } else if (vehicle.type === "CAR") {
        counts.cars += 1;
      } else {
        counts.scooters += 1;
      }
    });
    return counts;
  }, [availableVehicles]);

  const statCards = [
    {
      label: "Active Rentals",
      value: activeReservations === null ? "--" : String(activeReservations),
      valueClass: "text-cyan-400",
    },
    {
      label: "Available Vehicles",
      value: String(availableVehicles.length),
      valueClass: "text-blue-500",
    },
    { label: "Free Parking Spots", value: "382", valueClass: "text-orange-400" },
    { label: "CO2 Saved Today", value: "2.4+", valueClass: "text-violet-400" },
  ];

  const quickActions = [
    { label: "Find a Vehicle", to: "/vehicles/search" },
    { label: "Book / Reserve", to: "/reservation" },
    { label: "Find Parking", to: "/services/parking" },
    { label: "Transit Routes", to: "/services/public-transport" },
  ];

  return (
    <>
      <SiteNav />
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
        <header className="mb-3 border-b border-[#253047] pb-2.5">
          <h1 className="text-xl font-bold tracking-tight">Dashboard</h1>
        </header>

        <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2.5 text-amber-300">
          <p className="text-lg font-semibold">
            {weather.description ?? "Weather"} Alert
          </p>
          <p className="text-sm">
            {isLoadingWeather && "Loading latest conditions..."}
            {!isLoadingWeather &&
              (weather.temperature != null
                ? `Currently ${weather.temperature.toFixed(1)}°C with ${
                    weather.windSpeed != null ? `${weather.windSpeed.toFixed(0)} km/h` : ""
                  } winds. Enclosed vehicles recommended in heavy rain or snow.`
                : "Live weather data is unavailable. Please check your connection.")}
          </p>
        </div>

        <section className="mb-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {statCards.map((card) => (
            <article key={card.label} className="rounded-xl border border-[#2a354a] bg-[#06142b] p-3.5">
              <p className="text-base text-gray-300">{card.label}</p>
              <p className={`mt-2 text-4xl font-bold ${card.valueClass}`}>{card.value}</p>
            </article>
          ))}
        </section>

        <section className="mb-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {quickActions.map((action) => (
            <button
              key={action.label}
              type="button"
              onClick={() => navigate(action.to)}
              className="rounded-xl border border-[#2a354a] bg-[#06142b] px-4 py-2.5 text-base font-semibold text-gray-100 transition hover:bg-[#11213c]"
            >
              {action.label}
            </button>
          ))}
        </section>

         <section className="grid gap-3 xl:grid-cols-[1.8fr_1fr]">
           <article className="rounded-xl border border-[#2a354a] bg-black">
             <h2 className="border-b border-[#2a354a] px-4 py-2.5 text-xl font-semibold">Live City Map</h2>
             <div className="p-3.5">
               <div className="relative h-56 overflow-hidden rounded-lg border border-[#1f2e49]">
                 <MapView
                   center={mapCenter}
                   zoom={13}
                   markers={mapMarkers}
                   className="h-full w-full rounded-lg overflow-hidden"
                 />
                 {hasPreciseUserLocation && userLocation && (
                   <button
                     type="button"
                     onClick={() =>
                       setMapCenter([userLocation.latitude, userLocation.longitude])
                     }
                     className="absolute right-3 top-3 z-[500] rounded-md border border-cyan-300 bg-black/80 px-2.5 py-1 text-xs font-semibold text-cyan-200 hover:bg-black"
                   >
                     My Location
                   </button>
                 )}

                 {isLoadingMap && (
                   <p className="pointer-events-none absolute inset-0 grid place-items-center text-sm text-gray-200 bg-black/40">
                     Loading vehicles...
                   </p>
                 )}
                 {!isLoadingMap && mapMarkers.length === 0 && (
                   <p className="pointer-events-none absolute inset-0 grid place-items-center text-sm text-gray-200 bg-black/40">
                     {mapError ?? "No available vehicle coordinates to display."}
                   </p>
                 )}
               </div>
               <div className="mt-3 flex flex-wrap gap-2 text-sm">
                 <span className="rounded-md bg-black px-3 py-1 text-cyan-400">Bike: {vehiclesByType.bikes}</span>
                 <span className="rounded-md bg-black px-3 py-1 text-blue-500">Car: {vehiclesByType.cars}</span>
                 <span className="rounded-md bg-black px-3 py-1 text-red-400">Scooter: {vehiclesByType.scooters}</span>
                 <span className="rounded-md bg-black px-3 py-1 text-gray-300">Available: {availableVehicles.length}</span>
               </div>
             </div>
           </article>

          <div className="space-y-3">
            <article className="rounded-xl border border-[#2a354a] bg-[#06142b] p-3.5">
              <p className="text-4xl font-bold">
                {weather.temperature != null ? `${weather.temperature.toFixed(0)}°C` : "--"}
              </p>
              <p className="mt-1 text-lg text-gray-300">
                {weather.description ?? "Current Conditions"}
                {userLocation && " - Your Location"}
              </p>
              <div className="mt-2 flex items-center gap-4 border-b border-[#2a354a] pb-2 text-base text-gray-200">
                <span>
                  Humidity {weather.humidity != null ? `${weather.humidity}%` : "--"}
                </span>
                <span>
                  Wind {weather.windSpeed != null ? `${weather.windSpeed.toFixed(0)} km/h` : "--"}
                </span>
              </div>
              <p className="mt-2 text-sm text-amber-300">
                {isLoadingWeather
                  ? "Loading live weather for your area..."
                  : weatherError ??
                    "Bike-sharing may be restricted in heavy rain or snow. Cars and Metro recommended."}
              </p>
            </article>

            <article className="rounded-xl border border-[#2a354a] bg-[#06142b]">
              <div className="flex items-center justify-between border-b border-[#2a354a] px-4 py-2.5">
                <h2 className="text-xl font-semibold">My Active Bookings</h2>
                <button
                  type="button"
                  onClick={() => navigate("/my-reservations")}
                  className="rounded-md border border-cyan-400 px-2.5 py-1 text-xs font-semibold text-cyan-300 transition hover:bg-cyan-500/15"
                >
                  View All
                </button>
              </div>
              <div className="space-y-3 p-3.5">
                {isLoadingReservations ? (
                  <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2 text-sm text-gray-300">
                    Loading active bookings...
                  </div>
                ) : reservations.length === 0 && !activeTrip ? (
                  <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2 text-sm text-gray-300">
                    No active bookings.
                  </div>
                ) : (
                  <>
                    {activeTrip && (
                      <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2">
                        <p className="text-lg font-semibold">Active Trip #{activeTrip.tripId}</p>
                        <p className="text-sm text-gray-300">Vehicle #{activeTrip.vehicleId} - In progress</p>
                      </div>
                    )}
                    {reservations.map((reservation) => (
                      <div
                        key={`${reservation.reservationType}:${reservation.reservationId}`}
                        className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2"
                      >
                        <p className="text-lg font-semibold">{reservation.title}</p>
                        <p className="text-sm text-gray-300">{reservation.subtitle}</p>
                      </div>
                    ))}
                  </>
                )}
              </div>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}

function parkingReservationSortTimestamp(reservation: ParkingReservationResponse): number {
  const timestamp = Date.parse(`${reservation.arrivalDate}T${reservation.arrivalTime}`);
  if (Number.isFinite(timestamp)) {
    return timestamp;
  }
  return reservation.reservationId;
}

function buildWeatherDescription(weatherCode: number | undefined): string | null {
  if (weatherCode == null) return null;
  // Basic mapping based on Open-Meteo weather codes
  if (weatherCode === 0) return "Clear sky";
  if ([1, 2, 3].includes(weatherCode)) return "Partly cloudy";
  if ([45, 48].includes(weatherCode)) return "Foggy";
  if ([51, 53, 55].includes(weatherCode)) return "Drizzle";
  if ([61, 63, 65].includes(weatherCode)) return "Rain";
  if ([71, 73, 75, 77].includes(weatherCode)) return "Snow";
  if ([80, 81, 82].includes(weatherCode)) return "Rain showers";
  if ([85, 86].includes(weatherCode)) return "Snow showers";
  if ([95, 96, 99].includes(weatherCode)) return "Thunderstorm";
  return "Mixed conditions";
}

function markerKindForVehicleType(
  type: string,
): "bike" | "car" | "scooter" | "vehicle" {
  const normalizedType = type.toUpperCase();
  if (normalizedType === "BICYCLE" || normalizedType === "BIKE") {
    return "bike";
  }
  if (normalizedType === "SCOOTER") {
    return "scooter";
  }
  if (normalizedType === "CAR") {
    return "car";
  }
  return "vehicle";
}