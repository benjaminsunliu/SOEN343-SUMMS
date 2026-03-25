import { useEffect, useMemo, useState } from "react";
import type { DropOffOption } from "../utils/drop-off-options";

export interface ActiveTripViewModel {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  vehicleStatus: string;
}

interface ActiveTripProps {
  trip: ActiveTripViewModel;
  selectedDropOffKey: string;
  dropOffOptions: DropOffOption[];
  isEnding: boolean;
  onSelectedDropOffKeyChange: (value: string) => void;
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
  selectedDropOffKey,
  dropOffOptions,
  isEnding,
  onSelectedDropOffKeyChange,
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
    <section className="max-w-2xl space-y-4">
      {/* Trip Info Card */}
      <div className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
        <h3 className="border-b border-[#2a354a] pb-3 text-lg font-semibold text-cyan-400 mb-3">
          Current Trip Status
        </h3>
        <div className="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
          <div className="rounded-lg bg-gray-900 p-3">
            <p className="text-xs uppercase tracking-widest text-gray-400">Trip ID</p>
            <p className="text-lg font-semibold text-white">{trip.tripId}</p>
          </div>
          <div className="rounded-lg bg-gray-900 p-3">
            <p className="text-xs uppercase tracking-widest text-gray-400">Vehicle ID</p>
            <p className="text-lg font-semibold text-white">{trip.vehicleId}</p>
          </div>
          <div className="rounded-lg bg-gray-900 p-3">
            <p className="text-xs uppercase tracking-widest text-gray-400">Status</p>
            <p className="text-lg font-semibold text-cyan-300">{trip.vehicleStatus}</p>
          </div>
          <div className="rounded-lg bg-gray-900 p-3">
            <p className="text-xs uppercase tracking-widest text-gray-400">Elapsed</p>
            <p className="text-lg font-semibold text-white font-mono">{elapsedText}</p>
          </div>
        </div>
      </div>

      {/* Drop-off Location Form */}
      <form className="space-y-4 rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4" onSubmit={onEndTrip}>
        <h3 className="text-lg font-semibold text-cyan-400">End Trip</h3>
        
        <div>
          <label className="block text-sm uppercase tracking-widest text-gray-300 mb-2" htmlFor="active-dropoff-select">
            Drop-off Location
          </label>
          <select
            id="active-dropoff-select"
            value={selectedDropOffKey}
            onChange={(event) => onSelectedDropOffKeyChange(event.target.value)}
            disabled={isEnding || dropOffOptions.length === 0}
            className="w-full rounded-lg border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-white outline-none disabled:opacity-50"
          >
            <option value="">
              {dropOffOptions.length === 0
                ? "No drop-off options available"
                : "Select a valid drop-off option"}
            </option>
            {dropOffOptions.map((option) => (
              <option
                key={option.key}
                value={option.key}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <button
          type="submit"
          disabled={isEnding}
          className="w-full rounded-lg bg-cyan-600 text-white px-4 py-2.5 font-semibold transition hover:bg-cyan-500 disabled:opacity-50"
        >
          {isEnding ? "Ending Trip..." : "End Trip"}
        </button>
      </form>
    </section>
  );
}
