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
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Vehicle Return</h1>

        {!activeTrip && (
          <p className="text-gray-400">No active trip found in this browser session.</p>
        )}

        {activeTrip && (
          <form
            className="max-w-md space-y-4 bg-gray-950 border border-gray-800 rounded-xl p-4"
            onSubmit={handleEndTrip}
          >
            <p className="text-sm text-gray-300">Active Trip ID: {activeTrip.tripId}</p>
            <p className="text-sm text-gray-300">Vehicle ID: {activeTrip.vehicleId}</p>

            <label className="block text-sm text-gray-300" htmlFor="dropoff-latitude">
              Drop-off Latitude
            </label>
            <input
              id="dropoff-latitude"
              type="number"
              step="any"
              value={latitude}
              onChange={(event) => setLatitude(event.target.value)}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
            />

            <label className="block text-sm text-gray-300" htmlFor="dropoff-longitude">
              Drop-off Longitude
            </label>
            <input
              id="dropoff-longitude"
              type="number"
              step="any"
              value={longitude}
              onChange={(event) => setLongitude(event.target.value)}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
            />

            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-lg bg-cyan-600 text-white px-4 py-2 disabled:opacity-50"
            >
              {isSubmitting ? "Ending..." : "End Trip"}
            </button>
          </form>
        )}

        <p className="mt-4 text-gray-400">
          Simulated valid drop-off zones are near Montreal Downtown, Verdun, and Plateau.
        </p>

        {message && <p className="mt-4 text-green-300">{message}</p>}
        {error && <p className="mt-4 text-red-300">{error}</p>}
      </main>
    </>
  );
}
