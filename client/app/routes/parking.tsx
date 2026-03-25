import { useState } from "react";
import { SiteNav } from "../root";
import type { Route } from "./+types/parking";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Parking | SUMMS" },
    {
      name: "description",
      content: "Navigation entry point for the parking feature (external service).",
    },
  ];
}

export default function ParkingPage() {
  const parkingLots = [
    {
      name: "Indigo Place des Arts",
      address: "175 rue Ste-Catherine O.",
      distance: "0.0 km away",
      tags: ["Covered", "24h", "EV Charging", "Security"],
      price: "$21.00",
      note: "est. for 6h",
      spots: "47 spots left",
      rating: "4.6",
    },
    {
      name: "Indigo Ville Marie",
      address: "777 rue de la Gauchetiere O.",
      distance: "0.1 km away",
      tags: ["Covered", "24h", "EV Charging", "Security"],
      price: "$24.00",
      note: "est. for 6h",
      spots: "4 spots left",
      rating: "4.8",
    },
    {
      name: "Parking Vieux-Montreal",
      address: "777 rue de la Gauchetiere O.",
      distance: "0.4 km away",
      tags: ["24h", "Security"],
      price: "$16.50",
      note: "est. for 6h",
      spots: "23 spots left",
      rating: "4.1",
    },
    {
      name: "ABM Parking Centre-Eaton",
      address: "777 rue de la Gauchetiere O.",
      distance: "0.3 km away",
      tags: ["Covered", "24h", "EV Charging", "Security"],
      price: "$27.00",
      note: "est. for 6h",
      spots: "15 spots left",
      rating: "4.8",
    },
  ];
  const [selectedLotName, setSelectedLotName] = useState<string | null>(null);

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight">Parking Finder</h1>
        </header>

        <section className="grid gap-5 xl:grid-cols-[320px_1fr]">
          <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-4">
            <h2 className="mb-1 text-xl font-semibold">Find Parking</h2>
            <p className="mb-3 border-b border-[#2a354a] pb-2 text-sm text-gray-400">Enter your trip details</p>

            <form className="space-y-3" onSubmit={(event) => event.preventDefault()}>
              <div>
                <label htmlFor="destination" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Destination
                </label>
                <input
                  id="destination"
                  type="text"
                  defaultValue="175 rue Ste-Catherine O."
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="arrival-date" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Arrival Date
                </label>
                <input
                  id="arrival-date"
                  type="date"
                  defaultValue="2026-03-23"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="arrival-time" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Arrival Time
                </label>
                <input
                  id="arrival-time"
                  type="text"
                  defaultValue="2:00 PM"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="duration" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Duration (hours)
                </label>
                <input
                  id="duration"
                  type="range"
                  min={1}
                  max={24}
                  defaultValue={6}
                  className="w-full accent-cyan-400"
                />
                <div className="flex justify-between text-xs text-gray-400">
                  <span>1h</span>
                  <span>6h</span>
                  <span>24h</span>
                </div>
              </div>

              <div>
                <label htmlFor="vehicle-type" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Vehicle Type
                </label>
                <select
                  id="vehicle-type"
                  defaultValue="standard"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                >
                  <option value="standard">Standard</option>
                  <option value="compact">Compact</option>
                  <option value="electric">Electric</option>
                  <option value="suv">SUV</option>
                </select>
              </div>

              <div>
                <label htmlFor="max-price" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Max Price / Hr
                </label>
                <input
                  id="max-price"
                  type="text"
                  defaultValue="$5"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-2 pt-1">
                <button
                  type="submit"
                  className="rounded-xl bg-cyan-400 px-4 py-2 text-base font-semibold text-slate-900 transition hover:bg-cyan-300"
                >
                  Find Parking
                </button>
                <button
                  type="button"
                  className="rounded-xl border border-[#50617c] bg-[#13233d] px-4 py-2 text-base font-semibold text-gray-100 transition hover:bg-[#1d2f4d]"
                >
                  Reset
                </button>
              </div>
            </form>
          </article>

          <div className="space-y-3">
            <div className="rounded-xl border border-[#2a354a] bg-[#06142b] px-4 py-3">
              <p className="text-lg font-medium">6 parking lots near 175 rue Ste-Catherine O.</p>
            </div>

            {parkingLots.map((lot) => (
              <article
                key={lot.name}
                className={`cursor-pointer rounded-xl border bg-[#06142b] p-4 transition-colors ${selectedLotName === lot.name ? "border-cyan-400" : "border-[#2a354a] hover:border-[#4b5d79]"}`}
                onClick={() => setSelectedLotName(lot.name)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    setSelectedLotName(lot.name);
                  }
                }}
                role="button"
                tabIndex={0}
                aria-label={`Select ${lot.name}`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="text-xl font-semibold leading-tight">
                      {lot.name}
                      {lot.spots.includes("4 spots") && <span className="ml-2 text-sm text-red-400">Almost Full</span>}
                    </h3>
                    <p className="text-sm text-gray-300">{lot.address}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-3xl font-bold text-gray-100">{lot.price}</p>
                    <p className="text-xs text-gray-400">{lot.note}</p>
                  </div>
                </div>

                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <span className="rounded-full border border-[#4d5f7b] px-2 py-0.5 text-sm text-gray-200">{lot.distance}</span>
                  {lot.tags.map((tag) => (
                    <span key={tag} className="rounded-full border border-[#4d5f7b] px-2 py-0.5 text-sm text-gray-200">
                      {tag}
                    </span>
                  ))}
                </div>

                <div className="mt-3 flex items-center justify-between">
                  <p className="text-sm text-yellow-300">{lot.rating} ★</p>
                  <div className="flex items-center gap-3">
                    <p className="text-xs text-gray-400">{lot.spots}</p>
                    {selectedLotName === lot.name && (
                      <button
                        type="button"
                        className="rounded-lg bg-cyan-400 px-3 py-1.5 text-sm font-semibold text-slate-900 transition hover:bg-cyan-300"
                      >
                        Reserve Spot
                      </button>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>
      </main>
    </>
  );
}
