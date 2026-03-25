import { SiteNav } from "../root";
import { useEffect, useState } from "react";
import { apiFetch } from "../utils/api";
import { clearActiveTrip, getActiveTrip, type ActiveTrip } from "../utils/trips";
import type { Route } from "./+types/vehicle-return";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Return | SUMMS" },
    { name: "description", content: "Handle vehicle returns after rentals." },
  ];
}

interface TripEndResponse {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
}

export default function VehicleReturnPage() {
  const [activeTrip, setActiveTripState] = useState<ActiveTrip | null>(null);
  const [latitude, setLatitude] = useState("45.50");
  const [longitude, setLongitude] = useState("-73.57");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setActiveTripState(getActiveTrip());
  }, []);

  async function handleEndTrip(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!activeTrip) {
      setError("No active trip found. Start a trip first.");
      setMessage(null);
      return;
    }

    const parsedLatitude = Number(latitude);
    const parsedLongitude = Number(longitude);
    if (!Number.isFinite(parsedLatitude) || !Number.isFinite(parsedLongitude)) {
      setError("Please enter valid drop-off coordinates.");
      setMessage(null);
      return;
    }

    setIsSubmitting(true);
    setError(null);
    setMessage(null);

    try {
      const response = await apiFetch(`/api/trips/${activeTrip.tripId}/end`, {
        method: "POST",
        body: JSON.stringify({
          dropOffLocation: {
            latitude: parsedLatitude,
            longitude: parsedLongitude,
          },
        }),
      });

      const payload = (await response.json()) as TripEndResponse | { message?: string };
      if (!response.ok) {
        const errorMessage = "message" in payload && payload.message
          ? payload.message
          : "Unable to end trip.";
        throw new Error(errorMessage);
      }

      const endedTrip = payload as TripEndResponse;
      clearActiveTrip();
      setActiveTripState(null);
      setMessage(
        `Trip #${endedTrip.tripId} ended. Duration: ${endedTrip.totalDurationMinutes} minute(s).`,
      );
    } catch (caught) {
      const messageToShow = caught instanceof Error ? caught.message : "Unable to end trip.";
      setError(messageToShow);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Vehicle Return</h1>
        </header>

        {!activeTrip && (
          <div className="rounded-xl border border-amber-500/70 bg-amber-500/20 px-5 py-4">
            <p className="text-amber-200">
              No active trip found in this browser session. Use the <strong>Active Trip</strong> page to manage your current rental.
            </p>
          </div>
        )}

        {activeTrip && (
          <section className="max-w-2xl space-y-5">
            {/* Trip Info Card */}
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h3 className="text-lg font-semibold text-cyan-400 mb-3">Active Trip Details</h3>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div className="rounded-lg bg-gray-900 p-3">
                  <p className="text-xs uppercase tracking-widest text-gray-400">Trip ID</p>
                  <p className="text-lg font-semibold text-white">{activeTrip.tripId}</p>
                </div>
                <div className="rounded-lg bg-gray-900 p-3">
                  <p className="text-xs uppercase tracking-widest text-gray-400">Vehicle ID</p>
                  <p className="text-lg font-semibold text-white">{activeTrip.vehicleId}</p>
                </div>
              </div>
            </article>

            {/* Return Form Card */}
            <form
              className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4 space-y-4"
              onSubmit={handleEndTrip}
            >
              <h3 className="text-lg font-semibold text-cyan-400">Return Vehicle</h3>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm uppercase tracking-widest text-gray-300 mb-2" htmlFor="dropoff-latitude">
                    Drop-off Latitude
                  </label>
                  <input
                    id="dropoff-latitude"
                    type="number"
                    step="any"
                    value={latitude}
                    onChange={(event) => setLatitude(event.target.value)}
                    disabled={isSubmitting}
                    placeholder="45.50"
                    className="w-full rounded-lg border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-white placeholder:text-gray-500 outline-none disabled:opacity-50"
                  />
                </div>

                <div>
                  <label className="block text-sm uppercase tracking-widest text-gray-300 mb-2" htmlFor="dropoff-longitude">
                    Drop-off Longitude
                  </label>
                  <input
                    id="dropoff-longitude"
                    type="number"
                    step="any"
                    value={longitude}
                    onChange={(event) => setLongitude(event.target.value)}
                    disabled={isSubmitting}
                    placeholder="-73.57"
                    className="w-full rounded-lg border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-white placeholder:text-gray-500 outline-none disabled:opacity-50"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-lg bg-cyan-600 text-white px-4 py-2.5 font-semibold transition hover:bg-cyan-500 disabled:opacity-50"
              >
                {isSubmitting ? "Processing Return..." : "Return Vehicle"}
              </button>
            </form>

            {/* Info Card */}
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h3 className="text-base font-semibold text-gray-300 mb-2">Valid Drop-off Zones</h3>
              <p className="text-sm text-gray-400 mb-3">
                You can return your vehicle in simulated valid drop-off zones near:
              </p>
              <ul className="space-y-1 text-sm text-gray-400 list-disc list-inside">
                <li>Montreal Downtown (45.49°N - 45.53°N, 73.54°W - 73.59°W)</li>
                <li>Verdun (45.44°N - 45.47°N, 73.55°W - 73.60°W)</li>
                <li>Plateau (45.52°N - 45.55°N, 73.56°W - 73.61°W)</li>
              </ul>
            </article>
          </section>
        )}

        {message && (
          <div className="mt-5 rounded-xl border border-green-500/70 bg-green-500/20 px-4 py-3 text-sm text-green-200">
            {message}
          </div>
        )}
        {error && (
          <div className="mt-5 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-3 text-sm text-red-200">
            {error}
          </div>
        )}
      </main>
    </>
  );
}
