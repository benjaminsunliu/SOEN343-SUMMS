import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { buildDropOffOptions, type DropOffOption } from "../utils/drop-off-options";
import { getAuthUser } from "../utils/auth";
import { clearActiveTrip, setActiveTrip } from "../utils/trips";
import ActiveTrip, { type ActiveTripViewModel } from "../components/active-trip-panel";
import type { Route } from "./+types/active-trip";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Active Trip | SUMMS" },
    { name: "description", content: "Live rental status and controls." },
  ];
}

interface TripApiResponse extends ActiveTripViewModel {
  reservationId: number;
  endTime: string | null;
  totalDurationMinutes: number | null;
}

interface ReservationDetailsResponse {
  city: string;
  endLocation: {
    latitude: number;
    longitude: number;
  };
}

export default function ActiveTripPage() {
  const navigate = useNavigate();
  const authUser = useMemo(() => getAuthUser(), []);

  const [trip, setTrip] = useState<ActiveTripViewModel | null>(null);
  const [dropOffOptions, setDropOffOptions] = useState<DropOffOption[]>(
    buildDropOffOptions(null, null),
  );
  const [selectedDropOffKey, setSelectedDropOffKey] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEnding, setIsEnding] = useState(false);

  useEffect(() => {
    async function loadActiveTrip() {
      if (!authUser) {
        setError("You must be logged in to view active trips.");
        setIsLoading(false);
        return;
      }

      try {
        const response = await apiFetch(`/api/trips/active/${authUser.id}`);
        if (!response.ok) {
          setIsLoading(false);
          return;
        }

        const activeTrip = (await response.json()) as TripApiResponse;
        const tripData: ActiveTripViewModel = {
          tripId: activeTrip.tripId,
          vehicleId: activeTrip.vehicleId,
          citizenId: activeTrip.citizenId,
          startTime: activeTrip.startTime,
          vehicleStatus: activeTrip.vehicleStatus,
        };
        setTrip(tripData);
        setActiveTrip({
          tripId: activeTrip.tripId,
          reservationId: activeTrip.reservationId,
          vehicleId: activeTrip.vehicleId,
          citizenId: activeTrip.citizenId,
          startTime: activeTrip.startTime,
        });

        const options = await buildDropOffOptionsForReservation(activeTrip.reservationId);
        if (isMounted) {
          setDropOffOptions(options);
          setSelectedDropOffKey("");
        }
      } catch {
        // No active trip
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    let isMounted = true;
    void loadActiveTrip();
    return () => {
      isMounted = false;
    };
  }, [authUser]);

  async function handleEndTrip(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!trip) {
      setError("No active trip found.");
      return;
    }

    const selectedOption = findDropOffOptionByKey(
      selectedDropOffKey,
      dropOffOptions,
    );
    if (!selectedOption) {
      setError("Please select a valid drop-off option from the dropdown.");
      return;
    }

    setIsEnding(true);
    setError(null);
    setMessage(null);

    try {
      const response = await apiFetch(`/api/trips/${trip.tripId}/end`, {
        method: "POST",
        body: JSON.stringify({
          dropOffLocation: {
            latitude: selectedOption.latitude,
            longitude: selectedOption.longitude,
          },
        }),
      });

      const payload = (await response.json()) as TripApiResponse | { message?: string };
      if (!response.ok) {
        const errorMessage = "message" in payload && payload.message
          ? payload.message
          : "Unable to end trip.";
        throw new Error(errorMessage);
      }

      const endedTrip = payload as TripApiResponse;
      clearActiveTrip();
      navigate(`/trips/summary/${endedTrip.tripId}`);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Unable to end trip.");
    } finally {
      setIsEnding(false);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Active Trip</h1>
        </header>

        {isLoading && (
          <div className="rounded-xl border border-gray-700 bg-gray-950 p-5 text-center">
            <p className="text-gray-300">Loading...</p>
          </div>
        )}

        {!isLoading && !trip && (
          <div className="rounded-xl border border-amber-500/70 bg-amber-500/20 px-5 py-4">
            <p className="text-amber-200">
              No active trip found. Go to{" "}
              <a href="/my-reservations" className="underline text-amber-100 hover:text-white">My Reservations</a>{" "}
              and click "Start Trip" on a confirmed reservation.
            </p>
          </div>
        )}

        {!isLoading && trip && (
          <>
            <section className="mb-5 space-y-4">
              <ActiveTrip
                trip={trip}
                dropOffOptions={dropOffOptions}
                selectedDropOffKey={selectedDropOffKey}
                isEnding={isEnding}
                onSelectedDropOffKeyChange={setSelectedDropOffKey}
                onEndTrip={handleEndTrip}
              />

              <div className="rounded-xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
                <h3 className="text-base font-semibold text-gray-300 mb-2">Valid Drop-off Zones</h3>
                <p className="text-sm text-gray-400">
                  End trips only using the dropdown options above.
                </p>
                <ul className="mt-2 text-sm text-gray-400 list-disc list-inside space-y-1">
                  <li>Reserved destination from your reservation</li>
                  <li>Montreal Downtown drop-off zone</li>
                  <li>Verdun drop-off zone</li>
                  <li>Plateau drop-off zone</li>
                </ul>
              </div>
            </section>

            {message && (
              <div className="mb-5 rounded-xl border border-green-500/70 bg-green-500/20 px-4 py-3 text-sm text-green-200">
                {message}
              </div>
            )}
            {error && (
              <div className="mb-5 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-3 text-sm text-red-200">
                {error}
              </div>
            )}
          </>
        )}
      </main>
    </>
  );
}

async function buildDropOffOptionsForReservation(
  reservationId: number,
): Promise<DropOffOption[]> {
  try {
    const reservationResponse = await apiFetch(`/api/reservations/${reservationId}`);
    if (!reservationResponse.ok) {
      return buildDropOffOptions(null, null);
    }

    const reservation = (await reservationResponse.json()) as ReservationDetailsResponse;
    return buildDropOffOptions(reservation.endLocation, reservation.city);
  } catch {
    return buildDropOffOptions(null, null);
  }
}

function findDropOffOptionByKey(
  key: string,
  options: DropOffOption[],
): DropOffOption | null {
  if (key.trim().length === 0) {
    return null;
  }

  return options.find(
    (option) => option.key === key,
  ) ?? null;
}
