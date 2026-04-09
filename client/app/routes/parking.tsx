import { useState, useCallback, useEffect } from "react";
import type { Route } from "./+types/parking";
import {
  listActiveParkingFacilities,
  searchParking,
  type ParkingFacility,
  type ParkingSearchParams,
} from "../utils/api";
import { getBrowserLocation, type GeoLocation } from "../utils/location";
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
  const [searchParams, setSearchParams] = useState<ParkingSearchParams | null>(null);

  const applyClientFilters = useCallback(
    (facilities: ParkingFacility[], params: ParkingSearchParams) => {
      const normalizedDestination = params.destination.trim().toLowerCase();

      return facilities.filter((facility) => {
        const hasAvailability = facility.availableSpots > 0;
        if (!hasAvailability) {
          return false;
        }

        const withinPrice =
          Number.isFinite(params.maxPricePerHour) && params.maxPricePerHour > 0
            ? facility.pricePerHour <= params.maxPricePerHour
            : true;
        if (!withinPrice) {
          return false;
        }

        if (!normalizedDestination) {
          return true;
        }

        const searchableText = `${facility.name} ${facility.address} ${facility.city}`.toLowerCase();
        return searchableText.includes(normalizedDestination);
      });
    },
    [],
  );

  const loadAvailableProviderInventory = useCallback(async () => {
    setLoading(true);
    setError(null);
    setSearched(true);
    setDest("");
    setDuration(6);
    setSearchParams(null);

    try {
      const data = await listActiveParkingFacilities();
      setResults(data.filter((facility) => facility.availableSpots > 0));
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Parking service unavailable.";
      setError(msg);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const search = useCallback(async (params: ParkingSearchParams) => {
    const normalizedParams: ParkingSearchParams = {
      ...params,
      destination: params.destination.trim(),
    };

    setLoading(true);
    setError(null);
    setSearched(true);
    setDest(normalizedParams.destination);
    setDuration(normalizedParams.durationHours);
    setSearchParams(normalizedParams);

    try {
      const data = await searchParking(normalizedParams);
      setResults(applyClientFilters(data, normalizedParams));
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Parking service unavailable.";
      setError(msg);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, [applyClientFilters]);

  const reset = useCallback(() => {
    void loadAvailableProviderInventory();
  }, [loadAvailableProviderInventory]);

  useEffect(() => {
    void loadAvailableProviderInventory();
  }, [loadAvailableProviderInventory]);

  return { results, loading, error, searched, destination, duration, searchParams, search, reset };
}

export default function ParkingPage() {
  const [userLocation, setUserLocation] = useState<GeoLocation | null>(null);
  const { results, loading, error, searched, destination, duration, searchParams, search, reset } =
    useParkingSearch();

  useEffect(() => {
    let isMounted = true;

    async function locateUser() {
      try {
        const location = await getBrowserLocation();
        if (!isMounted) {
          return;
        }
        setUserLocation(location);
      } catch {
        if (!isMounted) {
          return;
        }
        setUserLocation(null);
      }
    }

    void locateUser();

    return () => {
      isMounted = false;
    };
  }, []);

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
            searchParams={searchParams}
            userLocation={userLocation}
          />
        </main>
      </div>
    </div>
  );
}
