import type { TransitRoute } from "../../utils/api";
import TransitRouteCard from "./TransitRouteCard";

interface Props {
  routes:      TransitRoute[];
  loading:     boolean;
  error:       string | null;
  searched:    boolean;
  origin:      string;
  destination: string;
}

export default function TransitResultsList({ routes, loading, error, searched, origin, destination }: Props) {

  if (loading) return (
    <div className="space-y-3">
      {[1,2,3].map(i => (
        <div key={i} className="h-36 rounded-xl bg-gray-800 animate-pulse" />
      ))}
    </div>
  );

  if (error) return (
    <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/30
                    rounded-xl p-4 text-red-400 text-sm">
      ⚠️ {error}
    </div>
  );

  if (!searched) return (
    <div className="flex flex-col items-center justify-center h-64 text-center">
      <p className="text-4xl mb-3">🚇</p>
      <p className="text-gray-400 text-sm">Enter your origin and destination to find routes.</p>
    </div>
  );

  if (routes.length === 0) return (
    <div className="flex flex-col items-center justify-center h-64 text-center">
      <p className="text-4xl mb-3">🔍</p>
      <p className="text-gray-400 text-sm">No routes found. Try different filters.</p>
    </div>
  );

  return (
    <div>
      <div className="bg-gray-800/50 border border-gray-700 rounded-xl px-4 py-3 mb-4">
        <p className="text-white text-sm font-medium">
          {routes.length} step{routes.length !== 1 ? "s" : ""} found
          {origin && destination && (
            <span className="text-gray-400">
              {` from `}<span className="text-white">{origin}</span>
              {` to `}<span className="text-white">{destination}</span>
            </span>
          )}
        </p>
      </div>

      <div className="space-y-3">
        {routes.map(route => (
          <TransitRouteCard key={route.routeId} route={route} />
        ))}
      </div>
    </div>
  );
}