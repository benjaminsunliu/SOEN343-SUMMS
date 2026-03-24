import { SiteNav } from "../root";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { apiFetch } from "../utils/api";
import type { Route } from "./+types/dashboard";

interface VehicleResponse {
  id: number;
  type: string;
  status: string;
  location: {
    latitude: number | null;
    longitude: number | null;
  } | null;
}

interface MapPoint {
  id: number;
  type: string;
  left: number;
  top: number;
}

function markerClassByType(type: string): string {
  if (type === "BICYCLE") {
    return "bg-cyan-400 shadow-[0_0_10px_rgba(34,211,238,0.8)]";
  }
  if (type === "CAR") {
    return "bg-blue-500 shadow-[0_0_10px_rgba(59,130,246,0.8)]";
  }
  return "bg-red-400 shadow-[0_0_10px_rgba(248,113,113,0.8)]";
}

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
  const [availableVehicles, setAvailableVehicles] = useState<VehicleResponse[]>([]);
  const [isLoadingMap, setIsLoadingMap] = useState(true);
  const [mapError, setMapError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadAvailableVehicles() {
      try {
        const response = await apiFetch("/api/vehicles/status/AVAILABLE");
        if (!response.ok) {
          throw new Error("Failed to load vehicles");
        }

        const vehicles = (await response.json()) as VehicleResponse[];
        if (!isMounted) {
          return;
        }

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

  const mapPoints = useMemo<MapPoint[]>(() => {
    const withCoordinates = availableVehicles.filter(
      (vehicle) =>
        vehicle.location !== null &&
        typeof vehicle.location.latitude === "number" &&
        typeof vehicle.location.longitude === "number",
    );

    if (withCoordinates.length === 0) {
      return [];
    }

    const latitudes = withCoordinates.map((vehicle) => vehicle.location?.latitude ?? 0);
    const longitudes = withCoordinates.map((vehicle) => vehicle.location?.longitude ?? 0);
    const minLat = Math.min(...latitudes);
    const maxLat = Math.max(...latitudes);
    const minLng = Math.min(...longitudes);
    const maxLng = Math.max(...longitudes);
    const latRange = maxLat - minLat;
    const lngRange = maxLng - minLng;

    return withCoordinates.map((vehicle) => {
      const lat = vehicle.location?.latitude ?? minLat;
      const lng = vehicle.location?.longitude ?? minLng;
      const rawLeft = lngRange === 0 ? 50 : ((lng - minLng) / lngRange) * 100;
      const rawTop = latRange === 0 ? 50 : 100 - ((lat - minLat) / latRange) * 100;

      return {
        id: vehicle.id,
        type: vehicle.type,
        left: Math.min(96, Math.max(4, rawLeft)),
        top: Math.min(96, Math.max(4, rawTop)),
      };
    });
  }, [availableVehicles]);

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
    { label: "Active Rentals", value: "847", valueClass: "text-cyan-400" },
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
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-3 border-b border-[#253047] pb-2.5">
          <h1 className="text-xl font-bold tracking-tight">Dashboard</h1>
        </header>

        <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2.5 text-amber-300">
          <p className="text-lg font-semibold">Weather Alert</p>
          <p className="text-sm">Heavy rain expected in Montreal. Enclosed vehicles recommended.</p>
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
              <div className="relative h-56 overflow-hidden rounded-lg border border-[#1f2e49] bg-black">
                <p className="absolute left-3 top-2 text-[11px] font-medium tracking-wide text-gray-300">
                  Downtown Montreal
                </p>
                <p className="absolute right-3 top-2 text-[11px] text-gray-400">Live availability layer</p>

                {mapPoints.map((point) => (
                  <button
                    key={point.id}
                    type="button"
                    className={`absolute h-2.5 w-2.5 -translate-x-1/2 -translate-y-1/2 rounded-full ${markerClassByType(point.type)}`}
                    style={{ left: `${point.left}%`, top: `${point.top}%` }}
                    onClick={() => navigate(`/vehicles/search?vehicleId=${point.id}`)}
                    title={`${point.type} #${point.id}`}
                    aria-label={`View ${point.type} #${point.id}`}
                  />
                ))}

                {isLoadingMap && (
                  <p className="absolute inset-0 grid place-items-center text-sm text-gray-400">Loading vehicles...</p>
                )}
                {!isLoadingMap && mapPoints.length === 0 && (
                  <p className="absolute inset-0 grid place-items-center text-sm text-gray-500">
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
              <p className="text-4xl font-bold">8C</p>
              <p className="mt-1 text-lg text-gray-300">Heavy Rain - Montreal</p>
              <div className="mt-2 flex items-center gap-4 border-b border-[#2a354a] pb-2 text-base text-gray-200">
                <span>Humidity 94%</span>
                <span>Wind 22km/h</span>
              </div>
              <p className="mt-2 text-sm text-amber-300">Bike-sharing restricted. Cars and Metro recommended.</p>
            </article>

            <article className="rounded-xl border border-[#2a354a] bg-[#06142b]">
              <h2 className="border-b border-[#2a354a] px-4 py-2.5 text-xl font-semibold">My Active Bookings</h2>
              <div className="space-y-3 p-3.5">
                <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2">
                  <p className="text-lg font-semibold">CarShare - Toyota Corolla</p>
                  <p className="text-sm text-gray-300">Mar 18 - 2:00 PM to 5:00 PM</p>
                </div>
                <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2">
                  <p className="text-lg font-semibold">Bike #B-01</p>
                  <p className="text-sm text-gray-300">Mar 20 - All day</p>
                </div>
              </div>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}

