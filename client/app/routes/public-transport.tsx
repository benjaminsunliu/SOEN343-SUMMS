import { useState, useCallback, useEffect } from "react";
import type { Route } from "./+types/public-transport";
import {
  searchTransitRoutes, fetchTransitLineStatuses,
  type TransitRoute, type TransitLineStatus, type TransitSearchParams,
} from "../utils/api";
import TransitSearchForm   from "../components/transit/TransitSearchForm";
import TransitResultsList  from "../components/transit/TransitResultsList";
import TransitLineStatusPanel from "../components/transit/TransitLineStatus";

export function meta({}: Route.MetaArgs) {
  return [{ title: "Public Transit | SUMMS" }];
}

function useTransit() {
  const [routes, setRoutes]       = useState<TransitRoute[]>([]);
  const [statuses, setStatuses]   = useState<TransitLineStatus[]>([]);
  const [searching, setSearching] = useState(false);
  const [statusLoading, setStatusLoading] = useState(true);
  const [error, setError]         = useState<string | null>(null);
  const [searched, setSearched]   = useState(false);
  const [origin, setOrigin]       = useState("");
  const [destination, setDest]    = useState("");

  // Load line statuses once on mount
  useEffect(() => {
    fetchTransitLineStatuses()
      .then(setStatuses)
      .catch(() => setStatuses([]))
      .finally(() => setStatusLoading(false));
  }, []);

  const search = useCallback(async (params: TransitSearchParams) => {
    setSearching(true); setError(null); setSearched(true);
    setOrigin(params.origin); setDest(params.destination);
    try {
      const data = await searchTransitRoutes(params);
      setRoutes(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Transit service unavailable.");
      setRoutes([]);
    } finally { setSearching(false); }
  }, []);

  const reset = useCallback(() => {
    setRoutes([]); setSearched(false); setError(null);
    setOrigin(""); setDest("");
  }, []);

  return { routes, statuses, searching, statusLoading, error, searched, origin, destination, search, reset };
}

//Page
export default function PublicTransportPage() {
  const {
    routes, statuses, searching, statusLoading,
    error, searched, origin, destination, search, reset,
  } = useTransit();

  return (
    <div className="flex flex-col h-full bg-gray-900">

      {/* Header */}
      <div className="px-7 py-5 border-b border-gray-800 shrink-0">
        <h1 className="text-xl font-bold text-white">Public Transit</h1>
      </div>

      <div className="flex flex-1 overflow-hidden">

        {/* LEFT sidebar — search form + line statuses */}
        <aside className="w-72 shrink-0 p-5 border-r border-gray-800 overflow-y-auto space-y-6">
          <TransitSearchForm
            onSearch={search}
            onReset={reset}
            loading={searching}
          />

          <div className="pt-4 border-t border-gray-800">
            <TransitLineStatusPanel
              statuses={statuses}
              loading={statusLoading}
            />
          </div>
        </aside>

        {/* RIGHT — results */}
        <main className="flex-1 overflow-y-auto p-5">
          <TransitResultsList
            routes={routes}
            loading={searching}
            error={error}
            searched={searched}
            origin={origin}
            destination={destination}
          />
        </main>
      </div>
    </div>
  );
}
