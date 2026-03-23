import { SiteNav } from "../root";
import type { Route } from "./+types/vehicle-search";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Search | SUMMS" },
    { name: "description", content: "Search available vehicles for rental." },
  ];
}

export default function VehicleSearchPage() {
  const vehicles = [
    {
      name: "City Bike #B-41",
      distance: "0.2 km away",
      energy: "Full charge",
      provider: "BIXI Montreal",
      price: "$3.50/hr",
      available: true,
    },
    {
      name: "E-Bike #E-12",
      distance: "0.5 km away",
      energy: "82%",
      provider: "BIXI Montreal",
      price: "$5.00/hr",
      available: true,
    },
    {
      name: "City Bike #B-89",
      distance: "0.6 km away",
      energy: "Full charge",
      provider: "BIXI Montreal",
      price: "Unavailable",
      available: false,
    },
    {
      name: "Scooter #S-07",
      distance: "0.8 km away",
      energy: "91%",
      provider: "Lime",
      price: "$4.00/hr",
      available: true,
    },
    {
      name: "Toyota Corolla",
      distance: "1.1 km away",
      energy: "Full tank",
      provider: "Communauto",
      price: "$12.00/hr",
      available: true,
    },
    {
      name: "Honda Civic",
      distance: "1.4 km away",
      energy: "Electric",
      provider: "Communauto",
      price: "$13.50/hr",
      available: true,
    },
  ];

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight">Find a Vehicle</h1>
        </header>

        <section className="grid gap-5 xl:grid-cols-[320px_1fr]">
          <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-4">
            <h2 className="mb-3 border-b border-[#2a354a] pb-2 text-xl font-semibold">Search Filters</h2>

            <form className="space-y-3" onSubmit={(event) => event.preventDefault()}>
              <div>
                <label className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">Vehicle Type</label>
                <div className="grid grid-cols-3 gap-2 rounded-xl bg-[#14233d] p-1">
                  <button type="button" className="rounded-lg bg-cyan-400 px-2 py-1.5 text-xs font-semibold text-slate-900">
                    Bike
                  </button>
                  <button type="button" className="rounded-lg px-2 py-1.5 text-xs font-semibold text-gray-200 hover:bg-[#1d2f4d]">
                    Scooter
                  </button>
                  <button type="button" className="rounded-lg px-2 py-1.5 text-xs font-semibold text-gray-200 hover:bg-[#1d2f4d]">
                    Car
                  </button>
                </div>
              </div>

              <div>
                <label htmlFor="city" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  City
                </label>
                <input
                  id="city"
                  type="text"
                  defaultValue="Montreal, QC"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="pickup" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Pickup Location
                </label>
                <input
                  id="pickup"
                  type="text"
                  defaultValue="Rue Sainte-Catherine"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="date" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Date
                </label>
                <input
                  id="date"
                  type="date"
                  defaultValue="2026-03-23"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <div>
                <label htmlFor="max-price" className="mb-1.5 block text-xs uppercase tracking-wide text-gray-300">
                  Max Price / Hr
                </label>
                <input
                  id="max-price"
                  type="text"
                  defaultValue="$15.00"
                  className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2 text-sm outline-none"
                />
              </div>

              <button
                type="submit"
                className="w-full rounded-xl bg-cyan-400 px-4 py-2.5 text-lg font-semibold text-slate-900 transition hover:bg-cyan-300"
              >
                Search Vehicles
              </button>
            </form>
          </article>

          <div className="space-y-3">
            <h2 className="text-xl font-semibold">Available Vehicles Near You</h2>

            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {vehicles.map((vehicle) => (
                <article key={vehicle.name} className="relative rounded-2xl border border-[#23324c] bg-[#06142b] p-3.5">
                  <span
                    className={`absolute right-3 top-3 h-3 w-3 rounded-full ${vehicle.available ? "bg-green-400" : "bg-red-400"}`}
                    aria-hidden="true"
                  />
                  <p className="mb-1 text-2xl text-gray-300">#</p>
                  <h3 className="text-xl font-semibold leading-tight">{vehicle.name}</h3>
                  <p className="mt-1 text-base text-gray-300">{vehicle.distance}</p>
                  <p className="mt-1 text-base text-gray-300">{vehicle.energy}</p>
                  <p className="mt-1 text-base text-gray-300">{vehicle.provider}</p>
                  <p className={`mt-2.5 text-2xl font-bold ${vehicle.available ? "text-cyan-400" : "text-gray-400"}`}>
                    {vehicle.price}
                  </p>
                </article>
              ))}
            </div>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <div className="flex items-center justify-between border-b border-[#2a354a] px-4 py-3">
                <h3 className="text-xl font-semibold">Selected: City Bike #B-41</h3>
                <span className="rounded-md bg-green-500/25 px-3 py-1 text-sm font-semibold text-green-400">Bike</span>
              </div>

              <div className="flex flex-col gap-3 px-4 py-4 md:flex-row md:items-end md:justify-between">
                <div className="space-y-1 text-base text-gray-200">
                  <p>Station: Peel & Sainte-Catherine</p>
                  <p>Condition: Excellent</p>
                  <p>Provider: BIXI Montreal</p>
                </div>

                <p className="text-4xl font-bold text-cyan-400">
                  $3.50<span className="text-gray-400">/hr</span>
                </p>
              </div>

              <div className="px-4 pb-4">
                <button
                  type="button"
                  className="w-full rounded-xl bg-cyan-400 px-4 py-2.5 text-xl font-semibold text-slate-900 transition hover:bg-cyan-300"
                >
                  Reserve this Vehicle
                </button>
              </div>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}
