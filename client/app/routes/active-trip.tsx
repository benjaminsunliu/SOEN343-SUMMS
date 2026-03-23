import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { getAuthUser } from "../utils/auth";
import { clearActiveTrip, getActiveTrip, setActiveTrip } from "../utils/trips";
import ActiveTrip, { type ActiveTripViewModel } from "../components/active-trip";
import type { Route } from "./+types/active-trip";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Active Trip | SUMMS" },
    { name: "description", content: "Live rental status and controls." },
  ];
}

interface TripApiResponse extends ActiveTripViewModel {
  endTime: string | null;
  totalDurationMinutes: number | null;
}

export default function ActiveTripPage() {
  const navigate = useNavigate();
  const authUser = useMemo(() => getAuthUser(), []);
  const localActiveTrip = useMemo(() => getActiveTrip(), []);

  const [trip, setTrip] = useState<ActiveTripViewModel | null>(null);
  const [latitude, setLatitude] = useState("45.50");
  const [longitude, setLongitude] = useState("-73.57");
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
        const payload = (await response.json()) as TripApiResponse | { message?: string };

        if (!response.ok) {
          const errorMessage = "message" in payload && payload.message
            ? payload.message
            : "No active trip found.";
          throw new Error(errorMessage);
        }

        const activeTrip = payload as TripApiResponse;
        setTrip({
          tripId: activeTrip.tripId,
          vehicleId: activeTrip.vehicleId,
          citizenId: activeTrip.citizenId,
          startTime: activeTrip.startTime,
          vehicleStatus: activeTrip.vehicleStatus,
        });

        setActiveTrip({
          tripId: activeTrip.tripId,
          vehicleId: activeTrip.vehicleId,
          citizenId: activeTrip.citizenId,
          startTime: activeTrip.startTime,
        });
      } catch (caught) {
        const messageToShow = caught instanceof Error ? caught.message : "No active trip found.";
        setError(messageToShow);
      } finally {
        setIsLoading(false);
      }
    }

    void loadActiveTrip();
  }, [authUser]);

  async function handleEndTrip(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const sourceTrip = trip ?? localActiveTrip;
    if (!sourceTrip) {
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

    setIsEnding(true);
    setError(null);
    setMessage(null);

    try {
      const response = await apiFetch(`/api/trips/${sourceTrip.tripId}/end`, {
        method: "POST",
        body: JSON.stringify({
          dropOffLocation: {
            latitude: parsedLatitude,
            longitude: parsedLongitude,
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
      setMessage("Trip ended successfully.");
      navigate(`/trips/summary/${endedTrip.tripId}`);
    } catch (caught) {
      const messageToShow = caught instanceof Error ? caught.message : "Unable to end trip.";
      setError(messageToShow);
    } finally {
      setIsEnding(false);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Active Trip</h1>

        {isLoading && <p className="text-gray-400">Loading active trip...</p>}

        {!isLoading && trip && (
          <ActiveTrip
            trip={trip}
            latitude={latitude}
            longitude={longitude}
            isEnding={isEnding}
            onLatitudeChange={setLatitude}
            onLongitudeChange={setLongitude}
            onEndTrip={handleEndTrip}
          />
        )}

        {!isLoading && !trip && (
          <p className="text-gray-400">No active trip found.</p>
        )}

        <p className="mt-4 text-gray-400">
          Valid drop-off zones are simulated near Montreal Downtown, Verdun, and Plateau.
        </p>

        {message && <p className="mt-4 text-green-300">{message}</p>}
        {error && <p className="mt-4 text-red-300">{error}</p>}
      </main>
    </>
  );
}
