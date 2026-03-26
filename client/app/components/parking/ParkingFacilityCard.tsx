import { useNavigate } from "react-router";
import type { ParkingFacility } from "../../utils/api";

interface Props {
  facility: ParkingFacility;
  durationHours: number;
}

export default function ParkingFacilityCard({ facility, durationHours }: Props) {
  const navigate = useNavigate();

  const isAlmostFull = facility.availabilityStatus === "ALMOST_FULL";
  const isFull      = facility.availabilityStatus === "FULL";

    const handleReserve = () => {
    navigate("/services/parking/confirm", { 
        state: { facility, durationHours },
    });
    };

  return (
    <div
      className={[
        "bg-gray-800 border rounded-xl p-4 transition-colors",
        isAlmostFull ? "border-orange-500/60" : "border-gray-700 hover:border-cyan-600",
      ].join(" ")}
    >
      <div className="flex items-start justify-between mb-2">
        <div className="flex-1 mr-4">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <h3 className="text-white font-bold text-[15px] leading-tight">
              {facility.name}
            </h3>
            {isAlmostFull && (
              <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full
                               bg-orange-500/15 text-orange-400 border border-orange-500/30">
                Almost Full
              </span>
            )}
            {isFull && (
              <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full
                               bg-red-500/15 text-red-400 border border-red-500/30">
                Full
              </span>
            )}
          </div>
          <p className="text-gray-400 text-xs flex items-center gap-1">
            <span>📍</span> {facility.address}
          </p>
        </div>

        <div className="text-right shrink-0">
          <p className="text-white font-bold text-xl leading-tight">
            ${facility.estimatedTotal?.toFixed(2)}
          </p>
          <p className="text-gray-400 text-[11px]">est. for {durationHours}h</p>
          <p className="text-gray-400 text-[11px]">
            ${facility.pricePerHour?.toFixed(2)}/h max ${(facility.pricePerHour * 24)?.toFixed(2)}/day
          </p>
        </div>
      </div>

      <div className="flex flex-wrap gap-1.5 mb-3">
        <span className="text-xs px-2.5 py-0.5 rounded-full border border-gray-600 text-gray-400">
          {facility.distanceKm?.toFixed(1)} km away
        </span>
        {facility.amenityTags?.map((tag) => (
          <span
            key={tag}
            className="text-xs px-2.5 py-0.5 rounded-full border border-gray-600 text-gray-400"
          >
            {tag}
          </span>
        ))}
      </div>

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <span className="text-sm">⭐</span>
          <span className="text-white text-sm font-semibold">{facility.rating}</span>
        </div>

        <div className="flex items-center gap-3">
          {isFull ? (
            <span className="text-xs text-red-400">No spots available</span>
          ) : (
            <>
              <span
                className={
                  isAlmostFull
                    ? "text-xs text-orange-400 font-semibold"
                    : "text-xs text-gray-400"
                }
              >
                {facility.availableSpots} spots left
              </span>
              <button
                type="button"
                onClick={handleReserve}
                className="bg-cyan-500 hover:bg-cyan-400 text-black font-bold
                           text-xs px-3 py-1.5 rounded-lg transition-colors"
              >
                Reserve Spot
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}