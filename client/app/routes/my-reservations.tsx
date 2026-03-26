import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { SiteNav } from "../root";
import {
  apiFetch,
  cancelParkingReservation,
  listParkingReservations,
  type ParkingReservationResponse,
} from "../utils/api";
import type { Route } from "./+types/my-reservations";

interface ReservationLocation {
  latitude: number;
  longitude: number;
}

interface VehicleReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
  endDate: string;
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

type ReservationType = "VEHICLE" | "PARKING";

type ReservationCard =
  | (VehicleReservationResponse & { reservationType: "VEHICLE" })
  | (ParkingReservationResponse & { reservationType: "PARKING" });

export function meta({}: Route.MetaArgs) {
  return [
    { title: "My Reservations | SUMMS" },
    { name: "description", content: "View and manage your reservations." },
  ];
}

export default function MyReservationsPage() {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState<ReservationCard[]>([]);
  const [tripDetailsByReservationId, setTripDetailsByReservationId] = useState<
    Record<number, ReservationTripDetails>
  >({});
  const [isLoadingReservations, setIsLoadingReservations] = useState(true);
  const [cancelingReservationKeys, setCancelingReservationKeys] = useState<string[]>([]);
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
        const [vehicleResponse, parkingReservations] = await Promise.all([
          apiFetch("/api/reservations"),
          listParkingReservations().catch(() => []),
        ]);

        if (!vehicleResponse.ok) {
          const message = await readErrorMessage(
            vehicleResponse,
            "Unable to load reservations.",
          );
          throw new Error(message);
        }

        const vehicleReservations =
          (await vehicleResponse.json()) as VehicleReservationResponse[];
        if (!isMounted) {
          return;
        }

        const mergedReservations: ReservationCard[] = [
          ...vehicleReservations.map((reservation) => ({
            ...reservation,
            reservationType: "VEHICLE" as const,
          })),
          ...parkingReservations.map((reservation) => ({
            ...reservation,
            reservationType: "PARKING" as const,
          })),
        ].sort((a, b) => reservationSortTimestamp(b) - reservationSortTimestamp(a));

        setReservations(mergedReservations);
        setReservationError(null);
        setIsLoadingReservations(false);

        const completedVehicleReservations = mergedReservations.filter(
          (reservation): reservation is VehicleReservationResponse & { reservationType: "VEHICLE" } =>
            isVehicleReservation(reservation) && reservation.status.toUpperCase() === "COMPLETED",
        );

        if (completedVehicleReservations.length === 0) {
          setTripDetailsByReservationId({});
        } else {
          const tripEntries = await Promise.all(
            completedVehicleReservations.map(async (reservation) => {
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

  const handleCancelReservation = async (reservation: ReservationCard) => {
    setActionError(null);
    setActionMessage(null);
    const cancelKey = buildReservationKey(
      reservation.reservationId,
      reservation.reservationType,
    );
    setCancelingReservationKeys((previousKeys) => [...previousKeys, cancelKey]);

    try {
      if (reservation.reservationType === "PARKING") {
        await cancelParkingReservation(reservation.reservationId);
      } else {
        const response = await apiFetch(`/api/reservations/${reservation.reservationId}`, {
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
      }

      setReservations((previousReservations) =>
        previousReservations.map((existingReservation) =>
          existingReservation.reservationId === reservation.reservationId &&
          existingReservation.reservationType === reservation.reservationType
            ? { ...existingReservation, status: "CANCELLED" }
            : existingReservation,
        ),
      );

      const reservationLabel =
        reservation.reservationType === "PARKING"
          ? "Parking reservation"
          : "Reservation";
      setActionMessage(`${reservationLabel} #${reservation.reservationId} has been cancelled.`);
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : reservation.reservationType === "PARKING"
            ? "Network error while canceling parking reservation."
            : "Network error while canceling reservation.";
      setActionError(message);
    } finally {
      setCancelingReservationKeys((previousKeys) =>
        previousKeys.filter((existingKey) => existingKey !== cancelKey),
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
              View current and past vehicle + parking reservations, and cancel upcoming ones.
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
                const canStartTrip =
                  reservation.reservationType === "VEHICLE" &&
                  normalizedStatus === "CONFIRMED";
                const canCancel = normalizedStatus === "PENDING" || normalizedStatus === "CONFIRMED";
                const isCompleted =
                  reservation.reservationType === "VEHICLE" &&
                  normalizedStatus === "COMPLETED";
                const isCanceling = cancelingReservationKeys.includes(
                  buildReservationKey(reservation.reservationId, reservation.reservationType),
                );
                const tripDetails =
                  reservation.reservationType === "VEHICLE"
                    ? tripDetailsByReservationId[reservation.reservationId]
                    : null;

                return (
                  <div
                    key={buildReservationKey(
                      reservation.reservationId,
                      reservation.reservationType,
                    )}
                    className="rounded-xl border border-[#2b3b55] bg-[#14233d] px-4 py-3.5"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-lg font-semibold">
                          {reservation.reservationType === "PARKING"
                            ? "Parking Reservation"
                            : "Reservation"} #{reservation.reservationId}
                        </p>

                        {isVehicleReservation(reservation) ? (
                          <>
                            <p className="text-sm text-gray-300">
                              Vehicle #{reservation.vehicleId} - {reservation.city}
                            </p>
                            <p className="text-xs text-gray-400">
                              From: {getLocationDisplayLabel(reservation.startLocation)}
                            </p>
                            <p className="text-xs text-gray-400">
                              To: {getLocationDisplayLabel(reservation.endLocation)}
                            </p>
                          </>
                        ) : (
                          <>
                            <p className="text-sm text-gray-300">
                              {reservation.facilityName} - {reservation.city}
                            </p>
                            <p className="text-xs text-gray-400">
                              Address: {reservation.facilityAddress || "N/A"}
                            </p>
                            <p className="text-xs text-gray-400">
                              Arrival: {formatParkingDateTime(reservation.arrivalDate, reservation.arrivalTime)}
                            </p>
                            <p className="text-xs text-gray-400">
                              Duration: {reservation.durationHours}h
                            </p>
                            <p className="text-xs text-gray-400">
                              Total: ${reservation.totalCost?.toFixed(2) ?? "0.00"}
                            </p>
                          </>
                        )}

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
                            onClick={() => void handleCancelReservation(reservation)}
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

function formatParkingDateTime(arrivalDate: string, arrivalTime: string): string {
  const mergedDateTime = `${arrivalDate}T${arrivalTime}`;
  const parsed = new Date(mergedDateTime);
  if (Number.isNaN(parsed.getTime())) {
    return `${arrivalDate} ${arrivalTime}`;
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

function reservationSortTimestamp(reservation: ReservationCard): number {
  if (isVehicleReservation(reservation)) {
    const timestamp = Date.parse(reservation.startDate);
    if (Number.isFinite(timestamp)) {
      return timestamp;
    }

    const fallbackTimestamp = Date.parse(reservation.endDate);
    if (Number.isFinite(fallbackTimestamp)) {
      return fallbackTimestamp;
    }

    return reservation.reservationId;
  }

  const parkingConfirmedTimestamp = parseParkingConfirmedAt(reservation.confirmedAt);
  if (Number.isFinite(parkingConfirmedTimestamp)) {
    return parkingConfirmedTimestamp;
  }

  const parkingTimestamp = Date.parse(`${reservation.arrivalDate}T${reservation.arrivalTime}`);
  if (Number.isFinite(parkingTimestamp)) {
    return parkingTimestamp;
  }

  return reservation.reservationId;
}

function parseParkingConfirmedAt(confirmedAt: string): number {
  if (!confirmedAt) {
    return Number.NaN;
  }

  const isoLikeValue = confirmedAt.includes("T")
    ? confirmedAt
    : confirmedAt.replace(" ", "T");
  return Date.parse(isoLikeValue);
}

function isVehicleReservation(
  reservation: ReservationCard,
): reservation is VehicleReservationResponse & { reservationType: "VEHICLE" } {
  return reservation.reservationType === "VEHICLE";
}

function buildReservationKey(reservationId: number, reservationType: ReservationType): string {
  return `${reservationType}:${reservationId}`;
}
