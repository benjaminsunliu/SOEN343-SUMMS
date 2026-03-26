import { useState } from "react";
import type { ParkingSearchParams } from "../../utils/api";

interface Props {
  onSearch: (params: ParkingSearchParams) => void;
  onReset: () => void;
  loading: boolean;
}

const today = new Date().toISOString().split("T")[0];

const DEFAULTS: ParkingSearchParams = {
  destination:     "",
  city:            "Montreal",
  arrivalDate:     today,
  arrivalTime:     "14:00",
  durationHours:   6,
  vehicleType:     "STANDARD",
  maxPricePerHour: 5,
};

export default function ParkingSearchForm({ onSearch, onReset, loading }: Props) {
  const [form, setForm] = useState<ParkingSearchParams>(DEFAULTS);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "range" || type === "number" ? Number(value) : value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.destination.trim()) return;
    onSearch(form);
  };

  const handleReset = () => {
    setForm(DEFAULTS);
    onReset();
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-gray-800 border border-gray-700 rounded-xl p-5 flex flex-col gap-4"
    >
      {/* Header */}
      <div>
        <h3 className="text-white font-bold text-base">Find Parking</h3>
        <p className="text-gray-400 text-xs mt-0.5">Enter your trip details</p>
      </div>

      {/* Destination */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
          Destination
        </label>
        <input
          type="text"
          name="destination"
          value={form.destination}
          onChange={handleChange}
          placeholder="e.g. 175 rue Ste-Catherine O."
          required
          className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm
                     placeholder-gray-500 focus:outline-none focus:border-cyan-500 transition-colors"
        />
      </div>

      {/* Arrival Date */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
          Arrival Date
        </label>
        <input
          type="date"
          name="arrivalDate"
          value={form.arrivalDate}
          onChange={handleChange}
          className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm
                     focus:outline-none focus:border-cyan-500 transition-colors"
        />
      </div>

      {/* Arrival Time */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
          Arrival Time
        </label>
        <input
          type="time"
          name="arrivalTime"
          value={form.arrivalTime}
          onChange={handleChange}
          className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm
                     focus:outline-none focus:border-cyan-500 transition-colors"
        />
      </div>

      {/* Duration Slider */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide flex items-center gap-2">
          Duration
          <span className="text-cyan-400 font-bold">{form.durationHours}h</span>
        </label>
        <div className="flex items-center gap-2">
          <span className="text-gray-500 text-xs">1h</span>
          <input
            type="range"
            name="durationHours"
            min={1} max={24} step={1}
            value={form.durationHours}
            onChange={handleChange}
            className="flex-1 accent-cyan-500 cursor-pointer"
          />
          <span className="text-gray-500 text-xs">24h</span>
        </div>
      </div>

      {/* Vehicle Type */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
          Vehicle Type
        </label>
        <select
          name="vehicleType"
          value={form.vehicleType}
          onChange={handleChange}
          className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm
                     focus:outline-none focus:border-cyan-500 transition-colors cursor-pointer"
        >
          <option value="STANDARD">Standard</option>
          <option value="COMPACT">Compact</option>
          <option value="EV">Electric Vehicle</option>
          <option value="ACCESSIBLE">Accessible</option>
        </select>
      </div>

      {/* Max Price Slider */}
      <div className="flex flex-col gap-1.5">
        <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide flex items-center gap-2">
          Max Price / hr
          <span className="text-cyan-400 font-bold">${form.maxPricePerHour}/h</span>
        </label>
        <div className="flex items-center gap-2">
          <span className="text-gray-500 text-xs">$1</span>
          <input
            type="range"
            name="maxPricePerHour"
            min={1} max={20} step={0.5}
            value={form.maxPricePerHour}
            onChange={handleChange}
            className="flex-1 accent-cyan-500 cursor-pointer"
          />
          <span className="text-gray-500 text-xs">$20</span>
        </div>
      </div>

      {/* Buttons */}
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50 disabled:cursor-not-allowed
                   text-black font-bold py-2.5 rounded-lg text-sm transition-colors"
      >
        {loading ? "Searching…" : "Find Parking"}
      </button>

      <button
        type="button"
        onClick={handleReset}
        className="w-full bg-gray-700 hover:bg-gray-600 text-white font-medium py-2 rounded-lg text-sm transition-colors"
      >
        Reset
      </button>
    </form>
  );
}