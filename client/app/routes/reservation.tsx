import { SiteNav } from "../root";
import { useEffect, useMemo, useState } from "react";
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
  reservationId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
}

interface VehicleOption {
  id: number;
  type: string;
  status: string;
  providerId: number;
  costPerMinute: number;
  location: {
    latitude: number;
    longitude: number;
  };
}

interface ReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
  endDate: string;
  startLocation: {
    latitude: number;
    longitude: number;
  };
  endLocation: {
    latitude: number;
    longitude: number;
  };
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString();
}

export default function ReservationPage() {
  const navigate = useNavigate();
  const authUser = useMemo(() => getAuthUser(), []);
  const [vehicles, setVehicles] = useState<VehicleOption[]>([]);
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isReserving, setIsReserving] = useState(false);
  const [startingReservationId, setStartingReservationId] = useState<number | null>(null);

  const canStartTrip = authUser?.role === "CITIZEN" || authUser?.role === "ADMIN";

  const availableVehicles = vehicles.filter((vehicle) => vehicle.status === "AVAILABLE");
  const reservableVehicleOptions = availableVehicles.sort((left, right) => left.id - right.id);
  const startableReservations = reservations
    .filter((reservation) => reservation.status === "CONFIRMED")
    .sort((left, right) => Date.parse(left.endDate) - Date.parse(right.endDate));

  useEffect(() => {
    async function loadReservationContext() {
      if (!authUser || !canStartTrip) {
        setIsLoading(false);
        return;
      }

      try {
        const [vehiclesResponse, reservationsResponse] = await Promise.all([
          apiFetch("/api/vehicles"),
          apiFetch("/api/reservations"),
        ]);

        const vehiclesPayload = (await vehiclesResponse.json()) as
          | VehicleOption[]
          | { message?: string };
        const reservationsPayload = (await reservationsResponse.json()) as
          | ReservationResponse[]
          | { message?: string };

        if (!vehiclesResponse.ok) {
          throw new Error(
            "message" in vehiclesPayload && vehiclesPayload.message
              ? vehiclesPayload.message
              : "Unable to load vehicles.",
          );
        }

        if (!reservationsResponse.ok) {
          throw new Error(
            "message" in reservationsPayload && reservationsPayload.message
              ? reservationsPayload.message
              : "Unable to load reservations.",
          );
        }

        setVehicles(vehiclesPayload as VehicleOption[]);
        setReservations(reservationsPayload as ReservationResponse[]);
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : "Unable to load reservations.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadReservationContext();
  }, [authUser, canStartTrip]);

  async function handleReserveVehicle(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!authUser) {
      setError("You must be logged in to reserve a vehicle.");
      setMessage(null);
      return;
    }

    const vehicleId = Number(selectedVehicleId);
    const vehicle = vehicles.find((candidate) => candidate.id === vehicleId);

    if (!vehicle) {
      setError("Please choose an available vehicle.");
      setMessage(null);
      return;
    }

    setIsReserving(true);
    setError(null);
    setMessage(null);

    try {
      const now = Date.now();
      const response = await apiFetch(`/api/vehicles/${vehicleId}/reservations`, {
        method: "POST",
        body: JSON.stringify({
          city: "Montreal",
          startLocation: vehicle.location,
          endLocation: vehicle.location,
          startDate: new Date(now).toISOString(),
          endDate: new Date(now + 15 * 60 * 1000).toISOString(),
        }),
      });

      const payload = (await response.json()) as ReservationResponse | { message?: string };
      if (!response.ok) {
        const errorMessage = "message" in payload && payload.message
          ? payload.message
          : "Unable to reserve vehicle.";
        throw new Error(errorMessage);
      }

      const reservation = payload as ReservationResponse;
      setReservations((currentReservations) => [reservation, ...currentReservations]);
      setVehicles((currentVehicles) => currentVehicles.map((candidate) => (
        candidate.id === vehicle.id
          ? { ...candidate, status: "RESERVED" }
          : candidate
      )));
      setSelectedVehicleId("");
      setMessage(
        `Vehicle #${reservation.vehicleId} reserved until ${formatDateTime(reservation.endDate)}. You can start the trip now.`,
      );
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Unable to reserve vehicle.");
    } finally {
      setIsReserving(false);
    }
  }

  async function handleStartTrip(reservation: ReservationResponse) {
    if (!authUser) {
      setError("You must be logged in to start a trip.");
      setMessage(null);
      return;
    }

    setStartingReservationId(reservation.reservationId);
    setError(null);
    setMessage(null);

    try {
      const response = await apiFetch("/api/trips/start", {
        method: "POST",
        body: JSON.stringify({
          reservationId: reservation.reservationId,
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
      navigate("/trips/active");
    } catch (caught) {
      const messageToShow = caught instanceof Error ? caught.message : "Unable to start trip.";
      setError(messageToShow);
    } finally {
      setStartingReservationId(null);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Reservation</h1>

        {!canStartTrip && (
          <p className="text-red-300 mb-4">
            Only citizen/admin accounts can reserve vehicles and start trips.
          </p>
        )}

        {isLoading && <p className="text-gray-400">Loading reservation options...</p>}

        {!isLoading && canStartTrip && (
          <section className="space-y-6 max-w-4xl">
            <form
              className="space-y-4 bg-gray-950 border border-gray-800 rounded-xl p-4"
              onSubmit={handleReserveVehicle}
            >
              <div>
                <h2 className="text-lg font-semibold text-white">Reserve a vehicle</h2>
                <p className="text-sm text-gray-400 mt-1">
                  Choose an available vehicle. Reservations are held for 15 minutes so you can reach the vehicle and start your trip.
                </p>
              </div>

              <label className="block text-sm text-gray-300" htmlFor="vehicleId">
                Available vehicles
              </label>
              <select
                id="vehicleId"
                value={selectedVehicleId}
                onChange={(event) => setSelectedVehicleId(event.target.value)}
                disabled={isReserving || reservableVehicleOptions.length === 0}
                className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
              >
                <option value="">Select a vehicle</option>
                {reservableVehicleOptions.map((vehicle) => (
                  <option key={vehicle.id} value={vehicle.id}>
                    {`#${vehicle.id} · ${vehicle.type} · $${vehicle.costPerMinute.toFixed(2)}/min`}
                  </option>
                ))}
              </select>

              <button
                type="submit"
                disabled={isReserving || !selectedVehicleId}
                className="rounded-lg bg-cyan-600 text-white px-4 py-2 disabled:opacity-50"
              >
                {isReserving ? "Reserving..." : "Reserve Vehicle"}
              </button>

              {reservableVehicleOptions.length === 0 && (
                <p className="text-sm text-gray-500">
                  No vehicles are currently available to reserve.
                </p>
              )}
            </form>

            <section className="space-y-3 bg-gray-950 border border-gray-800 rounded-xl p-4">
              <div>
                <h2 className="text-lg font-semibold text-white">Reserved vehicles</h2>
                <p className="text-sm text-gray-400 mt-1">
                  Confirmed reservations stay here until they expire or you start the trip.
                </p>
              </div>

              {startableReservations.length === 0 && (
                <p className="text-sm text-gray-500">No confirmed reservations are ready to start.</p>
              )}

              {startableReservations.map((reservation) => (
                <article
                  key={reservation.reservationId}
                  className="rounded-xl border border-gray-800 bg-gray-900/70 p-4 flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
                >
                  <div className="space-y-1 text-sm">
                    <p className="text-white font-medium">Vehicle #{reservation.vehicleId}</p>
                    <p className="text-gray-400">Reservation #{reservation.reservationId}</p>
                    <p className="text-gray-400">Status: <span className="text-amber-300 font-medium">RESERVED</span></p>
                    <p className="text-gray-400">City: <span className="text-white">{reservation.city}</span></p>
                    <p className="text-gray-400">Valid until: <span className="text-white">{formatDateTime(reservation.endDate)}</span></p>
                  </div>

                  <button
                    type="button"
                    onClick={() => void handleStartTrip(reservation)}
                    disabled={startingReservationId === reservation.reservationId}
                    className="rounded-lg bg-emerald-600 text-white px-4 py-2 disabled:opacity-50"
                  >
                    {startingReservationId === reservation.reservationId ? "Starting..." : "Start Trip"}
                  </button>
                </article>
              ))}
            </section>
          </section>
        )}

        {message && <p className="mt-4 text-green-300">{message}</p>}
        {error && <p className="mt-4 text-red-300">{error}</p>}
      </main>
    </>
  );
}
