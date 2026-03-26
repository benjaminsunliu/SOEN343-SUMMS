import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
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

interface ReservationTripDetails {
  tripId: number;
  reservationId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
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
  const [tripDetailsByReservationId, setTripDetailsByReservationId] = useState<
    Record<number, ReservationTripDetails>
  >({});
  const [isLoadingReservations, setIsLoadingReservations] = useState(true);
  const [cancelingReservationIds, setCancelingReservationIds] = useState<number[]>([]);
  const [reservationError, setReservationError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    let isRefreshing = false;

    async function loadReservations() {
      if (isRefreshing) {
        return;
      }
      isRefreshing = true;

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

        const sortedReservations = [...data].sort(
          (a, b) => b.reservationId - a.reservationId,
        );
        setReservations(sortedReservations);
        setReservationError(null);
        setIsLoadingReservations(false);

        const completedReservations = sortedReservations.filter(
          (reservation) => reservation.status.toUpperCase() === "COMPLETED",
        );

        if (completedReservations.length === 0) {
          setTripDetailsByReservationId({});
        } else {
          const tripEntries = await Promise.all(
            completedReservations.map(async (reservation) => {
              try {
                const tripResponse = await apiFetch(
                  `/api/trips/reservation/${reservation.reservationId}`,
                );
                if (!tripResponse.ok) {
                  return null;
                }

                const tripDetails =
                  (await tripResponse.json()) as ReservationTripDetails;
                return [reservation.reservationId, tripDetails] as const;
              } catch {
                return null;
              }
            }),
          );

          if (!isMounted) {
            return;
          }

          const mappedTripEntries: Record<number, ReservationTripDetails> = {};
          tripEntries.forEach((entry) => {
            if (!entry) {
              return;
            }
            mappedTripEntries[entry[0]] = entry[1];
          });
          setTripDetailsByReservationId(mappedTripEntries);
        }
      } catch (error) {
        if (isMounted) {
          setReservationError(
            error instanceof Error ? error.message : "Unable to load reservations.",
          );
          setReservations([]);
          setTripDetailsByReservationId({});
        }
      } finally {
        isRefreshing = false;
        if (isMounted) {
          setIsLoadingReservations(false);
        }
      }
    }

    void loadReservations();
    const refreshIntervalId = window.setInterval(() => {
      void loadReservations();
    }, 30_000);

    return () => {
      isMounted = false;
      window.clearInterval(refreshIntervalId);
    };
  }, []);

  const handleStartTrip = (reservationId: number) => {
    setActionError(null);
    setActionMessage(null);
    navigate(`/payment?reservationId=${reservationId}`);
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
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
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
                const normalizedStatus = reservation.status.toUpperCase();
                const canStartTrip = normalizedStatus === "CONFIRMED";
                const canCancel = normalizedStatus === "PENDING" || normalizedStatus === "CONFIRMED";
                const isCompleted = normalizedStatus === "COMPLETED";
                const isCanceling = cancelingReservationIds.includes(reservation.reservationId);
                const tripDetails = tripDetailsByReservationId[reservation.reservationId];

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
                          From: {getLocationDisplayLabel(reservation.startLocation)}
                        </p>
                        <p className="text-xs text-gray-400">
                          To: {getLocationDisplayLabel(reservation.endLocation)}
                        </p>
                        {isCompleted && (
                          <div className="mt-2 rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2 text-xs text-gray-300">
                            {tripDetails ? (
                              <>
                                <p className="font-semibold text-cyan-300">
                                  Trip #{tripDetails.tripId}
                                </p>
                                <p>
                                  Started: {formatDateTime(tripDetails.startTime)}
                                </p>
                                <p>
                                  Ended: {formatDateTime(tripDetails.endTime)}
                                </p>
                                <p>
                                  Duration: {tripDetails.totalDurationMinutes ?? 0} minutes
                                </p>
                                <Link
                                  to={`/trips/summary/${tripDetails.tripId}`}
                                  className="mt-1 inline-block text-cyan-300 underline transition hover:text-cyan-200"
                                >
                                  View full trip summary
                                </Link>
                              </>
                            ) : (
                              <p className="text-gray-400">
                                Trip details unavailable for this completed reservation.
                              </p>
                            )}
                          </div>
                        )}
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <p
                          className={`rounded-md px-3 py-1 text-xs font-semibold ${statusBadgeClass(reservation.status)}`}
                        >
                          {reservation.status}
                        </p>
                        {canStartTrip && (
                          <button
                            type="button"
                            onClick={() => handleStartTrip(reservation.reservationId)}
                            className="rounded-md bg-cyan-600 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-cyan-500"
                          >
                            Pay & Start Trip
                          </button>
                        )}
                        {canCancel && (
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

function formatDateTime(dateTime: string | null): string {
  if (!dateTime) {
    return "N/A";
  }

  const parsed = new Date(dateTime);
  if (Number.isNaN(parsed.getTime())) {
    return dateTime;
  }

  return parsed.toLocaleString();
}

function formatCoordinates(location: ReservationLocation): string {
  return `(${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)})`;
}

function getLocationDisplayLabel(location: ReservationLocation): string {
  return formatCoordinates(location);
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
  if (normalizedStatus === "EXPIRED") {
    return "bg-amber-500/25 text-amber-300";
  }
  return "bg-gray-500/25 text-gray-200";
}
