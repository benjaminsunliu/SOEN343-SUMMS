import type { ParkingFacility, ParkingSearchParams } from "../../utils/api";
import ParkingFacilityCard from "./ParkingFacilityCard";

interface Props {
  results: ParkingFacility[];
  loading: boolean;
  error: string | null;
  searched: boolean;
  destination: string;
  durationHours: number;
  searchParams: ParkingSearchParams | null;
}

function SkeletonCard() {
  return (
    <div className="bg-gray-800 border border-gray-700 rounded-xl p-4 animate-pulse">
      <div className="flex justify-between mb-3">
        <div className="flex-1 space-y-2 mr-4">
          <div className="h-4 bg-gray-700 rounded w-3/4" />
          <div className="h-3 bg-gray-700 rounded w-1/2" />
        </div>
        <div className="space-y-2">
          <div className="h-5 bg-gray-700 rounded w-16" />
          <div className="h-3 bg-gray-700 rounded w-12" />
        </div>
      </div>
      <div className="flex gap-2 mb-3">
        <div className="h-5 bg-gray-700 rounded-full w-20" />
        <div className="h-5 bg-gray-700 rounded-full w-16" />
        <div className="h-5 bg-gray-700 rounded-full w-24" />
      </div>
      <div className="h-8 bg-gray-700 rounded-lg w-full" />
    </div>
  );
}

export default function ParkingResultsList({
  results, loading, error, searched, destination, durationHours, searchParams,
}: Props) {

  // 1 — Not yet searched
  if (!searched && !loading) {
    return (
      <div className="flex flex-col items-center justify-center h-full py-20 text-center">
        <span className="text-5xl mb-4 opacity-40">🅿️</span>
        <p className="text-gray-400 text-sm">Enter your destination to find nearby parking</p>
      </div>
    );
  }

  // 2 — Loading skeletons
  if (loading) {
    return (
      <div className="flex flex-col gap-3">
        {[1, 2, 3].map((i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  // 3 — Error
  if (error) {
    return (
      <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/30
                      rounded-xl p-4 text-red-400 text-sm">
        <span>⚠️</span> {error}
      </div>
    );
  }

  // 4 — No results
  if (results.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <span className="text-4xl mb-4 opacity-40">🔍</span>
        <p className="text-gray-400 text-sm">No parking lots found matching your criteria.</p>
        <p className="text-gray-500 text-xs mt-1">Try raising your max price or adjusting your search.</p>
      </div>
    );
  }

  // 5 — Results
  return (
    <div>
      <p className="text-gray-400 text-sm mb-3">
        <span className="text-white font-semibold">{results.length} parking lots</span>
        {destination && <> near <span className="text-white">{destination}</span></>}
      </p>
      <div className="flex flex-col gap-3">
        {results.map((facility) => (
          <ParkingFacilityCard
            key={facility.facilityId}
            facility={facility}
            durationHours={durationHours}
            searchParams={searchParams}
          />
        ))}
      </div>
    </div>
  );
}
