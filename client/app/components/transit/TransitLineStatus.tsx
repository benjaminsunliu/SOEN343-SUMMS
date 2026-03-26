import type { TransitLineStatus } from "../../utils/api";

const STATUS_CONFIG = {
  ON_TIME:   { label: "On Time",   cls: "bg-green-500/15 text-green-400 border-green-500/30"  },
  DELAYED:   { label: "Delayed",   cls: "bg-amber-500/15 text-amber-400 border-amber-500/30"   },
  DISRUPTED: { label: "Disrupted", cls: "bg-red-500/15 text-red-400 border-red-500/30"        },
};

const TYPE_ICON: Record<string, string> = {
  METRO: "M", BUS: "B", REM: "R", TRAIN: "T",
};

export default function TransitLineStatusPanel({
  statuses, loading,
}: { statuses: TransitLineStatus[]; loading: boolean }) {
  return (
    <div>
      <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-3">
        Live Line Status
      </p>

      {loading && (
        <div className="space-y-2">
          {[1,2,3].map(i => (
            <div key={i}
              className="h-12 rounded-lg bg-gray-800 animate-pulse" />
          ))}
        </div>
      )}

      {!loading && (
        <div className="space-y-2">
          {statuses.map(line => {
            const cfg = STATUS_CONFIG[line.status] ?? STATUS_CONFIG.ON_TIME;
            return (
              <div key={line.lineId}
                className="flex items-center gap-3 bg-gray-800/50 border border-gray-700
                            rounded-lg px-3 py-2">

                {/* Coloured line badge */}
                <div className="w-8 h-8 rounded-md flex items-center justify-center
                                text-white text-xs font-bold shrink-0"
                  style={{ backgroundColor: line.lineColor }}>
                  {TYPE_ICON[line.type] ?? "?"}
                </div>

                <div className="flex-1 min-w-0">
                  <p className="text-white text-xs font-semibold leading-tight truncate">
                    {line.lineNumber} · {line.lineName}
                  </p>
                  <p className="text-gray-500 text-xs truncate">{line.statusMessage}</p>
                </div>

                <span className={`text-xs px-2 py-0.5 rounded-full border font-semibold shrink-0 ${cfg.cls}`}>
                  {cfg.label}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}