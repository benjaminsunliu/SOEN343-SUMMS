import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import type { Route } from "./+types/trip-summary";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Trip Summary | SUMMS" },
    { name: "description", content: "Completed trip details and billing context." },
  ];
}

interface TripSummaryResponse {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
}

export default function TripSummaryPage() {
  const params = useParams();
  const [trip, setTrip] = useState<TripSummaryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadTrip() {
      if (!params.tripId) {
        setError("Missing trip ID.");
        return;
      }

      try {
        const response = await apiFetch(`/api/trips/${params.tripId}`);
        const payload = (await response.json()) as TripSummaryResponse | { message?: string };

        if (!response.ok) {
          const errorMessage = "message" in payload && payload.message
            ? payload.message
            : "Unable to load trip summary.";
          throw new Error(errorMessage);
        }

        setTrip(payload as TripSummaryResponse);
      } catch (caught) {
        const messageToShow = caught instanceof Error ? caught.message : "Unable to load trip summary.";
        setError(messageToShow);
      }
    }

    void loadTrip();
  }, [params.tripId]);

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Trip Summary</h1>

        {trip && (
          <section className="max-w-2xl space-y-2 bg-gray-950 border border-gray-800 rounded-xl p-5 text-sm">
            <p className="text-gray-300">Trip ID: <span className="text-white">{trip.tripId}</span></p>
            <p className="text-gray-300">Vehicle ID: <span className="text-white">{trip.vehicleId}</span></p>
            <p className="text-gray-300">Start: <span className="text-white">{trip.startTime}</span></p>
            <p className="text-gray-300">End: <span className="text-white">{trip.endTime ?? "N/A"}</span></p>
            <p className="text-gray-300">Duration: <span className="text-white">{trip.totalDurationMinutes ?? 0} minute(s)</span></p>
            <p className="text-gray-300">Vehicle Status: <span className="text-white">{trip.vehicleStatus}</span></p>
          </section>
        )}

        {error && <p className="mt-4 text-red-300">{error}</p>}
      </main>
    </>
  );
}
