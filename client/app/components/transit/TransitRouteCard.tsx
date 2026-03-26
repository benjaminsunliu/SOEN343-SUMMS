import type { TransitRoute } from "../../utils/api";

const STATUS_CONFIG = {
  ON_TIME:   { label: "On Time",   cls: "bg-green-500/15 text-green-400 border-green-500/30"  },
  DELAYED:   { label: "Delayed",   cls: "bg-amber-500/15 text-amber-400 border-amber-500/30"   },
  DISRUPTED: { label: "Disrupted", cls: "bg-red-500/15 text-red-400 border-red-500/30"        },
};

const TYPE_LABEL: Record<string, string> = {
  METRO: "Metro", BUS: "Bus", REM: "REM", TRAIN: "Train",
};

export default function TransitRouteCard({ route }: { route: TransitRoute }) {
  const cfg = STATUS_CONFIG[route.status] ?? STATUS_CONFIG.ON_TIME;
  const hrs = Math.floor(route.durationMinutes / 60);
  const mins = route.durationMinutes % 60;
  const durationLabel = hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;

  return (
    <div className="bg-gray-800/50 border border-gray-700 rounded-xl p-4
                    hover:border-gray-600 transition-colors">

      {/* Top row — line badge + times + fare */}
      <div className="flex items-start justify-between gap-3 mb-3">
        <div className="flex items-center gap-3">
          {/* Coloured line pill */}
          <div className="px-2.5 py-1 rounded-lg text-white text-xs font-bold shrink-0"
            style={{ backgroundColor: route.lineColor }}>
            {route.lineNumber}
          </div>
          <div>
            <p className="text-white font-bold text-sm leading-tight">
              {route.lineName}
            </p>
            <p className="text-gray-500 text-xs">
              {TYPE_LABEL[route.type] ?? route.type}
            </p>
          </div>
        </div>

        <div className="text-right shrink-0">
          <p className="text-white font-bold text-xl">
            ${route.fare.toFixed(2)}
          </p>
          <p className="text-gray-500 text-xs">per ticket</p>
        </div>
      </div>

      {/* Times row */}
      <div className="flex items-center gap-2 mb-3">
        <span className="text-white font-bold text-sm">{route.departureTime}</span>
        <div className="flex-1 flex items-center gap-1">
          <div className="flex-1 h-px bg-gray-700" />
          <span className="text-gray-500 text-xs px-1">{durationLabel}</span>
          <div className="flex-1 h-px bg-gray-700" />
        </div>
        <span className="text-white font-bold text-sm">{route.arrivalTime}</span>
      </div>

      {/* Origin → destination */}
      <div className="flex items-center gap-2 mb-3 text-sm">
        <span className="text-gray-400 truncate">📍 {route.origin}</span>
        <span className="text-gray-600">→</span>
        <span className="text-gray-400 truncate">{route.destination}</span>
      </div>

      {/* Stops preview */}
      <div className="flex flex-wrap gap-1 mb-3">
        {route.stops.slice(0, 4).map(stop => (
          <span key={stop}
            className="text-xs px-2 py-0.5 rounded-full border border-gray-700 text-gray-400">
            {stop}
          </span>
        ))}
        {route.stops.length > 4 && (
          <span className="text-xs text-gray-600">
            +{route.stops.length - 4} more stops
          </span>
        )}
      </div>

      {/* Bottom row — transfers + status */}
      <div className="flex items-center justify-between">
        <span className="text-gray-500 text-xs">
          {route.transfers === 0 ? "Direct" : `${route.transfers} transfer${route.transfers > 1 ? "s" : ""}`}
        </span>
        <span className={`text-xs px-2.5 py-0.5 rounded-full border font-semibold ${cfg.cls}`}>
          {cfg.label}
          {route.status !== "ON_TIME" && (
            <span className="ml-1 font-normal opacity-80">
              · {route.statusMessage}
            </span>
          )}
        </span>
      </div>
    </div>
  );
}