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

interface ReservationDetailsResponse {
  endLocation: {
    latitude: number;
    longitude: number;
  };
}

interface TripDetailsResponse {
  reservationId: number;
}

export default function VehicleReturnPage() {
  const [activeTrip, setActiveTripState] = useState<ActiveTrip | null>(null);
  const [reservationEndLocation, setReservationEndLocation] = useState<ReservationDetailsResponse["endLocation"] | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setActiveTripState(getActiveTrip());
  }, []);

  useEffect(() => {
    if (!activeTrip) {
      setReservationEndLocation(null);
      return;
    }

    const currentTrip = activeTrip;
    let isMounted = true;

    async function loadReservationEndLocation() {
      const reservationId = await resolveReservationId(currentTrip);
      const endLocation = await fetchReservationEndLocation(reservationId);

      if (isMounted) {
        setReservationEndLocation(endLocation);
      }
    }

    void loadReservationEndLocation();
    return () => {
      isMounted = false;
    };
  }, [activeTrip]);

  async function resolveReservationId(trip: ActiveTrip): Promise<number | null> {
    if (typeof trip.reservationId === "number") {
      return trip.reservationId;
    }

    try {
      const tripResponse = await apiFetch(`/api/trips/${trip.tripId}`);
      if (!tripResponse.ok) {
        return null;
      }
      const tripPayload = (await tripResponse.json()) as TripDetailsResponse;
      return typeof tripPayload.reservationId === "number"
        ? tripPayload.reservationId
        : null;
    } catch {
      return null;
    }
  }

  async function fetchReservationEndLocation(
    reservationId: number | null,
  ): Promise<ReservationDetailsResponse["endLocation"] | null> {
    if (reservationId === null) {
      return null;
    }

    try {
      const reservationResponse = await apiFetch(`/api/reservations/${reservationId}`);
      if (!reservationResponse.ok) {
        return null;
      }
      const reservation = (await reservationResponse.json()) as ReservationDetailsResponse;
      return reservation.endLocation;
    } catch {
      return null;
    }
  }

  async function handleEndTrip(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!activeTrip) {
      setError("No active trip found. Start a trip first.");
      setMessage(null);
      return;
    }

    if (!reservationEndLocation) {
      setError("Unable to determine reservation destination for ending this trip.");
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
            latitude: reservationEndLocation.latitude,
            longitude: reservationEndLocation.longitude,
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

            <form
              className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4 space-y-4"
              onSubmit={handleEndTrip}
            >
              <h3 className="text-lg font-semibold text-cyan-400">End Trip</h3>
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-lg bg-cyan-600 text-white px-4 py-2.5 font-semibold transition hover:bg-cyan-500 disabled:opacity-50"
              >
                {isSubmitting ? "Ending Trip..." : "End Trip"}
              </button>
            </form>
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
