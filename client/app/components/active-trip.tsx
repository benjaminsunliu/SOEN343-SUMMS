import { useEffect, useMemo, useState } from "react";

export interface ActiveTripViewModel {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  vehicleStatus: string;
}

interface ActiveTripProps {
  trip: ActiveTripViewModel;
  latitude: string;
  longitude: string;
  isEnding: boolean;
  onLatitudeChange: (value: string) => void;
  onLongitudeChange: (value: string) => void;
  onEndTrip: (event: React.FormEvent<HTMLFormElement>) => void;
}

function formatElapsed(totalSeconds: number): string {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  return [hours, minutes, seconds].map((value) => String(value).padStart(2, "0")).join(":");
}

export default function ActiveTrip({
  trip,
  latitude,
  longitude,
  isEnding,
  onLatitudeChange,
  onLongitudeChange,
  onEndTrip,
}: ActiveTripProps) {
  const [nowMs, setNowMs] = useState(() => Date.now());

  useEffect(() => {
    const interval = window.setInterval(() => {
      setNowMs(Date.now());
    }, 1000);

    return () => window.clearInterval(interval);
  }, []);

  const elapsedText = useMemo(() => {
    const startedAtMs = Date.parse(trip.startTime);
    if (!Number.isFinite(startedAtMs)) {
      return "00:00:00";
    }

    const elapsedSeconds = Math.max(0, Math.floor((nowMs - startedAtMs) / 1000));
    return formatElapsed(elapsedSeconds);
  }, [nowMs, trip.startTime]);

  return (
    <section className="max-w-2xl space-y-4 bg-gray-950 border border-gray-800 rounded-xl p-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
        <p className="text-gray-300">Trip ID: <span className="text-white font-semibold">{trip.tripId}</span></p>
        <p className="text-gray-300">Vehicle ID: <span className="text-white font-semibold">{trip.vehicleId}</span></p>
        <p className="text-gray-300">Status: <span className="text-cyan-300 font-semibold">{trip.vehicleStatus}</span></p>
        <p className="text-gray-300">Live Timer: <span className="text-white font-semibold">{elapsedText}</span></p>
      </div>

      <form className="space-y-3" onSubmit={onEndTrip}>
        <label className="block text-sm text-gray-300" htmlFor="active-dropoff-latitude">
          Drop-off Latitude
        </label>
        <input
          id="active-dropoff-latitude"
          type="number"
          step="any"
          value={latitude}
          onChange={(event) => onLatitudeChange(event.target.value)}
          disabled={isEnding}
          className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
        />

        <label className="block text-sm text-gray-300" htmlFor="active-dropoff-longitude">
          Drop-off Longitude
        </label>
        <input
          id="active-dropoff-longitude"
          type="number"
          step="any"
          value={longitude}
          onChange={(event) => onLongitudeChange(event.target.value)}
          disabled={isEnding}
          className="w-full rounded-lg border border-gray-700 bg-gray-900 text-white p-2"
        />

        <button
          type="submit"
          disabled={isEnding}
          className="rounded-lg bg-cyan-600 text-white px-4 py-2 disabled:opacity-50"
        >
          {isEnding ? "Ending..." : "End Trip"}
        </button>
      </form>
    </section>
  );
}
