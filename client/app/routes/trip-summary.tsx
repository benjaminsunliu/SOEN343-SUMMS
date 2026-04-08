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
  co2SavedKg: number;
  vehicleType: string;
}

export default function TripSummaryPage() {
  const params = useParams();
  const [trip, setTrip] = useState<TripSummaryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadTrip() {
      if (!params.tripId) {
        setError("Missing trip ID.");
        setIsLoading(false);
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
      } finally {
        setIsLoading(false);
      }
    }

    void loadTrip();
  }, [params.tripId]);

  const calculateEstimatedCost = (durationMinutes: number | null): string => {
    if (!durationMinutes) return "$0.00";
    // Assume $0.50 per minute (this should come from trip data ideally)
    const cost = durationMinutes * 0.5;
    return `$${cost.toFixed(2)}`;
  };

  return (
    <>
      <SiteNav />
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Trip Summary</h1>
        </header>

        {isLoading && (
          <div className="rounded-xl border border-gray-700 bg-gray-950 p-5 text-center">
            <p className="text-gray-300">Loading trip details...</p>
          </div>
        )}

        {!isLoading && trip && (
          <section className="max-w-2xl space-y-5">
            {/* Main Trip Summary Card */}
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <h2 className="border-b border-[#2a354a] px-5 py-3 text-xl font-semibold">Trip #{trip.tripId}</h2>

              <div className="space-y-3 p-5">
                <div className="grid grid-cols-2 gap-3">
                  <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                    <p className="text-xs uppercase tracking-[0.2em] text-gray-400">Trip ID</p>
                    <p className="text-xl font-medium leading-tight text-white">{trip.tripId}</p>
                  </div>

                  <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                    <p className="text-xs uppercase tracking-[0.2em] text-gray-400">Vehicle ID</p>
                    <p className="text-xl font-medium leading-tight text-white">{trip.vehicleId}</p>
                  </div>
                </div>

                <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                  <p className="text-xs uppercase tracking-[0.2em] text-gray-400">Start Time</p>
                  <p className="text-lg font-medium leading-tight text-white">{new Date(trip.startTime).toLocaleString()}</p>
                </div>

                <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                  <p className="text-xs uppercase tracking-[0.2em] text-gray-400">End Time</p>
                  <p className="text-lg font-medium leading-tight text-white">
                    {trip.endTime ? new Date(trip.endTime).toLocaleString() : "Trip still active"}
                  </p>
                </div>

                <div className="border-y border-[#2d3d57] py-4">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-200">Duration</span>
                    <span className="text-2xl font-bold text-cyan-400">
                      {trip.totalDurationMinutes ?? 0} <span className="text-sm text-gray-400">minutes</span>
                    </span>
                  </div>
                </div>

                <div className="flex items-end justify-between text-2xl">
                  <p className="font-semibold">Estimated Total</p>
                  <p className="font-bold text-cyan-400">{calculateEstimatedCost(trip.totalDurationMinutes)}</p>
                </div>

                {trip.vehicleType !== "CAR" && (
                  <div className="border-y border-[#2d3d57] py-4">
                    <div className="flex items-center justify-between">
                      <span className="text-gray-200">CO₂ Saved</span>
                      <span className="text-2xl font-bold text-green-400">
                        {trip.co2SavedKg.toFixed(2)} <span className="text-sm text-gray-400">kg</span>
                      </span>
                    </div>
                    <p className="text-xs text-gray-400 mt-2">
                      Equivalent to taking a car off the road
                    </p>
                  </div>
                )}

                <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                  <p className="text-xs uppercase tracking-[0.2em] text-gray-400">Vehicle Status</p>
                  <p className="text-lg font-medium leading-tight">
                    <span className={trip.vehicleStatus === "AVAILABLE" ? "text-green-400" : "text-yellow-400"}>
                      {trip.vehicleStatus}
                    </span>
                  </p>
                </div>
              </div>
            </article>

            {/* Next Steps Card */}
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h3 className="text-lg font-semibold text-cyan-400 mb-3">What's Next?</h3>
              <p className="text-gray-300 mb-4">
                Your trip has been completed successfully. Your vehicle has been returned to our system and may be available for other customers.
              </p>
              <a
                href="/reservation"
                className="inline-block rounded-lg bg-cyan-600 text-white px-6 py-2.5 font-semibold transition hover:bg-cyan-500"
              >
                Book Another Trip
              </a>
            </article>
          </section>
        )}

        {!isLoading && error && (
          <div className="rounded-xl border border-red-500/70 bg-red-500/20 px-5 py-4">
            <p className="text-red-200">{error}</p>
          </div>
        )}
      </main>
    </>
  );
}
