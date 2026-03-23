import { SiteNav } from "../root";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { apiFetch } from "../utils/api";
import { getAuthUser } from "../utils/auth";
import { setActiveTrip } from "../utils/trips";
import type { Route } from "./+types/reservation";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservation | SUMMS" },
    { name: "description", content: "Create and manage vehicle reservations." },
  ];
}

interface TripStartResponse {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
}

export default function ReservationPage() {
  const navigate = useNavigate();
  const authUser = useMemo(() => getAuthUser(), []);
  const [vehicleIdInput, setVehicleIdInput] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const canStartTrip = authUser?.role === "CITIZEN" || authUser?.role === "ADMIN";

  async function handleStartTrip(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!authUser) {
      setError("You must be logged in to start a trip.");
      setMessage(null);
      return;
    }

    const vehicleId = Number(vehicleIdInput);
    if (!Number.isFinite(vehicleId) || vehicleId <= 0) {
      setError("Please enter a valid vehicle ID.");
      setMessage(null);
      return;
    }

    setIsSubmitting(true);
    setError(null);
    setMessage(null);

    try {
      const response = await apiFetch("/api/trips/start", {
        method: "POST",
        body: JSON.stringify({
          vehicleId,
          citizenId: authUser.id,
          reservationCode: `RES-${vehicleId}-${authUser.id}`,
          reservationValidUntil: new Date(Date.now() + 10 * 60 * 1000).toISOString(),
          paymentAuthorizationCode: `PAY-${Date.now()}`,
        }),
      });

      const payload = (await response.json()) as TripStartResponse | { message?: string };
      if (!response.ok) {
        const errorMessage = "message" in payload && payload.message
          ? payload.message
          : "Unable to start trip.";
        throw new Error(errorMessage);
      }

      const startedTrip = payload as TripStartResponse;
      setActiveTrip({
        tripId: startedTrip.tripId,
        vehicleId: startedTrip.vehicleId,
        citizenId: startedTrip.citizenId,
        startTime: startedTrip.startTime,
      });

      setMessage(
        `Trip #${startedTrip.tripId} started. Vehicle is now ${startedTrip.vehicleStatus}.`,
      );
      setVehicleIdInput("");
      navigate("/trips/active");
    } catch (caught) {
      const messageToShow = caught instanceof Error ? caught.message : "Unable to start trip.";
      setError(messageToShow);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Reservation</h1>

        {!canStartTrip && (
          <p className="text-red-300 mb-4">
            Only citizen/admin accounts can start trips.
          </p>
        )}

        <form
          className="max-w-md space-y-4 bg-gray-950 border border-gray-800 rounded-xl p-4"
          onSubmit={handleStartTrip}
        >
          <label className="block text-sm text-gray-300" htmlFor="vehicleId">
            Vehicle ID
          </label>
          <input
            id="vehicleId"
            type="number"
            min={1}
            value={vehicleIdInput}
            onChange={(event) => setVehicleIdInput(event.target.value)}
            disabled={!canStartTrip || isSubmitting}
            className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
            placeholder="Enter vehicle ID"
          />

          <button
            type="submit"
            disabled={!canStartTrip || isSubmitting}
            className="rounded-lg bg-cyan-600 text-white px-4 py-2 disabled:opacity-50"
          >
            {isSubmitting ? "Starting..." : "Start Trip"}
          </button>
        </form>

        {message && <p className="mt-4 text-green-300">{message}</p>}
        {error && <p className="mt-4 text-red-300">{error}</p>}
      </main>
    </>
  );
}
