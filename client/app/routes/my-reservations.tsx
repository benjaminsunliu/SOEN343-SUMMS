import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { setActiveTrip } from "../utils/trips";
import type { Route } from "./+types/my-reservations";

interface ReservationLocation {
  latitude: number;
  longitude: number;
}

interface ReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startLocation: ReservationLocation;
  endLocation: ReservationLocation;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "My Reservations | SUMMS" },
    { name: "description", content: "View and manage your reservations." },
  ];
}

export default function MyReservationsPage() {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [isLoadingReservations, setIsLoadingReservations] = useState(true);
  const [cancelingReservationIds, setCancelingReservationIds] = useState<number[]>([]);
  const [startingReservationIds, setStartingReservationIds] = useState<number[]>([]);
  const [reservationError, setReservationError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadReservations() {
      try {
        const response = await apiFetch("/api/reservations");
        if (!response.ok) {
          const message = await readErrorMessage(
            response,
            "Unable to load reservations.",
          );
          throw new Error(message);
        }

        const data = (await response.json()) as ReservationResponse[];
        if (!isMounted) {
          return;
        }

        const activeStatuses = new Set(["PENDING", "CONFIRMED"]);
        const filtered = data.filter((r) => activeStatuses.has(r.status.toUpperCase()));
        setReservations([...filtered].sort((a, b) => b.reservationId - a.reservationId));
        setReservationError(null);
      } catch (error) {
        if (isMounted) {
          setReservationError(
            error instanceof Error ? error.message : "Unable to load reservations.",
          );
          setReservations([]);
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
  }, []);

  const handleStartTrip = async (reservationId: number) => {
    setActionError(null);
    setActionMessage(null);
    setStartingReservationIds((prev) => [...prev, reservationId]);

    try {
      const response = await apiFetch(`/api/rentals/${reservationId}/start`, {
        method: "POST",
      });

      if (!response.ok) {
        const message = await readErrorMessage(response, "Could not start trip.");
        setActionError(message);
        return;
      }

      const trip = (await response.json()) as {
        tripId: number;
        vehicleId: number;
        citizenId: number;
        startTime: string;
      };

      setActiveTrip({
        tripId: trip.tripId,
        vehicleId: trip.vehicleId,
        citizenId: trip.citizenId,
        startTime: trip.startTime,
      });

      navigate("/trips/active");
    } catch {
      setActionError("Network error while starting trip.");
    } finally {
      setStartingReservationIds((prev) => prev.filter((id) => id !== reservationId));
    }
  };

  const handleCancelReservation = async (reservationId: number) => {
    setActionError(null);
    setActionMessage(null);
    setCancelingReservationIds((previousIds) => [...previousIds, reservationId]);

    try {
      const response = await apiFetch(`/api/reservations/${reservationId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        const message = await readErrorMessage(
          response,
          "Could not cancel reservation.",
        );
        setActionError(message);
        return;
      }

      setReservations((previousReservations) =>
        previousReservations.map((reservation) =>
          reservation.reservationId === reservationId
            ? { ...reservation, status: "CANCELLED" }
            : reservation,
        ),
      );
      setActionMessage(`Reservation #${reservationId} has been cancelled.`);
    } catch {
      setActionError("Network error while canceling reservation.");
    } finally {
      setCancelingReservationIds((previousIds) =>
        previousIds.filter((id) => id !== reservationId),
      );
    }
  };

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 flex items-center justify-between border-b border-[#253047] pb-3">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-cyan-400">My Reservations</h1>
            <p className="text-sm text-gray-300">
              View current and past reservations, and cancel upcoming ones.
            </p>
          </div>
          <Link
            to="/reservation"
            className="rounded-lg bg-cyan-500 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-400"
          >
            New Reservation
          </Link>
        </header>

        {reservationError && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {reservationError}
          </div>
        )}
        {actionError && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {actionError}
          </div>
        )}
        {actionMessage && (
          <div className="mb-3 rounded-xl border border-green-500/70 bg-green-500/20 px-4 py-2 text-sm text-green-200">
            {actionMessage}
          </div>
        )}

        <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
          {isLoadingReservations ? (
            <div className="rounded-xl border border-[#2b3b55] bg-[#14233d] px-4 py-3.5 text-sm text-gray-300">
              Loading reservations...
            </div>
          ) : reservations.length === 0 ? (
            <div className="rounded-xl border border-[#2b3b55] bg-[#14233d] px-4 py-3.5 text-sm text-gray-300">
              No reservations found yet.
            </div>
          ) : (
            <div className="space-y-3">
              {reservations.map((reservation) => {
                const isCancelable = reservation.status.toUpperCase() !== "CANCELLED";
                const isCanceling = cancelingReservationIds.includes(reservation.reservationId);

                return (
                  <div
                    key={reservation.reservationId}
                    className="rounded-xl border border-[#2b3b55] bg-[#14233d] px-4 py-3.5"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-lg font-semibold">
                          Reservation #{reservation.reservationId}
                        </p>
                        <p className="text-sm text-gray-300">
                          Vehicle #{reservation.vehicleId} - {reservation.city}
                        </p>
                        <p className="text-xs text-gray-400">
                          From ({reservation.startLocation.latitude.toFixed(4)},{" "}
                          {reservation.startLocation.longitude.toFixed(4)}) to (
                          {reservation.endLocation.latitude.toFixed(4)},{" "}
                          {reservation.endLocation.longitude.toFixed(4)})
                        </p>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <p
                          className={`rounded-md px-3 py-1 text-xs font-semibold ${statusBadgeClass(reservation.status)}`}
                        >
                          {reservation.status}
                        </p>
                        <button
                          type="button"
                          onClick={() => void handleStartTrip(reservation.reservationId)}
                          disabled={startingReservationIds.includes(reservation.reservationId)}
                          className="rounded-md bg-cyan-600 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-cyan-500 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {startingReservationIds.includes(reservation.reservationId) ? "Starting..." : "Start Trip"}
                        </button>
                        {isCancelable && (
                          <button
                            type="button"
                            onClick={() => void handleCancelReservation(reservation.reservationId)}
                            disabled={isCanceling}
                            className="rounded-md border border-red-400 px-2.5 py-1 text-xs font-semibold text-red-300 transition hover:bg-red-500/20 disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            {isCanceling ? "Canceling..." : "Cancel"}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </main>
    </>
  );
}

async function readErrorMessage(
  response: Response,
  fallbackMessage: string,
): Promise<string> {
  try {
    const data = (await response.json()) as {
      message?: string;
      error?: string;
    };

    if (typeof data.message === "string" && data.message.trim().length > 0) {
      return data.message;
    }
    if (typeof data.error === "string" && data.error.trim().length > 0) {
      return data.error;
    }
  } catch {
    // Ignore parse errors and use fallback.
  }

  return `${fallbackMessage} (${response.status})`;
}

function statusBadgeClass(status: string): string {
  const normalizedStatus = status.toUpperCase();

  if (normalizedStatus === "CONFIRMED") {
    return "bg-cyan-500/25 text-cyan-300";
  }
  if (normalizedStatus === "CANCELLED") {
    return "bg-red-500/25 text-red-300";
  }
  if (normalizedStatus === "ACTIVE") {
    return "bg-green-500/25 text-green-300";
  }
  return "bg-gray-500/25 text-gray-200";
}
