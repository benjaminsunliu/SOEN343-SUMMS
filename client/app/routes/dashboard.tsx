import { SiteNav } from "../root";
import { useNavigate } from "react-router";
import type { Route } from "./+types/dashboard";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Dashboard | SUMMS" },
    {
      name: "description",
      content: "Dashboard overview for rentals, vehicles, parking, and weather.",
    },
  ];
}

export default function DashboardPage() {
  const navigate = useNavigate();

  const statCards = [
    { label: "Active Rentals", value: "847", valueClass: "text-cyan-400" },
    { label: "Available Vehicles", value: "1243", valueClass: "text-blue-500" },
    { label: "Free Parking Spots", value: "382", valueClass: "text-orange-400" },
    { label: "CO2 Saved Today", value: "2.4+", valueClass: "text-violet-400" },
  ];

  const quickActions = [
    { label: "Find a Vehicle", to: "/vehicles/search" },
    { label: "Book / Reserve", to: "/reservation" },
    { label: "Find Parking", to: "/services/parking" },
    { label: "Transit Routes", to: "/services/public-transport" },
  ];

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-3 border-b border-[#253047] pb-2.5">
          <h1 className="text-xl font-bold tracking-tight">Dashboard</h1>
        </header>

        <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2.5 text-amber-300">
          <p className="text-lg font-semibold">Weather Alert</p>
          <p className="text-sm">Heavy rain expected in Montreal. Enclosed vehicles recommended.</p>
        </div>

        <section className="mb-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {statCards.map((card) => (
            <article key={card.label} className="rounded-xl border border-[#2a354a] bg-[#06142b] p-3.5">
              <p className="text-base text-gray-300">{card.label}</p>
              <p className={`mt-2 text-4xl font-bold ${card.valueClass}`}>{card.value}</p>
            </article>
          ))}
        </section>

        <section className="mb-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {quickActions.map((action) => (
            <button
              key={action.label}
              type="button"
              onClick={() => navigate(action.to)}
              className="rounded-xl border border-[#2a354a] bg-[#06142b] px-4 py-2.5 text-base font-semibold text-gray-100 transition hover:bg-[#11213c]"
            >
              {action.label}
            </button>
          ))}
        </section>

        <section className="grid gap-3 xl:grid-cols-[1.8fr_1fr]">
          <article className="rounded-xl border border-[#2a354a] bg-[#06142b]">
            <h2 className="border-b border-[#2a354a] px-4 py-2.5 text-xl font-semibold">Live City Map</h2>
            <div className="p-3.5">
              <div className="relative h-56 rounded-lg border border-[#1f2e49] bg-black">
                <span className="absolute left-[28%] top-[36%] h-3 w-3 rounded-full bg-blue-500" />
                <span className="absolute left-[37%] top-[58%] h-3 w-3 rounded-full bg-cyan-400" />
                <span className="absolute left-[58%] top-[47%] h-3 w-3 rounded-full bg-red-400" />
                <span className="absolute left-[70%] top-[64%] h-3 w-3 rounded-full bg-orange-400" />
              </div>
              <div className="mt-3 flex flex-wrap gap-2 text-sm">
                <span className="rounded-md bg-black px-3 py-1 text-cyan-400">Bike</span>
                <span className="rounded-md bg-black px-3 py-1 text-blue-500">Car</span>
                <span className="rounded-md bg-black px-3 py-1 text-red-400">Scooter</span>
                <span className="rounded-md bg-black px-3 py-1 text-orange-400">Transit</span>
              </div>
            </div>
          </article>

          <div className="space-y-3">
            <article className="rounded-xl border border-[#2a354a] bg-[#06142b] p-3.5">
              <p className="text-4xl font-bold">8C</p>
              <p className="mt-1 text-lg text-gray-300">Heavy Rain - Montreal</p>
              <div className="mt-2 flex items-center gap-4 border-b border-[#2a354a] pb-2 text-base text-gray-200">
                <span>Humidity 94%</span>
                <span>Wind 22km/h</span>
              </div>
              <p className="mt-2 text-sm text-amber-300">Bike-sharing restricted. Cars and Metro recommended.</p>
            </article>

            <article className="rounded-xl border border-[#2a354a] bg-[#06142b]">
              <h2 className="border-b border-[#2a354a] px-4 py-2.5 text-xl font-semibold">My Active Bookings</h2>
              <div className="space-y-3 p-3.5">
                <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2">
                  <p className="text-lg font-semibold">CarShare - Toyota Corolla</p>
                  <p className="text-sm text-gray-300">Mar 18 - 2:00 PM to 5:00 PM</p>
                </div>
                <div className="rounded-lg border border-[#2b3b55] bg-[#14233d] px-3 py-2">
                  <p className="text-lg font-semibold">Bike #B-01</p>
                  <p className="text-sm text-gray-300">Mar 20 - All day</p>
                </div>
              </div>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}


