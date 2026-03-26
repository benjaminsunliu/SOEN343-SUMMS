import { useState, useCallback } from "react";
import type { Route } from "./+types/parking";
import {
  searchParking,
  type ParkingFacility,
  type ParkingSearchParams,
} from "../utils/api";
import ParkingSearchForm from "../components/parking/ParkingSearchForm";
import ParkingResultsList from "../components/parking/ParkingResultsList";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Parking | SUMMS" },
    {
      name: "description",
      content: "Find and reserve parking near your destination.",
    },
  ];
}

function useParkingSearch() {
  const [results, setResults] = useState<ParkingFacility[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);
  const [destination, setDest] = useState("");
  const [duration, setDuration] = useState(6);

  const search = useCallback(async (params: ParkingSearchParams) => {
    setLoading(true);
    setError(null);
    setSearched(true);
    setDest(params.destination);
    setDuration(params.durationHours);

    try {
      const data = await searchParking(params);
      setResults(data);
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Parking service unavailable.";
      setError(msg);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const reset = useCallback(() => {
    setResults([]);
    setSearched(false);
    setError(null);
    setDest("");
    setDuration(6);
  }, []);

  return { results, loading, error, searched, destination, duration, search, reset };
}

export default function ParkingPage() {
  const { results, loading, error, searched, destination, duration, search, reset } =
    useParkingSearch();

  return (
    <div className="min-h-screen bg-gray-900 px-5 py-4 text-white">
      <div className="px-7 py-5 border-b border-gray-800">
        <h1 className="text-xl font-bold text-white">Parking Finder</h1>
      </div>

      <div className="flex flex-1 overflow-hidden">
        <aside className="w-72 shrink-0 p-5 border-r border-gray-800 overflow-y-auto">
          <ParkingSearchForm onSearch={search} onReset={reset} loading={loading} />
        </aside>

        <main className="flex-1 overflow-y-auto p-5">
          <ParkingResultsList
            results={results}
            loading={loading}
            error={error}
            searched={searched}
            destination={destination}
            durationHours={duration}
          />
        </main>
      </div>
    </div>
  );
}
