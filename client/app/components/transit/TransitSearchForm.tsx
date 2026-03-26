import { useState } from "react";
import type { TransitSearchParams } from "../../utils/api";

interface Props {
  onSearch: (params: TransitSearchParams) => void;
  onReset:  () => void;
  loading:  boolean;
}

export default function TransitSearchForm({ onSearch, onReset, loading }: Props) {
  const today = new Date().toISOString().split("T")[0];

  const [origin, setOrigin]           = useState("");
  const [destination, setDestination] = useState("");
  const [date, setDate]               = useState(today);
  const [time, setTime]               = useState("09:00");
  const [type, setType]               = useState("ALL");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch({ origin, destination, date, time, type });
  };

  const handleReset = () => {
    setOrigin(""); setDestination("");
    setDate(today); setTime("09:00"); setType("ALL");
    onReset();
  };

  const labelCls = "block text-xs font-semibold uppercase tracking-wide text-gray-400 mb-1.5";
  const inputCls = "w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:border-cyan-500 transition-colors";

  return (
    <div>
      <h2 className="text-white font-bold text-base mb-0.5">Find Transit</h2>
      <p className="text-gray-500 text-xs mb-4 pb-3 border-b border-gray-800">
        Search STM routes and schedules
      </p>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className={labelCls}>Origin</label>
          <input className={inputCls} placeholder="e.g. Berri-UQAM"
            value={origin} onChange={e => setOrigin(e.target.value)} />
        </div>

        <div>
          <label className={labelCls}>Destination</label>
          <input className={inputCls} placeholder="e.g. Atwater"
            value={destination} onChange={e => setDestination(e.target.value)} />
        </div>

        <div>
          <label className={labelCls}>Date</label>
          <input type="date" className={inputCls}
            value={date} onChange={e => setDate(e.target.value)} />
        </div>

        <div>
          <label className={labelCls}>Departure Time</label>
          <input type="time" className={inputCls}
            value={time} onChange={e => setTime(e.target.value)} />
        </div>

        <div>
          <label className={labelCls}>Transit Type</label>
          <select className={inputCls}
            value={type} onChange={e => setType(e.target.value)}>
            <option value="ALL">All Types</option>
            <option value="METRO">Metro</option>
            <option value="BUS">Bus</option>
            <option value="REM">REM</option>
            <option value="TRAIN">Train (Exo)</option>
          </select>
        </div>

        <button type="submit" disabled={loading}
          className="w-full bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50
                     text-black font-bold py-2.5 rounded-xl text-sm transition-colors">
          {loading ? "Searching…" : "Find Routes"}
        </button>

        <button type="button" onClick={handleReset}
          className="w-full border border-gray-700 text-gray-400 hover:text-white
                     hover:border-gray-500 py-2.5 rounded-xl text-sm transition-colors">
          Reset
        </button>
      </form>
    </div>
  );
}