import { SiteNav } from "../root";
import type { Route } from "./+types/reservation";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservation | SUMMS" },
    { name: "description", content: "Create and manage vehicle reservations." },
  ];
}

export default function ReservationPage() {
  const routeStops = [
    {
      label: "From",
      location: "Peel & Ste-Catherine",
      dateTime: "Mar 18 - 2:00 PM",
    },
    {
      label: "To",
      location: "Plateau Mont-Royal",
      dateTime: "Mar 18 - 4:00 PM",
    },
  ];

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Reservation</h1>
        </header>

        <section className="grid gap-5 xl:grid-cols-[1.05fr_1fr]">
          <div className="space-y-5">
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <h2 className="border-b border-[#2a354a] px-5 py-3 text-xl font-semibold">Reservation Summary</h2>

              <div className="space-y-3 p-5">
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                  <p className="text-xl font-bold leading-tight">Selected: City Bike #B-41</p>
                  <p className="text-base text-gray-300">BIXI Montreal - Excellent condition</p>
                  <p className="text-right text-3xl font-bold leading-none text-cyan-400">$3.50</p>
                </div>

                {routeStops.map((stop) => (
                  <div key={stop.label} className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                    <p className="text-xs uppercase tracking-[0.2em] text-gray-400">{stop.label}</p>
                    <p className="text-xl font-medium leading-tight">{stop.location}</p>
                    <p className="text-base text-gray-300">{stop.dateTime}</p>
                  </div>
                ))}

                <div className="border-y border-[#2d3d57] py-3.5 text-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-200">Duration</span>
                    <span className="font-semibold">2 hrs</span>
                  </div>
                </div>

                <div className="flex items-end justify-between text-2xl">
                  <p className="font-semibold">Total</p>
                  <p className="font-bold text-cyan-400">$7.00</p>
                </div>
              </div>
            </article>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h3 className="mb-3 text-2xl font-semibold">Active Rentals</h3>
              <div className="rounded-xl border border-[#2b3b55] bg-[#14233d] px-4 py-3.5">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-lg font-semibold">Toyota Corolla - Active</p>
                    <p className="text-sm text-gray-300">Returns in 1h 22min</p>
                  </div>
                  <div className="text-right">
                    <p className="rounded-md bg-green-500/25 px-3 py-1 text-xs font-semibold text-green-400">In Use</p>
                    <p className="mt-1.5 text-xl font-bold text-cyan-400">$18.00</p>
                  </div>
                </div>
              </div>
            </article>
          </div>

          <div className="space-y-5">
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h2 className="mb-4 text-2xl font-semibold">Payment</h2>

              <form className="space-y-4" onSubmit={(event) => event.preventDefault()}>
                <div>
                  <label className="mb-1.5 block text-sm uppercase text-gray-300">Payment Method</label>
                  <div className="grid gap-3 sm:grid-cols-2">
                    <button
                      type="button"
                      className="rounded-xl border border-[#50617c] bg-[#13233d] px-4 py-2.5 text-base font-semibold"
                    >
                      Credit Card
                    </button>
                    <button
                      type="button"
                      className="rounded-xl border border-[#50617c] bg-[#13233d] px-4 py-2.5 text-base font-semibold"
                    >
                      Debit Card
                    </button>
                  </div>
                </div>

                <div>
                  <label htmlFor="card-number" className="mb-1.5 block text-sm uppercase text-gray-300">
                    Card Number
                  </label>
                  <input
                    id="card-number"
                    type="text"
                    placeholder="••••"
                    className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none placeholder:text-gray-500"
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label htmlFor="expiry" className="mb-1.5 block text-sm uppercase text-gray-300">
                      Expiry
                    </label>
                    <input
                      id="expiry"
                      type="text"
                      defaultValue="09/27"
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    />
                  </div>

                  <div>
                    <label htmlFor="cvv" className="mb-1.5 block text-sm uppercase text-gray-300">
                      CVV
                    </label>
                    <input
                      id="cvv"
                      type="password"
                      placeholder="•••"
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full rounded-xl bg-cyan-400 px-5 py-2.5 text-xl font-semibold text-slate-950 transition hover:bg-cyan-300"
                >
                  Pay $7.91 & Confirm Booking
                </button>
              </form>
            </article>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <h3 className="border-b border-[#2a354a] px-5 py-3 text-2xl font-semibold">Cancellation Policy</h3>
              <p className="px-5 py-3 text-base leading-snug text-gray-200">
                Cancel up to <span className="font-semibold">1 hour before</span> start time for a full refund.
                Cancellations within 1 hour may incur a $2.00 fee. No-shows are charged the full amount.
              </p>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}
