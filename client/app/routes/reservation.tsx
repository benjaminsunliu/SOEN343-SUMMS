import { SiteNav } from "../root";
import type { Route } from "./+types/reservation";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservation | SUMMS" },
    { name: "description", content: "Create and manage vehicle reservations." },
  ];
}

export default function ReservationPage() {
  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 container mx-auto bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Reservation</h1>
        <p className="text-gray-400">
          This is the placeholder page for reservation steps in the rental lifecycle.
        </p>
      </main>
    </>
  );
}
